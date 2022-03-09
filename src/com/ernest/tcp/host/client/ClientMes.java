package com.ernest.tcp.host.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientMes {
    private Boolean status = false;
    private Socket socket;
    OutputStream outputStream;

    public ClientMes(String ip) {
        try {
            this.socket = new Socket(ip, 9998);
            status = true;
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMes(String message) {
        if (message != null) {
            try {
                outputStream.write((message + "#").getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public void releaseSource() {
        try {
            status = false;
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
