package com.xoease.snowstorm.xmp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;   
import java.security.KeyPair;   
import java.security.KeyPairGenerator;   
import java.security.PrivateKey;   
import java.security.PublicKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;   
import java.security.spec.PKCS8EncodedKeySpec;   
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;   
    
    
public class RSAHelper {   
    
      private final  static String RSA="RSA";
      public static PublicKey getPublicKey(String key) throws Exception {   
            byte[] keyBytes;   
            keyBytes = (new BASE64Decoder()).decodeBuffer(key);   
    
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);   
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);   
            PublicKey publicKey = keyFactory.generatePublic(keySpec);   
            return publicKey;   
      }   
        
      public static PrivateKey getPrivateKey(String key) throws Exception {   
            byte[] keyBytes;   
            keyBytes = (new BASE64Decoder()).decodeBuffer(key);   
    
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);   
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);   
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);   
            return privateKey;   
      }   
    
        
      public static String getKeyString(Key key) throws Exception {   
            byte[] keyBytes = key.getEncoded();   
            String s = (new BASE64Encoder()).encode(keyBytes);   
            return s;   
      }
      /**
       * Encode PublicKey (DSA or RSA encoded) to authorized_keys like string
       *
       * @param publicKey DSA or RSA encoded
       * @param user username for output authorized_keys like string
       * @return authorized_keys like string
       * @throws IOException
       */
      public static String encodePublicKey(PublicKey publicKey, String user)
              throws IOException {
            String publicKeyEncoded;
            if(publicKey.getAlgorithm().equals("RSA")){
                  RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
                  ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
                  DataOutputStream dos = new DataOutputStream(byteOs);
                  dos.writeInt("ssh-rsa".getBytes().length);
                  dos.write("ssh-rsa".getBytes());
                  dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
                  dos.write(rsaPublicKey.getPublicExponent().toByteArray());
                  dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
                  dos.write(rsaPublicKey.getModulus().toByteArray());

                publicKeyEncoded =  (new BASE64Encoder()).encodeBuffer(byteOs.toByteArray());
                  return "ssh-rsa " + publicKeyEncoded + " " + user;
            }
            else if(publicKey.getAlgorithm().equals("DSA")){
                  DSAPublicKey dsaPublicKey = (DSAPublicKey) publicKey;
                  DSAParams dsaParams = dsaPublicKey.getParams();

                  ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
                  DataOutputStream dos = new DataOutputStream(byteOs);
                  dos.writeInt("ssh-dss".getBytes().length);
                  dos.write("ssh-dss".getBytes());
                  dos.writeInt(dsaParams.getP().toByteArray().length);
                  dos.write(dsaParams.getP().toByteArray());
                  dos.writeInt(dsaParams.getQ().toByteArray().length);
                  dos.write(dsaParams.getQ().toByteArray());
                  dos.writeInt(dsaParams.getG().toByteArray().length);
                  dos.write(dsaParams.getG().toByteArray());
                  dos.writeInt(dsaPublicKey.getY().toByteArray().length);
                  dos.write(dsaPublicKey.getY().toByteArray());
                publicKeyEncoded =  (new BASE64Encoder()).encodeBuffer(byteOs.toByteArray());

                  return "ssh-dss " + publicKeyEncoded + " " + user;
            }
            else{
                  throw new IllegalArgumentException(
                          "Unknown public key encoding: " + publicKey.getAlgorithm());
            }
      }
    
      public static void main(String[] args) throws Exception {   
    
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
            //密钥位数   
            keyPairGen.initialize(1024);

            //密钥对   
            KeyPair keyPair = keyPairGen.genKeyPair();
            // 公钥   
            PublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            // 私钥   
            PrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   
    
            String publicKeyString = encodePublicKey(publicKey,"localhost");
            System.out.println("public:\n" + publicKeyString);   
    
            String privateKeyString = getKeyString(privateKey);   
            System.out.println("private:\n-----BEGIN RSA PRIVATE KEY-----\n" + privateKeyString+"\n-----END RSA PRIVATE KEY-----\n");
    
            //加解密类   
            Cipher cipher = Cipher.getInstance(RSA);//Cipher.getInstance("RSA/ECB/PKCS1Padding");   

            //明文   
            byte[] plainText = "我们都很好！邮件：@sina.com".getBytes();   
    
            //加密   
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);   
            byte[] enBytes = cipher.doFinal(plainText);   





           //通过密钥字符串得到密钥   
           // publicKey = getPublicKey(publicKeyString);
            privateKey = getPrivateKey(privateKeyString);   
    
            //解密   
            cipher.init(Cipher.DECRYPT_MODE, privateKey);   
            byte[]deBytes = cipher.doFinal(enBytes);   
    
         /*  publicKeyString = getKeyString(publicKey);
            System.out.println("public:\n" +publicKeyString);   
    
            privateKeyString = getKeyString(privateKey);   
            System.out.println("private:\n" + privateKeyString);*/
    
            String s = new String(deBytes);   
            System.out.println(s);   
    
    
      }   
    
}   