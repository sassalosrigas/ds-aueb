package main;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Master{
    ServerSocket serverSocket;
    Socket socket;
    private final List<Worker> workers;
    private Properties config;

    public Master(){
        this.workers = Collections.synchronizedList(new ArrayList<Worker>());
        this.config = new Properties();

        try (InputStream input = new FileInputStream("src/main/config.properties")) {
            config.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int workerCount = Integer.parseInt(config.getProperty("workerCount"));
        for (int i = 0; i < workerCount; i++) {
            Worker worker = new Worker(i);
            worker.start();
            workers.add(worker);
        }
    }

    public Master(int workerCount) {
        this.workers = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < workerCount; i++) {
            Worker worker = new Worker(i);
            worker.start();
            workers.add(worker);
            System.out.println("Initialized Worker " + i);
        }
    }

    public static void main(String[] args) {
        Master master;
        if (args.length > 0) {
            try {
                int workerCount = Integer.parseInt(args[0]);
                master = new Master(workerCount);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                master = new Master();
            }
        } else {
            master = new Master();
        }
        master.openServer();
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

    public List<Store> filterStores(String category, double minRate, double maxRate, String priceCat) {
        List<Store> mappedResults = workers.parallelStream().flatMap(worker -> worker.mapFilterStores(
                category, minRate, maxRate, priceCat
        ).stream()).collect(Collectors.toList());
        return mappedResults.stream().distinct().collect(Collectors.toList());
    }

}
