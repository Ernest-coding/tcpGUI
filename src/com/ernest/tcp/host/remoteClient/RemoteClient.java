package com.ernest.tcp.host.remoteClient;

import com.ernest.tcp.utils.StreamUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class RemoteClient {
    
    private static final Integer remotePort = 9999;
//    private static InetAddress ip;
    private static String ip;

    static {
        try {
//            ip = InetAddress.getLocalHost();
            ip = "115.25.45.172";   // 这是位于学校内的一台服务器
//            ip = "10.23.72.242";
//            ip = "10.39.141.254";
//            ip = InetAddress.getByName("ernest.work");
            System.out.println(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 上线通知
     */
    public static void logInNoti(String hostName) {
        try {
            // 建立连接
            Socket socket = new Socket(ip, remotePort);
//            Socket socket = new Socket("10.23.72.242", remotePort);
            // 获取本机信息并制作辅机问候信息
            String info = "Hi-" + InetAddress.getLocalHost().getHostAddress() + "-" + hostName;
            System.out.println("向服务器发送上线问候信息：" + info);
            // 获取输出流
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(info.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            socket.shutdownOutput();
            // 关闭相关资源
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 下线通知
     */
    public static void logOutNoti(String hostName) {
        try {
            // 建立连接
            Socket socket = new Socket(ip, remotePort);
            // 获取本机信息并制作辅机问候信息
            String info = "Bye-" + InetAddress.getLocalHost().getHostAddress() + "-" + hostName;
            System.out.println("SystemDebug===> 向服务器发送下线告别信息：" + info);
            // 获取输出流
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(info.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            socket.shutdownOutput();
            // 关闭相关资源
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取在线设备列表
     *
     * @return 设备列表的字符数组 ["ip-name","ip-name"]
     */
    public static String[] getOnlineInfo() {
        String[] machineList = null;
        try {
            // 建立连接
            Socket socket = new Socket(ip, remotePort);
            // 发送请求信息
            OutputStream outputStream = socket.getOutputStream();
            System.out.println("SystemDebug===> 向服务器请求获取在线列表");
            outputStream.write("getNowClients".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            // 每次发送完信息必须要调用这个标志，否则服务端会一直等待
            socket.shutdownOutput();
            InputStream inputStream = socket.getInputStream();
            String onlineInfo = StreamUtils.streamToString(inputStream);
            machineList = onlineInfo.split(";");
            if (machineList[0] != null) {
                System.out.println("SystemDebug===> 获取在线列表 成功");
//                for (String s : machineList) {
//                    System.out.println(s);
//                }
            }else{
                System.out.println("SystemDebug===> 获取在线列表 失败");
            }
            // 关闭相关资源
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return machineList;
    }
    
    /**
     * 解析并显示在线主机信息
     *
     * @param onlineInfo 在线主机列表
     * @return 要连接的主机 ip
     */
    public static String getIp(String[] onlineInfo) {
        System.out.println("当前在线的主机列表如下: ");
        for (String info : onlineInfo) {
            System.out.println("[HostName]-" + info.split("-")[1] +
                    "\t[HostIP]-" + info.split("-")[0]);
        }
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入要连接主机的 ip: ");
        String ip = scanner.next();
        scanner.close();
        return ip;
    }
}
