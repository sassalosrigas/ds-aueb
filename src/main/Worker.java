package main;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Worker extends Thread {
    private final int workerId;
    static List<Store> storeList = new ArrayList<>();

    public Worker(int workerId) {
        this.workerId = workerId;
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

    public boolean hasProduct(String storeName, String productName) {
        Store store = getStore(storeName);
        if(store!=null){
            for(Product product : store.getProducts()){
                if(product.getProductName().equals(productName)){
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void modifyStock(String storeName, String productName, int quantity) {
        if(hasStore(storeName) && hasProduct(storeName, productName)){
            Store store = getStore(storeName);
            for(Product product : store.getProducts()){
                if(product.getProductName().equals(productName)){
                    product.availableAmount = quantity;
                }
            }
            JsonHandler.writeStoreToJson(store, store.getFilepath());
            System.out.println("Changed product " + productName + " from store " + store.getStoreName());
        }else{
            System.out.println("Product does not exist");
        }
    }

    public synchronized void addProduct(String storeName, Product product) {
        if(hasStore(storeName)){
            Store store = getStore(storeName);
            store.getProducts().add(product);
            JsonHandler.writeStoreToJson(store, store.getFilepath());
            System.out.println("Added product " + product.getProductName() + " to store " + store.getStoreName());
        }else{
            System.out.println("Store does not exist");
        }
    }

    public synchronized void removeProduct(String storeName, String productName) {
        if(hasStore(storeName) && hasProduct(storeName, productName)){
            Store store = getStore(storeName);
            store.getProducts().removeIf(p -> p.getProductName().equals(productName));
            JsonHandler.writeStoreToJson(store, store.getFilepath());
            System.out.println("Removed product " + productName + " from store " + store.getStoreName());
        }else{
            System.out.println("Product does not exist");
        }
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
