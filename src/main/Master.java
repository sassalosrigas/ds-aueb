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
        /*
            Dhmiourgia listas worker kai enarksh tou kathenos
         */
        this.workers = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < workerCount; i++) {
            Worker worker = new Worker(i);
            worker.start();
            workers.add(worker);
        }
    }

    public static void main(String[] args) {
        Master master;
        /*
            An uparxei configuration file pare arithmo worker apo auto alliws apo
            ta arguments tou programmatos
         */
        if (args.length > 0) {
            try {
                int workerCount = Integer.parseInt(args[0]);
                System.out.println("Starting master with " + workerCount + " workers");
                master = new Master(workerCount);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out.println("Invalid argument");
                master = new Master();
            }
        } else {
            System.out.println("Reading number of workers from configuration file");
            master = new Master();
        }
        master.openServer();
    }

    void openServer() {
        /*
            Arxikopoihsh kai ksekinhma leitourgias Master server
         */
        new Thread(()-> {
            try {
                System.out.println("Opening server...");
                serverSocket = new ServerSocket(8080);
                while (true) {
                    socket = serverSocket.accept();
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
        /*
            Hash sunarthsh me bash to onoma tou katasthmatos gia anathesh se swsto worker
         */
        return Math.abs(storeName.hashCode()) % numOfWorkers;
    }

    /*
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

     */

    public Map<String, Integer> aggregateProductSales(String storeName) {
        List<AbstractMap.SimpleEntry<String, Integer>> mappedResults = new ArrayList<>();
        List<Thread> workerThreads = new ArrayList<>();
        for (Worker worker : workers) {
            Thread t = new Thread(()-> {
                synchronized (mappedResults) {
                    mappedResults.addAll(worker.mapProductSales(storeName));
                }
            });
            workerThreads.add(t);
            t.start();
        }
        for (Thread t : workerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Map<String, Integer> results = new HashMap<>();
        synchronized (mappedResults){
            for (AbstractMap.SimpleEntry<String, Integer> entry : mappedResults) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return results;
    }

    /*
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

     */

    public Map<String, Integer> aggregateProductCategorySales() {
        List<AbstractMap.SimpleEntry<String, Integer>> mappedResults =
                Collections.synchronizedList(new ArrayList<>());
        List<Thread> workerThreads = new ArrayList<>();

        for (Worker worker : workers) {
            Thread t = new Thread(() -> {
                List<AbstractMap.SimpleEntry<String, Integer>> workerResults =
                        worker.mapProductCategorySales();
                synchronized (mappedResults) {
                    mappedResults.addAll(workerResults);
                }
            });
            workerThreads.add(t);
            t.start();
        }

        for (Thread t : workerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return mappedResults.stream()
                .collect(Collectors.toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue,
                        Integer::sum
                ));
    }

    /*

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
     */

    public Map<String, Integer> aggregateShopCategorySales() {
        List<AbstractMap.SimpleEntry<String, Integer>> mappedResults = new ArrayList<>();
        List<Thread> workerThreads = new ArrayList<>();
        for (Worker worker : workers) {
            Thread t = new Thread(() -> {
                List<AbstractMap.SimpleEntry<String, Integer>> workerResults = worker.mapShopCategorySales();
                synchronized (mappedResults) {
                    mappedResults.addAll(workerResults);
                }
            });
            workerThreads.add(t);
            t.start();
        }
        for (Thread t : workerThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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
