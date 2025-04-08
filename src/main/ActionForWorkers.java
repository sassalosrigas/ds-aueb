package main;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ActionForWorkers extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    private final List<Worker> workers;

    public ActionForWorkers(Socket connection, List<Worker> workers) {
        this.workers = workers;
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
                    worker.addStore(store);
                    try {
                        out.writeObject(store);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }else if(operation.equals("ADD_PRODUCT")) {
                System.out.println("product");
                Product product = (Product)request.getObject();
                String storeName = request.getName();
                System.out.println("Current store " + storeName);
                int assign = Master.hashToWorker(storeName, workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    worker.addProduct(storeName, product);
                    try {
                        out.writeObject(product);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else if(operation.equals("REMOVE_PRODUCT")) {
                String storeName = request.getName();
                System.out.println("Current store " + storeName);
                String productName = request.getName2();
                int assign = Master.hashToWorker(storeName, workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    worker.removeProduct(storeName, productName);
                    try {
                        out.writeObject(storeName);
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
                int quantity = (Integer) request.getNum();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                worker.receiveTask(() -> {
                    worker.buyProduct(store, product, quantity);
                    try{
                        out.writeObject(store);
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
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
