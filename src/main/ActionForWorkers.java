package main;

import java.io.*;
import java.net.*;
import java.util.*;

public class ActionForWorkers extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    private List<Worker> workers;
    private final Master master;

    public ActionForWorkers(Socket connection, List<Worker> workers, Master master) {
        this.workers = workers;
        this.master = master;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try{
            try {
                while (true) {
                    WorkerFunctions request = (WorkerFunctions) in.readObject();
                    processRequest(request);
                }
            } catch (EOFException e) {
                System.out.println("Client disconnected.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void rebalanceStores() {
        List<Store> allStores = new ArrayList<>();
        for (Worker worker : workers) {
            allStores.addAll(worker.getAllStores());
        }
        for (Worker worker : workers) {
            worker.clearStores();
        }
        int numWorkers = workers.size();
        Map<Integer, List<Store>> storeAssignment = new HashMap<>();

        for (Store store : allStores) {
            int workerIndex = Master.hashToWorker(store.getStoreName(), numWorkers);
            storeAssignment.computeIfAbsent(workerIndex, k -> new ArrayList<>()).add(store);
        }
        for (Map.Entry<Integer, List<Store>> entry : storeAssignment.entrySet()) {
            int workerIndex = entry.getKey();
            if (workerIndex < workers.size()) {
                workers.get(workerIndex).addStores(entry.getValue());
            }
        }

        System.out.println("Stores rebalanced across " + numWorkers + " workers");
    }

    public void handleWorkerScaleRequest(int newWorkerCount) {
        if (newWorkerCount <= 0) {
            System.out.println("Error: Need at least 1 worker");
            return;
        }

        synchronized (workers) {
            int currentCount = workers.size();

            if (newWorkerCount == currentCount) {
                System.out.println("Worker count unchanged");
                return;
            }

            System.out.println("Scaling from " + currentCount + " to " + newWorkerCount + " workers");

            if (newWorkerCount > currentCount) {
                for (int i = currentCount; i < newWorkerCount; i++) {
                    Worker worker = new Worker(i);
                    worker.start();
                    workers.add(worker);
                    System.out.println("Added worker " + i);
                }
            }
            else {
                List<Store> storesToReassign = new ArrayList<>();
                for (int i = newWorkerCount; i < currentCount; i++) {
                    Worker worker = workers.get(i);
                    storesToReassign.addAll(worker.getAllStores());
                    worker.shutdown();
                }
                workers = workers.subList(0, newWorkerCount);
                if (!storesToReassign.isEmpty()) {
                    workers.get(0).addStores(storesToReassign);
                }
            }

            rebalanceStores();

            System.out.println("Scaling completed. Current workers: " + workers.size());
        }
    }

    public void processRequest(WorkerFunctions request) {
        /*
            Dexetai ena WorkerFunctions antikeimeno kai afou diabazei to operation kanei tis
            antistoixes energeies kai anathetei thn diadikasia se enan worker ston opoio kanei hash gia thn epilogh tou
         */
        try{
            String operation = request.getOperation();
            if(operation.equals("ADD_STORE")) {
                Store store = (Store)request.getObject();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if(master.isAlive(primary)){
                    primary.receiveTask(() -> {
                        boolean added = primary.addStore(store);
                        try {
                            if (added) {
                                out.writeObject(store);
                            } else {
                                out.writeObject("Store is already registered");
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    // update replica

                    replica.receiveTask(() -> {
                        replica.addStore(store);
                    });
                }else{
                    replica.receiveTask(() -> {
                        boolean added = replica.addStore(store);
                        try {
                            if (added) {
                                out.writeObject(store);
                            } else {
                                out.writeObject("Store is already registered");
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }else if(operation.equals("ADD_PRODUCT")) {
                System.out.println("product");
                Store store = (Store) request.getObject();
                Product product = (Product)request.getObject2();
                System.out.println("Current store " + store.getStoreName());
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if (master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                        boolean existed = primary.addProduct(store, product);
                        try {
                            if(existed){
                                out.writeObject("Product " + product.getProductName() + " was set online");
                            }else{
                                out.writeObject("Product " + product.getProductName() + " was registered successfully");
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    //update replica

                    replica.receiveTask(() -> {
                        replica.addProduct(store, product);
                    });
                }else {
                    replica.receiveTask(() -> {
                        boolean existed = replica.addProduct(store, product);
                        try {
                            if(existed){
                                out.writeObject("Product " + product.getProductName() + " was set online");
                            }else {
                                out.writeObject("Product " + product.getProductName() + " was registered successfully");
                            }
                            out.flush();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    });
                }
            } else if(operation.equals("REMOVE_PRODUCT")) {
                Store store = (Store) request.getObject();
                Product product = (Product) request.getObject2();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if (master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                        boolean removed = primary.removeProduct(store, product);
                        try {
                            if(removed){
                                out.writeObject("Removed product " + product.getProductName() +  " from " + store.getStoreName());
                            }else{
                                out.writeObject("Product is already offline");
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    replica.receiveTask(() -> {
                        replica.removeProduct(store, product);
                    });
                }else {
                    replica.receiveTask(() -> {
                        boolean removed = replica.removeProduct(store, product);
                        try {
                            if(removed){
                                out.writeObject("Removed product " + product.getProductName() +  " from " + store.getStoreName());
                            }else{
                                out.writeObject("Product is already offline");
                            }
                        }catch (IOException e) {
                            e.printStackTrace();
                        }

                    });
                }
            }else if(operation.equals("MODIFY_STOCK")) {
                String storeName = request.getName();
                String productName = request.getName2();
                int quantity = request.getNum();
                List<Integer> assign = Master.getWorkerIndicesForStore(storeName, workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if(master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                    primary.modifyStock(storeName, productName, quantity);
                    try {
                        out.writeObject(storeName);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    });
                    replica.receiveTask(() -> {
                        replica.modifyStock(storeName, productName, quantity);
                    });
                }else{
                    replica.receiveTask(() -> {
                        replica.modifyStock(storeName, productName, quantity);
                        try{
                            out.writeObject(storeName);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                };
            }else if(operation.equals("SHOW_STORES")) {
                Customer customer = (Customer)request.getObject();
                List<Store> stores = new ArrayList<Store>();
                List<Thread> workerThreads = new ArrayList<>();
                for(Worker worker: workers) {
                    Thread t = new Thread(() -> {
                        List<Store> workerStores = worker.showStores(customer);
                        synchronized(stores) {
                            stores.addAll(workerStores);
                        }
                    });
                    workerThreads.add(t);
                    t.start();
                }
                for(Thread t : workerThreads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    out.writeObject(stores);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if(operation.equals("SHOW_ALL_STORES")) {
                List<Store> stores = new ArrayList<Store>();
                List<Thread> workerThreads = getThreads(stores);
                for (Thread t : workerThreads) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    out.writeObject(stores);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else if (operation.equals("FILTER_STORES")) {
                String foodCategory = request.getName();
                double lowerStars = request.getDouble1();
                double upperStars = request.getDouble2();
                String priceCategory = request.getName2();

                List<Store> results = master.filterStores(foodCategory, lowerStars, upperStars, priceCategory);
                out.writeObject(results);
            }else if(operation.equals("APPLY_RATING")){
                Store store = (Store)request.getObject();
                int rating = request.getNum();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if (master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                        primary.rateStore(store, rating);
                        try {
                            out.writeObject("Successful");
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    replica.receiveTask(() -> {
                        replica.rateStore(store, rating);
                    });
                }else {
                    replica.receiveTask(() -> {
                        replica.rateStore(store, rating);
                        try{
                            out.writeObject("Successful");
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }else if (operation.equals("PRODUCT_SALES")) {
                String storeName = (String) request.getName();
                Map<String, Integer> results = this.master.aggregateProductSales(storeName);
                out.writeObject(results);
            }else if (operation.equals("PRODUCT_CATEGORY_SALES")) {
                Map<String, Integer> results = this.master.aggregateProductCategorySales();
                out.writeObject(results);
            }else if (operation.equals("SHOP_CATEGORY_SALES")) {
                Map<String, Integer> results = this.master.aggregateShopCategorySales();
                out.writeObject(results);
            }else if(operation.equals("RESERVE_PRODUCT")){
                Store store = (Store)request.getObject2();
                Product product = (Product) request.getObject();
                Customer customer = (Customer) request.getObject3();
                int quantity = request.getNum();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if (master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                        boolean reserved = primary.reserveProduct(store, product, customer, quantity);
                        try {
                            if (reserved) {
                                out.writeObject(new Customer.ProductOrder(product.getProductName(), quantity));
                            } else {
                                out.writeObject("Reservation failed");
                            }
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    replica.receiveTask(() -> {
                        replica.reserveProduct(store, product,customer, quantity);
                    });
                }else {
                    replica.receiveTask(() -> {
                        boolean reserved = replica.reserveProduct(store, product, customer,quantity);
                        try {
                            if (reserved) {
                                out.writeObject(new Customer.ProductOrder(product.getProductName(), quantity));
                            } else {
                                out.writeObject("Reservation failed");
                            }
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }else if(operation.equals("COMPLETE_PURCHASE")){
                Store store = (Store)request.getObject();
                Customer customer = (Customer) request.getObject2();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if (master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                        boolean completed = primary.completePurchase(store.getStoreName(), customer.getUsername());
                        try {
                            if (completed) {
                                out.writeObject("Purchase successful");
                            } else {
                                out.writeObject("Purchase failed");
                            }
                            out.flush();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    replica.receiveTask(() -> {
                        replica.completePurchase(store.getStoreName(), customer.getUsername());
                    });
                }else {
                    replica.receiveTask(() -> {
                        boolean completed = replica.completePurchase(store.getStoreName(), customer.getUsername());
                        try {
                            if (completed) {
                                out.writeObject("Purchase successful");
                            } else {
                                out.writeObject("Purchase failed");
                            }
                            out.flush();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

            }else if(operation.equals("ROLLBACK_PURCHASE")){
                Store store = (Store)request.getObject();
                Customer customer = (Customer) request.getObject2();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if (master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                        boolean reverted = primary.rollbackPurchase(store.getStoreName(), customer.getUsername());
                        try {
                            if (reverted) {
                                out.writeObject("Revert successful");
                            } else {
                                out.writeObject("Revert unsuccessful");
                            }
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    replica.receiveTask(() -> {
                        replica.rollbackPurchase(store.getStoreName(), customer.getUsername());
                    });
                }else {
                    replica.receiveTask(() -> {
                        boolean reverted = replica.rollbackPurchase(store.getStoreName(), customer.getUsername());
                        try {
                            if (reverted) {
                                out.writeObject("Revert successful");
                            } else {
                                out.writeObject("Revert unsuccessful");
                            }
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }

            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private List<Thread> getThreads(List<Store> stores) {
        Set<String> storeNames = Collections.synchronizedSet(new HashSet<>());
        List<Thread> workerThreads = new ArrayList<>();
        for (Worker worker : workers) {
            Thread t = new Thread(() -> {
                List<Store> workerStores = worker.showAllStores();
                synchronized (stores) {
                    for (Store store : workerStores) {
                        if (storeNames.add(store.getStoreName())) {
                            stores.add(store);
                        }
                    }                        }
            });
            workerThreads.add(t);
            t.start();
        }
        return workerThreads;
    }


}
