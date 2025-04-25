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
    private static int workersSize;

    public Master(){
        this.workers = Collections.synchronizedList(new ArrayList<Worker>());
        this.config = new Properties();

        try (InputStream input = new FileInputStream("src/main/config.properties")) {
            config.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int workerCount = Integer.parseInt(config.getProperty("workerCount"));
        workersSize = workerCount;
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
        workersSize = workerCount;
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
                    socket = serverSocket.accept();  //perimene energeia
                    Thread t = new ActionForWorkers(socket, workers, this); //molis labeis request anethese to se ena kainourio actionsForWorkers thread
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

    public static List<Integer> getWorkerIndicesForStore(String storeName, int numOfWorkers) {
        /*
        Bres ta index twn workers pou tha apothikeuseis to primary kai to replica store
         */
        int mainIndex = Math.abs(storeName.hashCode()) % numOfWorkers;
        int replicaIndex = (mainIndex + 1) % numOfWorkers;
        return Arrays.asList(mainIndex, replicaIndex);
    }

    public Map<String,Integer> reduceProductSales(String storeName) {
        /*
        Reduce sinarthsh gia epistrofh pwlhsewn kathe proiontos enos katasthmatos
         */
        List<AbstractMap.SimpleEntry<String, Integer>> mappedResults = workers.parallelStream()
                .flatMap(worker -> worker.mapProductSales(storeName, workers).stream())//kalese map
                .collect(Collectors.toList());

        Map<String,Integer> result =
                mappedResults.stream().collect(Collectors.toMap
                        (AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, Integer::sum)); //kane reduce kai ipologise sinolo
        return result;
    }



    public Map<String, Integer> reduceProductCategorySales(String productCategory) {
        /*
        Reduce sinarthsh gia tis pwlhseis katasthmatwn se mia sigkekrimenh kathgoria proiontwn
         */
        List<AbstractMap.SimpleEntry<String, Integer>> mappedResults = workers.parallelStream()
                .flatMap(worker -> worker.mapProductCategorySales(workers, productCategory).stream())//kalese map
                .collect(Collectors.toList());

        Map<String, Integer> result =  mappedResults.stream()    //kane reduce kai ipologise sinolo
                .collect(Collectors.toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue,
                        Integer::sum
                ));


        return result;
    }

    public Map<String,Integer> reduceShopCategorySales(String foodCategory) {
        /*
        Sinarthsh pou upologizei pwlhseis kathe katasthmatos pou anhkei se sigkekrimenh kathgoria
         */
        List<AbstractMap.SimpleEntry<String,Integer>> mappedResults = workers.parallelStream().
                flatMap(worker -> worker.mapShopCategorySales(workers, foodCategory).stream()).collect(Collectors.toList()); //kane map

        Map<String,Integer> result = mappedResults.stream().collect(Collectors.toMap(  //kane reduce kai ipologise sinolo
                AbstractMap.SimpleEntry::getKey,
                AbstractMap.SimpleEntry::getValue,
                Integer::sum
        ));

        return result;
    }


    public List<Store> filterStores(String category, double minRate, double maxRate, String priceCat) {
        List<Store> mappedResults = workers.parallelStream().flatMap(worker -> worker.mapFilterStores(
                category, minRate, maxRate, priceCat,workers
        ).stream()).collect(Collectors.toList());
        return mappedResults.stream().distinct().collect(Collectors.toList());
    }

    public boolean isAlive(Worker worker) {
        final boolean[] result = {false};
        Thread t = new Thread(() -> {
            result[0] = worker.ping();
        });
        t.start();
        try {
            t.join(2000); // 2 seconds timeout
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return !t.isAlive() && result[0];
    }



}
