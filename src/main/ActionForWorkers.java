package main;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionForWorkers extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    private final List<Worker> workers;
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

    public void processRequest(WorkerFunctions request) {
        try{
            String operation = request.getOperation();
            if(operation.equals("ADD_STORE")) {
                Store store = (Store)request.getObject();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    boolean added = worker.addStore(store);
                    try {
                        if(added){
                            out.writeObject(store);
                        }else{
                            out.writeObject("Store is already registered");
                        }
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }else if(operation.equals("ADD_PRODUCT")) {
                System.out.println("product");
                Store store = (Store) request.getObject();
                Product product = (Product)request.getObject2();
                System.out.println("Current store " + store.getStoreName());
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    boolean existed = worker.addProduct(store, product);
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
            } else if(operation.equals("REMOVE_PRODUCT")) {
                Store store = (Store) request.getObject();
                Product product = (Product) request.getObject2();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    boolean removed = worker.removeProduct(store, product);
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
            }else if(operation.equals("MODIFY_STOCK")) {
                String storeName = request.getName();
                String productName = request.getName2();
                int quantity = request.getNum();
                int assign = Master.hashToWorker(storeName, workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    worker.modifyStock(storeName, productName, quantity);
                    try {
                        out.writeObject(storeName);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
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
            }else if(operation.equals("SEARCH_STORES")) {
                String foodCategory = request.getName();
                double lowerStars = request.getDouble1();
                double upperStars = request.getDouble2();
                String priceCategory = request.getName2();
                List<Store> stores = new ArrayList<Store>();
                List<Thread> workerThreads = new ArrayList<>();
                for(Worker worker: workers) {
                    Thread t = new Thread(() -> {
                        List<Store> workerStores = worker.filterStores(foodCategory, lowerStars, upperStars, priceCategory);
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
            }else if(operation.equals("BUY_PRODUCT")) {
                Product product = (Product)request.getObject();
                Store store = (Store)request.getObject2();
                int quantity = request.getNum();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    boolean completed = worker.buyProduct(store, product, quantity);
                    try{
                        if(completed){
                            out.writeObject(store);
                        }else{
                            out.writeObject("There isn't enough available quantity");
                        }
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }else if(operation.equals("SHOW_ALL_STORES")) {
                List<Store> stores = new ArrayList<Store>();
                List<Thread> workerThreads = new ArrayList<>();
                for(Worker worker: workers) {
                    Thread t = new Thread(() -> {
                        synchronized(stores) {
                            stores.addAll(worker.showAllStores());
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
            }else if(operation.equals("APPLY_RATING")){
                Store store = (Store)request.getObject();
                int rating = request.getNum();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    worker.rateStore(store, rating);
                    try{
                        out.writeObject("Successful");
                        out.flush();
                    }catch(IOException e){
                        throw new RuntimeException(e);
                    }
                });
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
                int quantity = request.getNum();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    boolean reserved = worker.reserveProduct(store, product, quantity);
                    try{
                        if(reserved){
                            out.writeObject("Reserved successfully");
                        }else{
                            out.writeObject("Reservation failed");
                        }
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }else if(operation.equals("COMPLETE_PURCHASE")){
                Store store = (Store)request.getObject();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    boolean completed = worker.commitPurchase(store.getStoreName());
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
            }else if(operation.equals("ROLLBACK_PURCHASE")){
                Store store = (Store)request.getObject();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    boolean reverted = worker.rollbackPurchase(store.getStoreName());
                    try {
                        if(reverted){
                            out.writeObject("Revert successful");
                        }else{
                            out.writeObject("Revert unsuccessful");
                        }
                        out.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
