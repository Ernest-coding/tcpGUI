package com.ernest.tcp.host.server;

import com.ernest.tcp.host.client.ClientMes;
import com.ernest.tcp.utils.StreamUtils;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ServerMesRunnable {
    
    private ServerSocket serverSocket;
    
    public ServerMesRunnable() {
        try {
            serverSocket = new ServerSocket(9998);
            System.out.println("正在监听 9998 端口，等待消息通信......");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void accessMes(ExecutorService pool, ClientMes clientMes, JTextArea jt_chat, String nowIp, String name) {
        try {
            String chatInfo = "";
            if (name.contains(":")) {
                name = name.split(": ")[1];
            } else {
                name = "系统信息";
            }
            while (true) {
                System.out.println("测试信息");
                Socket socket = serverSocket.accept();
                
                try {
                    System.out.println(socket.getInetAddress().getHostAddress() + "已连接");
                    // 同时建立反方向的传输信道
                    makeSendTran(pool, socket.getInetAddress().getHostAddress(), clientMes);
                    InputStream inputStream = socket.getInputStream();
                    while (true) {
                        if (inputStream.available() > 0) {
                            String info = StreamUtils.streamToString(inputStream);
                            if (socket.getInetAddress().getHostAddress().equals(nowIp)) {
//                                jt_chat.setText(jt_chat.getText() + "\n" + name + " : " + info);
                                chatInfo = jt_chat.getText() + "\n" + name + " : " + info;
                            } else {
//                                jt_chat.setText(jt_chat.getText() + "\n" + socket.getInetAddress().getHostAddress() + " : " + info);
                                chatInfo = jt_chat.getText() + "\n" + socket.getInetAddress().getHostAddress() + " : " + info;
                            }
                            
                            
                            if (info.equals("bye")) {
                                break;
                            }
                        }
//                            Thread.sleep(100);
                    }
                    System.out.println("系统提示：对方已结束本次通话，你可以继续给对方发消息或输入 bye 向对方告别");
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void makeSendTran(ExecutorService pool, String ip, ClientMes clientMes) throws Exception {
        // TODO: 要确定一下这里是值传递还是地址传递
        clientMes = new ClientMes(ip);
    }
    
    public void releaseSource() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
