package com.ernest.tcp.host.server;

import com.ernest.gui.MainWindow;
import com.ernest.tcp.host.client.ClientMes;
import com.ernest.tcp.utils.StreamUtils;

import javax.swing.*;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 这是一个监听本地 9998 消息通信端口的子线程，用于实时获取其它连接
 */
public class HostMesServer {
    public SwingWorker<Boolean, String> getMesServerWorker(JLabel jl_showRemoteInfo, JTextField jt_inputRemoteIp,
                                                           JTextArea jta_showChat, JButton jb_connect, ClientMes clientMes,
                                                           JList<String> jlt_onlineList){
        return new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                ServerSocket serverSocket = new ServerSocket(9998);
                System.out.println("Debug==> 正在监听 9998 端口，等待消息通信......");
                String chatInfo = "";
                String name;
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Debug==> " + socket.getInetAddress().getHostAddress() + "已连接");
                    // 同时建立反方向的传输信道
                    publish("connect#"+socket.getInetAddress().getHostAddress());

                    InputStream inputStream = socket.getInputStream();
                    while (true) {
                        name = jl_showRemoteInfo.getText();
//                        System.out.println("当前用户名未  " + name);
                        if (name.contains("--")) {
                            name = name.split("--")[1];
                        } else {
                            name = "系统信息";
                        }

                        if (inputStream.available() > 0) {
                            String info = StreamUtils.streamToString(inputStream);
                            if (socket.getInetAddress().getHostAddress().equals(jt_inputRemoteIp.getText())) {
                                chatInfo = name + " : " + info;
                            } else {
                                chatInfo = socket.getInetAddress().getHostAddress() + " : " + info;
                            }
                            publish(chatInfo);
                            if (info.equals("bye")) {
                                break;
                            }
                        }
                    }
                    chatInfo = jta_showChat.getText() + "\n" +
                            "【系统消息: 对方已结束本次通话，你可以继续给对方发消息或输入 bye 向对方告别】";
                    publish(chatInfo);
                    inputStream.close();
//                    socket.close();
//                    serverSocket.close();
//                    return true;
                }
            }

            protected void done(){
                boolean connectStatus;
                try {
                    connectStatus = get();
                    System.out.println("MesDEBUG==>  后台消息监听线程结束");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void process(List<String> infos){
                for (String info : infos) {
                    if(info.contains("connect")){
                        // 建立反向连接
                        if(clientMes == null){
                            String ip = info.split("#")[1];
                            System.out.println("Debug==>  系统建立消息通信反向连接成功");
                            int onlineNum = jlt_onlineList.getModel().getSize();
                            for (int i = 0; i < onlineNum; i++) {
                                String item = jlt_onlineList.getModel().getElementAt(i);
                                if (item.contains(ip)) {
                                    jlt_onlineList.setSelectedIndex(i);
                                }
                            }
                            jb_connect.doClick();
                        }else{
                            System.out.println("Debug==>  系统建立消息通信反向连接失败，已有连接");
                        }

                    }else{
                        jta_showChat.setText(jta_showChat.getText() + "\n  ~~" + infos.get(0));
                    }
                }
            }
        };
    }
}
