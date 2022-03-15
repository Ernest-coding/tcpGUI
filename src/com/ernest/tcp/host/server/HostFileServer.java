package com.ernest.tcp.host.server;


import com.ernest.gui.MainWindow;
import com.ernest.tcp.utils.StreamUtils;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * 这是一个监听本地 9997 文件通信端口的子线程，用于实时获取其他连接
 */
public class HostFileServer {
    public SwingWorker<Boolean, String> getFileServerWorker(JTextArea jt_showChat,JLabel jl_message){
        return new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                ServerSocket serverSocket = new ServerSocket(9997);
                System.out.println("Debug==> 正在监听 9997 端口，等待文件通信......");
                String chatInfo = "";
                String name;
                while(true){
                    Socket socket = serverSocket.accept();
                    System.out.println("Debug==> " + socket.getInetAddress().getHostAddress() + "已连接");

                    BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                    try {
                        byte[] bytes = StreamUtils.streamToByteArray(bis);  // 现在已经拿到了客户端发来的文件的字节数组
                        // 4. 将得到的字节数组转换成文件，写入到本地项目 files 目录
                        String savePath = Objects.requireNonNull(MainWindow.class.getResource("/")).getPath() + "\\receives";
                        String fileName = "默认文件名";
                        if (jl_message.getText().contains("--")) {
                            fileName = jl_message.getText().split("--")[1];
                        }
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(savePath+"\\"+fileName));
                        bos.write(bytes);
                        bos.close();
                        chatInfo = "接收对方发送文件成功，保存到: " + savePath +"\\"+ fileName;
                        publish(chatInfo);
                        // 5. 向客户端回复收到文件
                        // 通过 socket 获取输出流，以字符方式处理
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        writer.write("我已收到文件");
                        writer.flush(); // 把内容刷新到数据通道
                        socket.shutdownOutput();    // 设置写入数据的结束标志
                        writer.close();


                        // 关闭其他相关流
                        bis.close();
//                        socket.close();
//                        serverSocket.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            protected void done(){
                boolean status;
                try {
                    status = get();
                    System.out.println("FileDEBUG==>  后台文件监听线程结束");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void process(List<String> chatInfos){
                for (String chatInfo : chatInfos) {
                    jt_showChat.setText(jt_showChat.getText() + "\n" + "【系统消息: " +
                            chatInfo + "】");
                }
            }
        };

    }
}
