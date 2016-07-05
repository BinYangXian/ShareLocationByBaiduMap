package com.zhanghao.learnmina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZH on 2016/1/7.
 */
public class SocketHandler extends IoHandlerAdapter {

    private List<IoSession> sessionsList = new ArrayList<>();

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        System.out.println("链接已经建立");
        sessionsList.add(session);
    }
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        String str = ((String) message).trim();
        System.out.println(str);
        for (IoSession ioSession : sessionsList) {
                if (ioSession != session){
                    ioSession.write(str);
                }
            }
        super.messageReceived(session, message);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        sessionsList.remove(session);
        System.out.println("连接关闭");
    }
}
