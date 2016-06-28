package com.jikexueyuan.mysocketclient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends Activity {

    EditText ip;
    EditText editText;
    TextView text;
    private boolean isSocketConnecting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ip = (EditText) findViewById(R.id.ip);
        editText = (EditText) findViewById(R.id.edit);
        text = (TextView) findViewById(R.id.text);

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                ipStr[0] = ip.getText().toString();
                System.out.println("in UI:" + ipStr[0]);
                connect();
            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                send();
            }
        });
    }

    //-------------------------------------

    Socket socket = null;
    BufferedWriter writer = null;
    BufferedReader reader = null;
    final String[] ipStr = new String[1];

    public void connect() {


        AsyncTask<Void, String, Void> read = new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {

                try {
                    socket = new Socket(ip.getText().toString(), 12345);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("in Async:" + ipStr[0]);
                        }
                    });
                    isSocketConnecting =true;
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    publishProgress("@success");
                } catch (IOException e1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "无法建立链接", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                try {
                    String line;
                    while ((line = reader.readLine()) != null&& isSocketConnecting) {
                        System.out.println(line);
                        publishProgress(line);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {//values[0]为上述的String line;
                if (values[0].equals("@success")) {
                    Toast.makeText(MainActivity.this, "链接成功！", Toast.LENGTH_SHORT).show();
                }

                text.append("别人说：" + values[0] + "\n");
                super.onProgressUpdate(values);
            }
        };
        read.execute();

    }

    @Override
    protected void onDestroy() {
        isSocketConnecting =false;
        super.onDestroy();
    }

    public void send() {
        try {
            text.append("我说：" + editText.getText().toString() + "\n");
            writer.write(editText.getText().toString() + "\n");
            writer.flush();
            editText.setText("");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
