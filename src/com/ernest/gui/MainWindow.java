package com.ernest.gui;

import com.ernest.tcp.host.client.ClientMes;
import com.ernest.tcp.host.remoteClient.RemoteClient;
import com.ernest.tcp.host.server.HostMesServer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.*;
import java.util.concurrent.ExecutionException;

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
    private volatile JButton jb_flushList;
    private volatile JFileChooser jf_fileChoose;
    
    private ClientMes clientMes;

    private Boolean status = false;
    
    public MainWindow() {
        init();
        actionListener();
    
        
    }
    
    /**
     * 系统初始化
     */
    public void init() {
        // 系统初始化
        new SetTimeUtil().getTimeWorker(jl_nowTime).execute();

    }
    
    /**
     * 处理登录事件
     */
    public void parseLogIn(String hostName) {
        System.out.println("Debug==>  用户操作登录");
        // 登录
        RemoteClient.logInNoti(hostName);
        // 获取在线列表
        String[] onlineInfo = RemoteClient.getOnlineInfo();
        // 解析在线列表并添加进 gui 在线列表中
        jlt_onlineList.setListData(onlineInfo);
        jl_message.setText("登陆成功，已获取到在线用户列表 ~");
        // 启动服务监听子线程
        new HostMesServer().getServerWorker(jl_showRemoteInfo, jt_inputRemoteIp, jta_showChat).execute();
        // 启动自动刷新列表子线程
        new SetOnlineUtil().getFlushWorker(jlt_onlineList).execute();
    }
    
    /**
     * 处理注销事件
     */
    public void parseLogOut(String hostName) {
        System.out.println("Debug==>  用户操作注销");
        try {
            if(clientMes != null){
                parseSendMes("bye");
            }
            RemoteClient.logOutNoti(hostName);
            status = false;
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
        status = true;
        jl_message.setText("连接 " + jt_inputRemoteIp.getText() + " 成功!");
    }
    
    /**
     * 处理断开远程连接事件
     */
    public void parseDelete() throws Exception {
        System.out.println("Debug==>  远程主机断开");
        parseSendMes("bye");
        status = false;
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
                if (status) {
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
                }else{
                    jl_message.setText("请先连接用户，然后再发送消息! ");
                }


            }
        });
        jlt_onlineList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (jlt_onlineList.getSelectedValue() != null){
                    jt_inputRemoteIp.setText(jlt_onlineList.getSelectedValue().split("---")[1]);
                    jl_showRemoteInfo.setText("对方身份--" + jlt_onlineList.getSelectedValue().split("---")[0]);
                }
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
        MainWindow mainWindow = new MainWindow();
        frame.setContentPane(mainWindow.allPanel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(600, 250, 700, 500);
        frame.setResizable(false);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                String hostName = mainWindow.jt_inputHostName.getText();
                mainWindow.parseLogOut((hostName != null) ? hostName : "disMach");
                System.exit(0);
            }
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {}
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
        frame.setVisible(true);
    }

}
