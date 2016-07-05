package com.jikexueyuan.locatebybaidumap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MyService extends Service {

    Socket socket = null;
    BufferedWriter writer = null;
    BufferedReader reader = null;
    public MyService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    public class Binder extends android.os.Binder {
        public void sendData(String localLocateData) {
            try {
                writer.write(localLocateData +"\n");                  //向服务端发送定位数据
                writer.flush();// TODO: 2016/6/3 果然是有缓存冲刷类似语句，靠 卡了半天！
                System.out.println("发送出去的："+localLocateData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public MyService getService(){//外界通过该方法来添加事件的绑定
            return MyService.this;
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("192.168.5.100", 12345);
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    String otherLocateData = "";
                    while ((otherLocateData = reader.readLine()) != null) {
                        System.out.println(otherLocateData);
                        if (callBack!=null){
                            callBack.onDateChange(otherLocateData);      // 将otherLocateData传给MainActivity，
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private ICallBack callBack=null;//初值设置为null

    public void setCallBack(ICallBack callBack) {
        this.callBack = callBack;
    }
    public interface ICallBack{
        void onDateChange(String data);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Service on Destroy");
    }
}
