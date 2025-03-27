package main;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Master{
    ServerSocket serverSocket;
    Socket socket;
    private List<Worker> workers;

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
            try (ServerSocket serverSocket = new ServerSocket(8080)) {
                System.out.println("Opening server...");
                while (true) {
                    socket = serverSocket.accept();
                    new Thread(()-> {
                        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                            Store store = (Store) in.readObject();
                            int nodeID = Math.abs(store.getStoreName().hashCode()) % workers.size();
                            Worker targetWorker = workers.get(nodeID);
                            targetWorker.proccessRequest("ADD_STORE:" + store.getStoreName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
