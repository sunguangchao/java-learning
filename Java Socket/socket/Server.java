package socket;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 11981 on 2017/6/11.
 * 基于TCP协议的Socket通信，实现用户登陆
 * 服务器端
 */
public class Server {
    public static void main(String[] args) {
        try{
            //创建一个服务器端Socket，即SocketServer，指定绑定的端口，并监听此端口
            ServerSocket serverSocket = new ServerSocket(8888);
            Socket socket = null;
            int count = 0;
            System.out.println("服务器即将启动，正在等待客户端的连接");
            //循环监听等待客户端的连接
            while (true){
                //调用accept()方法开始监听，等待客户端的连接
                socket = serverSocket.accept();
                ServerThread serverThread = new ServerThread(socket);
                serverThread.start();
                count++;
                System.out.println("客户端的数量为："+count);
                InetAddress address = socket.getInetAddress();
                System.out.println("当前客户端的IP："+address.getHostAddress());

            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
