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

    public void run() {
        try{
            WorkerFunctions request = (WorkerFunctions)in.readObject();
            String operation = request.getOperation();
            if(operation.equals("ADD_STORE")) {
                Store store = (Store)request.getObject();
                int assign = Master.hashToWorker(store.getStoreName(), workers.size());
                Worker worker = workers.get(assign);
                boolean result = worker.addStore(store);
                if(result){
                    System.out.println("Stored successfully at #worker " + assign);
                }else{
                    System.out.println("Failed to add store at #worker " + assign);
                }
                out.writeObject(store);
                out.flush();
            }else if(operation.equals("ADD_PRODUCT")) {
                System.out.println("product");
                Product product = (Product)request.getObject();
                String storeName = request.getName();
                System.out.println("Current store " + storeName);
                int assign = Master.hashToWorker(storeName, workers.size());
                Worker worker = workers.get(assign);
                worker.addProduct(storeName, product);
                out.writeObject(product);
                out.flush();
            } else if(operation.equals("REMOVE_PRODUCT")) {
                String storeName = request.getName();
                System.out.println("Current store " + storeName);
                String productName = request.getName2();
                int assign = Master.hashToWorker(storeName, workers.size());
                Worker worker = workers.get(assign);
                worker.removeProduct(storeName, productName);
                out.writeObject(storeName);
                out.flush();
            }else if(operation.equals("MODIFY_STOCK")) {
                String storeName = request.getName();
                String productName = request.getName2();
                int quantity = request.getNum();
                int assign = Master.hashToWorker(storeName, workers.size());
                Worker worker = workers.get(assign);
                worker.modifyStock(storeName, productName, quantity);
                out.writeObject(storeName);
                out.flush();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
