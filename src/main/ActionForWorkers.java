package main;

import java.io.*;
import java.net.*;
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
            WorkerFunctions request = (WorkerFunctions)in.readObject();
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
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
