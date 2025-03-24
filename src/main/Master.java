package main;
import java.net.*;

public class Master{
    ServerSocket serverSocket;
    Socket socket;

    public static void main(String[] args) {
        new Master().openServer();
    }
    void openServer() {
        new Thread(()-> {
            try {
                System.out.println("Opening server...");
                serverSocket = new ServerSocket(8080);
                while (true) {
                    socket = serverSocket.accept();
                    Thread t = new ActionForWorkers(socket);
                    t.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    serverSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
