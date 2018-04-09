package com.xoease.snowstorm.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.security.auth.Destroyable;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.thread.ShutdownThread;

import com.xoease.snowstorm.server.SnowAbstractServer;

public class ShutdownMonitor 
{
    private final Set<LifeCycle> _lifeCycles = new CopyOnWriteArraySet<LifeCycle>();
	private static final Logger LOG = Log.getLogger(ShutdownMonitor.class);

    // Implementation of safe lazy init, using Initialization on Demand Holder technique.
    static class Holder
    {
        static ShutdownMonitor instance = new ShutdownMonitor();
    }

    public static ShutdownMonitor getInstance()
    {
        return Holder.instance;
    }
    
    /* ------------------------------------------------------------ */
    public static synchronized void register(LifeCycle... lifeCycles)
    {
        getInstance()._lifeCycles.addAll(Arrays.asList(lifeCycles));
    }

   
    /* ------------------------------------------------------------ */
    public static synchronized void deregister(LifeCycle lifeCycle)
    {
        getInstance()._lifeCycles.remove(lifeCycle);
    }
    
    /* ------------------------------------------------------------ */
    public static synchronized boolean isRegistered(LifeCycle lifeCycle)
    {
        return getInstance()._lifeCycles.contains(lifeCycle);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * ShutdownMonitorRunnable
     *
     * Thread for listening to STOP.PORT for command to stop Jetty.
     * If ShowndownMonitor.exitVm is true, then Sytem.exit will also be
     * called after the stop.
     *
     */
    private class ShutdownMonitorRunnable implements Runnable
    {
        public ShutdownMonitorRunnable()
        {
            startListenSocket();
        }
        
        @Override
        public void run()
        {
            if (serverSocket == null)
            {
                return;
            }

            while (serverSocket != null)
            {
                Socket socket = null;
                try
                {
                    socket = serverSocket.accept();

                    LineNumberReader lin = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
                    String receivedKey = lin.readLine();
                    if (!key.equals(receivedKey))
                    {
                        LOG.info("Ignoring command with incorrect key");
                        continue;
                    }

                    OutputStream out = socket.getOutputStream();

                    String cmd = lin.readLine();
                    debug("command={}",cmd);
                    if ("stop".equalsIgnoreCase(cmd)) //historic, for backward compatibility
                    {
                        //Stop the lifecycles, only if they are registered with the ShutdownThread, only destroying if vm is exiting
                        debug("Issuing stop...");
                        
                        for (LifeCycle l:_lifeCycles)
                        {
                            try
                            {
                                if (l.isStarted() && ShutdownThread.isRegistered(l))
                                {
                                    l.stop();
                                }
                                
                                if ((l instanceof Destroyable) && exitVm)
                                    ((Destroyable)l).destroy();
                            }
                            catch (Exception e)
                            {
                                debug(e);
                            }
                        }

                        //Stop accepting any more commands
                        stopInput(socket);

                        // Reply to client
                        debug("Informing client that we are stopped.");
                        informClient(out, "Stopped\r\n");

                        //Stop the output and close the monitor socket
                        stopOutput(socket);

                        if (exitVm)
                        {
                            // Kill JVM
                            debug("Killing JVM");
                            System.exit(0);
                        }
                    }
                    else if ("forcestop".equalsIgnoreCase(cmd))
                    {
                        debug("Issuing force stop...");
                        
                        //Ensure that objects are stopped, destroyed only if vm is forcibly exiting
                        stopLifeCycles(exitVm);

                        //Stop accepting any more commands
                        stopInput(socket);

                        // Reply to client
                        debug("Informing client that we are stopped.");
                        informClient(out, "Stopped\r\n");

                        //Stop the output and close the monitor socket
                        stopOutput(socket);
                        
                        //Honour any pre-setup config to stop the jvm when this command is given
                        if (exitVm)
                        {
                            // Kill JVM
                            debug("Killing JVM");
                            System.exit(0);
                        }
                    }
                    else if ("stopexit".equalsIgnoreCase(cmd))
                    {
                        debug("Issuing stop and exit...");
                        //Make sure that objects registered with the shutdown thread will be stopped
                        stopLifeCycles(true);
                        
                        //Stop accepting any more input
                        stopInput(socket);

                        // Reply to client
                        debug("Informing client that we are stopped.");                       
                        informClient(out, "Stopped\r\n");              

                        //Stop the output and close the monitor socket
                        stopOutput(socket);
                        
                        debug("Killing JVM");
                        System.exit(0);
                    }
                    else if ("exit".equalsIgnoreCase(cmd))
                    {
                        debug("Killing JVM");
                        System.exit(0);
                    }
                    else if ("status".equalsIgnoreCase(cmd))
                    {
                        // Reply to client
                        informClient(out, "OK\r\n");
                    }
                }
                catch (Exception e)
                {
                    debug(e);
                   LOG.info(e.toString());
                }
                finally
                {
                    close(socket);
                    socket = null;
                }
            }
        }
        
        public void stopInput (Socket socket)
        {
            //Stop accepting any more input
            close(serverSocket);
            serverSocket = null;
            //Shutdown input from client
            shutdownInput(socket);  
        }
        
        public void stopOutput (Socket socket) throws IOException
        {
            socket.shutdownOutput();
            close(socket);
            socket = null;                        
            debug("Shutting down monitor");
            serverSocket = null;
        }
        
        public void informClient (OutputStream out, String message) throws IOException
        {
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }

        /**
         * Stop the registered lifecycles, optionally
         * calling destroy on them.
         * 
         * @param destroy true if {@link Destroyable}'s should also be destroyed.
         */
        public void stopLifeCycles (boolean destroy)
        {
            for (LifeCycle l:_lifeCycles)
            {
                try
                {
                    if (l.isStarted())
                    {
                        l.stop();
                    }
                    
                    if ((l instanceof Destroyable) && destroy)
                        ((Destroyable)l).destroy();
                }
                catch (Exception e)
                {
                    debug(e);
                }
            }
        }

        public void startListenSocket()
        {
            if (port < 0)
            {            
                if (DEBUG)
                	 LOG.info("ShutdownMonitor not in use (port < 0): " + port);
                return;
            }

            try
            {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(InetAddress.getByName(host), port), 1);
                if (port == 0)
                {
                    // server assigned port in use
                    port = serverSocket.getLocalPort();
                    LOG.info("STOP.PORT={}",port);
                }

                if (key == null)
                {
                    // create random key
                    key = Long.toString((long)(Long.MAX_VALUE * Math.random() + this.hashCode() + System.currentTimeMillis()),36);
                    LOG.info("STOP.KEY={}",key);
                }
            }
            catch (Exception e)
            {
                debug(e);
                LOG.debug("Error binding monitor port " + port + ": " + e.toString());
                serverSocket = null;
            }
            finally
            {
                // establish the port and key that are in use
                debug("STOP.PORT={}",port);
                debug("STOP.KEY={}",key);
                debug("{}",serverSocket);
            }
        }

    }
    
