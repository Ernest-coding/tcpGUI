package com.ernest.gui;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * 这是一个用于实时更新程序时间显示的工具
 */

public class SetTimeUtil {
    public SwingWorker<Boolean, String> getTimeWorker(JLabel jl_nowTime){
        return new SwingWorker<>(){
            @Override
            protected Boolean doInBackground() throws Exception {
                SimpleDateFormat format = new SimpleDateFormat();
//                format.applyPattern("yyyy-MM-dd HH:mm:ss");
                format.applyPattern("yyyy-MM-dd HH:mm:ss");

                while (true){
                    Calendar calendar = Calendar.getInstance();
                    publish(format.format(calendar.getTime()));
                    calendar = null;
                }
            }

            @Override
            protected void process(List<String> times){
                for (String time : times) {
                    jl_nowTime.setText(time);
                }
            }
        };
    }
}