package main;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Worker extends Thread {
    private final int workerId;
    private List<Store> storeList = new ArrayList<>();
    private Runnable task = null;
    public Worker(int workerId) {
        this.workerId = workerId;
    }


    public List<Store> getStores() {
        return storeList;
    }

    @Override
    public void run() {
        while(true) {
            synchronized (this) {
                while(task == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                task.run();
                task = null;
            }
        }
    }

    public synchronized void receiveTask(Runnable task) {
        this.task = task;
        notify();
    }

    public synchronized void addStore(Store store) {
        if(store!=null && !storeList.contains(store)) {
            storeList.add(store);
            store.calculatePriceCategory();
        }
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
    public List<Store> filterStores(String category, double lower, double upper, String price) {
        List<Store> stores = new ArrayList<>();
        System.out.println(category);
        System.out.println(lower);
        System.out.println(upper);
        System.out.println(price);
        for(Store store : storeList){
            if(store.getFoodCategory().equals(category) && store.getStars()>= lower && store.getStars()<= upper && store.getPriceCategory().equals(price)){
                stores.add(store);
            }
        }
        return stores;
    }

    public List<Store> showStores(Customer customer){
        List<Store> stores = new ArrayList<>();
        for(Store store : storeList){
            if(isWithInRange(store, customer)){
                stores.add(store);
            }
        }
        return stores;
    }

    public boolean isWithInRange(Store store, Customer customer) {
        double storeLat = store.getLatitude();
        double storeLong = store.getLongitude();
        double customerLat = customer.getLatitude();
        double customerLong = customer.getLongitude();

        final double R = 6371.0; // Earth's radius in kilometers

        double lat1 = Math.toRadians(customerLat);
        double lon1 = Math.toRadians(customerLong);
        double lat2 = Math.toRadians(storeLat);
        double lon2 = Math.toRadians(storeLong);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;

        System.out.println("Distance: " + distance + " km");

        return distance <= 5.0;
    }
}
