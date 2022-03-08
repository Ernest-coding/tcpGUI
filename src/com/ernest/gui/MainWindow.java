package com.ernest.gui;

import com.ernest.tcp.host.client.ClientMes;
import com.ernest.tcp.host.remoteClient.RemoteClient;
import com.ernest.tcp.host.server.ServerMesRunnable;
import com.ernest.tcp.utils.StreamUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.lang.invoke.VolatileCallSite;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindow {
    private JPanel mainPanel;
    private JPanel online;
    private volatile JList<String> jlt_onlineList;
    private volatile JLabel jl_showOnline;
    private volatile JPanel chat;
    private volatile JTextArea jta_showChat;
    private volatile JTextArea jta_chat;
    private volatile JButton jb_file;
    private volatile JButton jb_send;
    private volatile JLabel jl_showChatInfo;
    private volatile JLabel jl_showRemoteInfo;
    private volatile JPanel menu;
    private volatile JPanel form;
    private volatile JPanel allPanel;
    private volatile JTextField jt_inputHostName;
    private volatile JButton fb_logIn;
    private volatile JTextField jt_inputRemoteIp;
    private volatile JButton jb_connect;
    private volatile JButton jb_delete;
    private volatile JPanel message;
    private volatile JLabel jl_message;
    private volatile JLabel jl_nowTime;
    private volatile JLabel jl_nowTimeText;
    private JButton jb_flushList;
    private volatile JFileChooser jf_fileChoose;
    
    private ClientMes clientMes;
    private List<String> onlines;
    
    public MainWindow() {
        init();
        actionListener();
    
        
    }
    
    /**
     * 系统初始化
     */
    public void init() {
        // 系统初始化
        parseSetNowTime();
        onlines = new ArrayList<>();
    }
    
    /**
     * 处理登录事件
     */
    public void parseLogIn(String hostName) {
        System.out.println("Debug==>  用户操作登录");
        System.out.println(hostName);
        // 登录
        RemoteClient.logInNoti(hostName);
        // 获取在线列表
        String[] onlineInfo = RemoteClient.getOnlineInfo();
        // 解析在线列表并添加进 gui 在线列表中
        jlt_onlineList.setListData(onlineInfo);
        jl_message.setText("登陆成功，已获取到在线用户列表 ~");
        
        // 启动服务监听
        parseBackgroundServer();
    }
    
    /**
     * 处理注销事件
     */
    public void parseLogOut() {
        System.out.println("Debug==>  用户操作注销");
        try {
            parseSendMes("bye");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 处理连接远程主机事件
     *
     * @param ip 远程主机 ip
     */
    public void parseConnect(String ip) throws Exception {
        System.out.println("Debug==>  连接远程主机");
        clientMes = new ClientMes(ip);
    }
    
    /**
     * 处理断开远程连接事件
     */
    public void parseDelete() throws Exception {
        System.out.println("Debug==>  远程主机断开");
        parseLogOut();
        clientMes.releaseSource();
        jta_showChat.setText(jta_showChat.getText() + "【系统消息: 你已单方面断开与对方的连接】");
        jl_message.setText("已断开与远程主机的连接!");
        // TODO:这里要删除这个对象
    }
    
    /**
     * 处理发送消息事件
     *
     * @param message 消息字符串
     */
    public void parseSendMes(String message) throws Exception {
        System.out.println("Debug==>  发送消息");
        jta_showChat.setText(jta_showChat.getText() + "\n  >> 我 : " + message);
        clientMes.sendMes(message);

    }
    
    /**
     * 处理发送文件事件
     * @param filePath 文件路径
     */
    public void parseSendFile(String filePath) {
        System.out.println("Debug==>  发送文件");
        System.out.println(filePath);
    }
    
    /**
     * 处理被动消息通信连接
     * @param ip 被动连接的ip
     */
    public void parsePassiveConnect(String ip){
        SwingWorker<ClientMes, Boolean> passiveConnectWorker = new SwingWorker<>(){
            @Override
            protected ClientMes doInBackground() throws Exception {
                return new ClientMes(ip);
            }
    
            protected void done(){
                try {
                    clientMes = get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        passiveConnectWorker.execute();
    }
    
    /**
     * 处理后台监听 9998 消息通信端口
     */
    public void parseBackgroundServer() {
        SwingWorker<Boolean, String> serverWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                ServerSocket serverSocket = new ServerSocket(9998);
                System.out.println("正在监听 9998 端口，等待消息通信......");
                String chatInfo = "";
                String name;
                while (true) {
                    System.out.println("测试信息");
                    Socket socket = serverSocket.accept();
                    System.out.println(socket.getInetAddress().getHostAddress() + "已连接");
                    // 同时建立反方向的传输信道
                    // TODO: 单机模式跑的话，没法建立反方向，因为一个发送消息端口无法同时连
//                    parsePassiveConnect(socket.getInetAddress().getHostAddress());
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
                    socket.close();
                    serverSocket.close();
                    return true;
                }
            }
            
            protected void done(){
                boolean status;
                try {
                    status = get();
                    System.out.println("后台监听线程结束");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            protected void process(List<String> infos){
                for (String info : infos) {
//                    jta_showChat.setText(jta_showChat.getText() + "\n" + infos.toString());
                    jta_showChat.setText(jta_showChat.getText() + "\n  ~~" + infos.get(0));
                    
                }
            }
            
        };
        serverWorker.execute();
    }
    
    /**
     * 设置当前时间
     */
    public void parseSetNowTime(){
        SwingWorker<Boolean, String> setTimeWorker = new SwingWorker<>(){
            @Override
            protected Boolean doInBackground() throws Exception {
                SimpleDateFormat format = new SimpleDateFormat();
//                format.applyPattern("yyyy-MM-dd HH:mm:ss");
                format.applyPattern("yyyy-MM-dd");
                Calendar calendar = Calendar.getInstance();
                publish(format.format(calendar.getTime()));
                return true;
            }
            
            @Override
            protected void process(List<String> times){
                for (String time : times) {
                    jl_nowTime.setText(time);
                }
            }
        };
        setTimeWorker.execute();
    }
    
    
    /**
     * 批量添加事件监听
     */
    public void actionListener() {
        fb_logIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parseLogIn(jt_inputHostName.getText());
            }
        });
        jb_connect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    parseConnect(jt_inputRemoteIp.getText());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        jb_delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    parseDelete();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
        jb_file.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jf_fileChoose = new JFileChooser(".");
                jf_fileChoose.setDialogTitle("普联-选择文件");
                jf_fileChoose.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jf_fileChoose.showOpenDialog(mainPanel);
                String filePath = jf_fileChoose.getSelectedFile().getAbsolutePath();
                jta_chat.setText("file:" + filePath);
            }
        });
        jb_send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = jta_chat.getText();
                if (!message.contains("file:")) {
                    try {
                        parseSendMes(message);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    parseSendFile(jta_chat.getText().substring(5));
                    
                }
                jta_chat.setText("");
            }
        });
        jlt_onlineList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                jt_inputRemoteIp.setText(jlt_onlineList.getSelectedValue().split("---")[1]);
                jl_showRemoteInfo.setText("对方身份--" + jlt_onlineList.getSelectedValue().split("---")[0]);
            }
        });
        jb_flushList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取在线列表
                String[] onlineInfo = RemoteClient.getOnlineInfo();
                // 解析在线列表并添加进 gui 在线列表中
                jlt_onlineList.setListData(onlineInfo);
            }
        });
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("普联");
        frame.setContentPane(new MainWindow().allPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(600, 250, 700, 500);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
