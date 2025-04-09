package main;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Master{
    ServerSocket serverSocket;
    Socket socket;
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<Worker>());

    public Master(){
        for(int i=0;i<10;i++){
            Worker worker = new Worker(i);
            worker.start();
            workers.add(worker);
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
                    Thread t = new ActionForWorkers(socket, workers, this);
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

//trying mapreduce
    public Map<String, Integer> aggregateProductSales(String storeName) {
        List<AbstractMap.SimpleEntry<String, Integer>> mappedResults = new ArrayList<>();
        for (Worker worker : workers) {
            mappedResults.addAll(worker.mapProductSales(storeName));
        }

        Map<String, Integer> results = new HashMap<>();
        for (AbstractMap.SimpleEntry<String, Integer> entry : mappedResults) {
            results.put(entry.getKey(), entry.getValue());
        }
        return results;
    }

    public Map<String, Integer> aggregateProductCategorySales() {
        List<AbstractMap.SimpleEntry<String, Integer>> mappedResults = new ArrayList<>();
        for (Worker worker : workers) {
            mappedResults.addAll(worker.mapProductCategorySales());
        }
        Map<String, Integer> results = new HashMap<>();
        for (AbstractMap.SimpleEntry<String, Integer> entry : mappedResults) {
            results.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        return results;
    }

    public Map<String, Integer> aggregateShopCategorySales() {
        List<AbstractMap.SimpleEntry<String, Integer>> mappedResults = new ArrayList<>();
        for (Worker worker : workers) {
            mappedResults.addAll(worker.mapShopCategorySales());
        }
        Map<String, Integer> results = new HashMap<>();
        for (AbstractMap.SimpleEntry<String, Integer> entry : mappedResults) {
            results.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
        return results;
    }
}
