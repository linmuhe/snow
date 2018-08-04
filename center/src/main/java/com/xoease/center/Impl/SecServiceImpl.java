package com.xoease.center.Impl;

import com.albedo.java.thrift.rpc.server.service.IThriftServerService;
import com.albedo.java.thrift.rpc.server.service.ThriftServerService;
import com.xoease.centerinterface.SecService;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.springframework.stereotype.Service;

@Service
public class SecServiceImpl extends ThriftServerService implements SecService.Iface {
    @Override
    public String getName() {
        return "secService";
    }

    @Override
    public TProcessor getProcessor(IThriftServerService bean) {
        SecService.Iface impl = ( SecService.Iface ) bean;
        return new SecService.Processor<>(impl);
    }

    @Override
    public boolean isOk(String token, String host) throws TException {
        return false;
    }
}
