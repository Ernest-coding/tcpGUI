package com.ernest.gui;

import com.ernest.tcp.host.remoteClient.RemoteClient;

import javax.swing.*;
import java.util.List;

/**
 * 这是一个每隔 5s 自动刷新在线列表的工具
 */
public class SetOnlineUtil {
    public SwingWorker<Boolean, String[]> getFlushWorker(JList<String> jlt_onlineList){
        return new SwingWorker<>(){
            @Override
            protected Boolean doInBackground() throws Exception {
                while(true){
                    System.out.println("GUIDebug===> 自动刷新在线列表");
                    String[] onlineInfo = RemoteClient.getOnlineInfo();
                    publish(onlineInfo);
                    // 每5秒刷新一次
                    Thread.sleep(5000);
                }
            }

            @Override
            protected void process(List<String[]> onlineInfo){
                for (String[] online : onlineInfo) {
                    jlt_onlineList.setListData(online);
                }
            }
        };
    }
}
