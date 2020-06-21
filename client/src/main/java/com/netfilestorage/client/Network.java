package com.netfilestorage.client;

import com.netfilestorage.common.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

class Network {
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;

    static void start() {
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 50 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void stop() {
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendMsg(FileMessage msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendFileDeleteMessage(String fileToDelete) {
        try {
            out.writeObject(new FileDeleteMessage(fileToDelete));
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static AbstractMessage readObject() throws ClassNotFoundException, IOException {
        Object obj = in.readObject();
        return (AbstractMessage) obj;
    }

    static void sendAuthMessage(String login, String password) {
        try {
            out.writeObject(new AuthMessage(login, password));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendRegMessage(String login, String password) {
        try {
            out.writeObject(new RegMessage(login, password));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Object readInObject() throws IOException, ClassNotFoundException {
        return in.readObject();
    }
}