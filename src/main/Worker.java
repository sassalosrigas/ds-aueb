package main;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Worker {
    private final int workerId;
    static List<Store> storeList = new ArrayList<>();

    public Worker(int workerId) {
        this.workerId = workerId;
    }

    public String proccessRequest(String request) {
        System.out.println("Worker");
        return "";
    }

    public List<Store> getStores() {
        return storeList;
    }

    public synchronized boolean addStore(Store store) {
        if(store!=null){
            storeList.add(store);
            return true;
        }
        return false;
    }

    public boolean hasStore(String storeName) {
        for(Store store : storeList){
            if(store.getStoreName().equals(storeName)){
                return true;
            }
        }
        return false;
    }

    public synchronized void addProduct(Store store, Product product) {
        store.getProducts().add(product);
    }


    public Store getStore(String storeName) {
        for(Store store : storeList){
            if(store.getStoreName().equals(storeName)){
                return store;
            }
        }
        return null;
    }
}
