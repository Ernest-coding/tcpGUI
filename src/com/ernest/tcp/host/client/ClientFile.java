package com.ernest.tcp.host.client;

import com.ernest.tcp.utils.StreamUtils;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class ClientFile {
    private Socket socket;
    OutputStream outputStream;

    /**
     * 初始化构造方法，建立连接
     * @param ip 目的主机ip
     */
    public ClientFile(String ip){
        try {
            this.socket = new Socket(ip, 9997);
            this.outputStream = this.socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送文件模块，直接从本地读取文件然后将文件转换为流，发送到目的主机
     * @param filePath 文件路径
     * @return 返回一个发送结果，若显示默认信息，则说明发送过程中出了问题，发送失败，否则应该显示正常的信息
     */
    public SwingWorker<String, String> getFileSender(String filePath, JTextArea jta_showChat){
        return new SwingWorker<>(){
            @Override
            protected String doInBackground() throws Exception {
                String info = "默认信息，即发送失败";
                try {
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
                    byte[] bytes = StreamUtils.streamToByteArray(bis);
                    BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                    bos.write(bytes);   // 将文件对应的字节数组的内容写入到数据通道
                    bis.close();
                    socket.        shutdownOutput();    // 设置写入数据的结束标志

                    // 接收从服务端回复的消息
                    InputStream inputStream = socket.getInputStream();
                    info = StreamUtils.streamToString(inputStream);
                    System.out.println(info);
                    inputStream.close();
                    // 关闭相关的流
//                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return info;
            }

            protected void done(){
                try {
                    String info = get();
                    jta_showChat.setText(jta_showChat.getText() + "\n" + "【系统消息: " + info + "】");
//                    outputStream.close();
//                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * 完成发送之后关闭相关资源
     */
    public void finishSend(){
        try {
            this.outputStream.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