    private boolean DEBUG;
    private String host;
    private int port;
    private String key;
    private boolean exitVm;
    private ServerSocket serverSocket;
    private Thread thread;

    /**
     * Create a ShutdownMonitor using configuration from the System properties.
     * <p>
     * <code>STOP.PORT</code> = the port to listen on (empty, null, or values less than 0 disable the stop ability)<br>
     * <code>STOP.KEY</code> = the magic key/passphrase to allow the stop (defaults to "eclipse")<br>
     * <p>
     * Note: server socket will only listen on localhost, and a successful stop will issue a System.exit() call.
     */
    private ShutdownMonitor()
    {
        Properties props = System.getProperties();

        this.DEBUG = props.containsKey("DEBUG");

        // Use values passed thru via /jetty-start/
        this.host = props.getProperty("STOP.HOST","127.0.0.1");
        this.port = Integer.parseInt(props.getProperty("STOP.PORT","-1"));
        this.key = props.getProperty("STOP.KEY",null);
        this.exitVm = true;
    }

    private void close(ServerSocket server)
    {
        if (server == null)
        {
            return;
        }

        try
        {
            server.close();
        }
        catch (IOException ignore)
        {
            debug(ignore);
        }
    }

    private void close(Socket socket)
    {
        if (socket == null)
        {
            return;
        }

        try
        {
            socket.close();
        }
        catch (IOException ignore)
        {
            debug(ignore);
        }
    }

    
    private void shutdownInput(Socket socket)
    {
        if (socket == null)
            return;
        
        try
        {
            socket.shutdownInput();
        }   
        catch (IOException ignore)
        {
            debug(ignore);
        }
    }
    
    
    private void debug(String format, Object... args)
    {
        if (DEBUG)
        {
            LOG.debug("[ShutdownMonitor] " + format ,args);
        }
    }

    private void debug(Throwable t)
    {
        if (DEBUG)
        {
            t.printStackTrace(System.err);
        }
    }

    public String getKey()
    {
        return key;
    }

    public int getPort()
    {
        return port;
    }

    public ServerSocket getServerSocket()
    {
        return serverSocket;
    }

    public boolean isExitVm()
    {
        return exitVm;
    }


    public void setDebug(boolean flag)
    {
        this.DEBUG = flag;
    }

    /**
     * @param exitVm true to exit the VM on shutdown
     */
    public void setExitVm(boolean exitVm)
    {
        synchronized (this)
        {
            if (thread != null && thread.isAlive())
            {
                throw new IllegalStateException("ShutdownMonitorThread already started");
            }
            this.exitVm = exitVm;
        }
    }

    public void setKey(String key)
    {
        synchronized (this)
        {
            if (thread != null && thread.isAlive())
            {
                throw new IllegalStateException("ShutdownMonitorThread already started");
            }
            this.key = key;
        }
    }

    public void setPort(int port)
    {
        synchronized (this)
        {
            if (thread != null && thread.isAlive())
            {
                throw new IllegalStateException("ShutdownMonitorThread already started");
            }
            this.port = port;
        }
    }

    public void start() throws Exception
    {
        Thread t = null;
        
        synchronized (this)
        {
            if (thread != null && thread.isAlive())
            {
                if (DEBUG)
                    LOG.debug("ShutdownMonitorThread already started");
                return; // cannot start it again
            }
         
            thread = new Thread(new ShutdownMonitorRunnable());
            thread.setDaemon(true);
            thread.setName("ShutdownMonitor");
            t = thread;
        }
         
        if (t != null)
            t.start();
    }


    protected boolean isAlive ()
    {
        boolean result = false;
        synchronized (this)
        {
            result = (thread != null && thread.isAlive());
        }
        return result;
    }
    
 
    @Override
    public String toString()
    {
        return String.format("{}[port={}]",this.getClass().getName(),port);
    }
}
