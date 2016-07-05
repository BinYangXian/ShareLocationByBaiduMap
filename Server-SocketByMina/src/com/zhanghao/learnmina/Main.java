package com.zhanghao.learnmina;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by ZH on 2016/1/7.
 */
public class Main {
    private static final int PORT = 12345;
    public static void main(String[] args){
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("Line",new ProtocolCodecFilter(new TextLineCodecFactory()));
        acceptor.setHandler(new SocketHandler());
        try {
            acceptor.bind(new InetSocketAddress(PORT));
            System.out.println("Server started at port "+ PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
