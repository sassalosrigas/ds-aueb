package main;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Master{
    ServerSocket serverSocket;
    Socket socket;
    private final List<Worker> workers;

    public Master(){
        this.workers = new ArrayList<Worker>();
        for(int i=0;i<10;i++){
            workers.add(new Worker(i));
        }
    }

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
                    System.out.println("ou mpoi");
                    Thread t = new ActionForWorkers(socket, workers);
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

    public static int hashToWorker(String storeName, int numOfWorkers) {
        return Math.abs(storeName.hashCode()) % numOfWorkers;
    }

}
