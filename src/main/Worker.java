package main;

import java.io.*;
import java.net.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Worker extends Thread {
    private final int workerId;
    private List<Store> storeList = new ArrayList<>();
    private Runnable task = null;
    private final Map<String, List<PendingPurchase>> pendingPurchases = new ConcurrentHashMap<>();
    public Worker(int workerId) {
        this.workerId = workerId;
    }

    private boolean running = true;

    private static class PendingPurchase{
        String productName;
        int quantity;

        PendingPurchase(String productName, int quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }

    }
    @Override
    public void run() {
        while(running) {
            synchronized (this) {
                while(task == null && running) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (!running) break;
                try {
                    task.run();
                } catch (Exception e) {
                    System.err.println("Error running worker " + workerId);
                } finally {
                    task = null;
                }
            }
        }
    }
    public synchronized void receiveTask(Runnable task) {
        this.task = task;
        notify();
    }

    public synchronized List<Store> getAllStores() {
        return new ArrayList<>(storeList);
    }

    public synchronized void clearStores() {
        storeList.clear();
    }

    public synchronized void addStores(List<Store> stores) {
        storeList.addAll(stores);
        storeList.forEach(Store::calculatePriceCategory);
    }

    public void shutdown() {
        this.running = false;
        this.notifyAll();
    }

    public synchronized boolean addStore(Store store) {
        if(store!=null && !storeList.contains(store)) {
            storeList.add(store);
            store.calculatePriceCategory();
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

    public synchronized boolean buyProduct(Store store, Product product, int quantity) {
        for(Store s: storeList){
            if(s.getStoreName().equals(store.getStoreName())){
                for(Product p : s.getProducts()){
                    if(p.getProductName().equals(product.getProductName())){
                        synchronized(p){
                            if(p.getAvailableAmount() >= quantity){
                                p.setAvailableAmount(p.getAvailableAmount() - quantity);
                                p.addSales(quantity);
                                if (p.getAvailableAmount() == 0) {
                                    p.setOnline(false);
                                }
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public synchronized boolean reserveProduct(Store store, Product product, int quantity) {
        for(Product p : store.getProducts()){
            if(p.getProductName().equals(product.getProductName())){
                synchronized(p){
                    if(p.getAvailableAmount() >= quantity){
                        p.setAvailableAmount(p.getAvailableAmount() - quantity);
                        pendingPurchases
                                .computeIfAbsent(store.getStoreName(), k -> new ArrayList<>())
                                .add(new PendingPurchase(product.getProductName(), quantity));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public synchronized boolean rollbackPurchase(String storeName) {
        List<PendingPurchase> pending = pendingPurchases.get(storeName);
        if (pending == null) return false;

        Store store = getStore(storeName);
        if (store == null) return false;

        for (PendingPurchase pp : pending) {
            for (Product p : store.getProducts()) {
                if (p.getProductName().equals(pp.productName)) {
                    synchronized (p) {
                        p.setAvailableAmount(p.getAvailableAmount() + pp.quantity);
                        if (p.getAvailableAmount() > 0 && !p.isOnline()) {
                            p.setOnline(true);
                        }
                    }
                    break;
                }
            }
        }

        pendingPurchases.remove(storeName);
        return true;
    }

    public synchronized boolean commitPurchase(String storeName) {
        List<PendingPurchase> pending = pendingPurchases.get(storeName);
        if (pending == null) return false;

        Store store = getStore(storeName);
        if (store == null) return false;

        for (PendingPurchase pp : pending) {
            for (Product p : store.getProducts()) {
                if (p.getProductName().equals(pp.productName)) {
                    synchronized (p) {
                        p.addSales(pp.quantity);

                        if (p.getAvailableAmount() == 0) {
                            p.setOnline(false);
                        }
                    }
                    break;
                }
            }
        }

        pendingPurchases.remove(storeName);
        return true;
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

    public synchronized boolean addProduct(Store store, Product product) {
        for(Store s : storeList){
            if(s.equals(store)){
                for(Product p : s.getProducts()){
                    if(p.getProductName().equals(product.getProductName())){
                        if(!p.isOnline()){
                            p.setOnline(true);
                        }
                        return true;
                    }
                }
                s.getProducts().add(product);
            }
        }
        return false;
    }

    public synchronized boolean removeProduct(Store store, Product product) {
        for(Store s : storeList){
            if(s.equals(store)){
                for(Product p: s.getProducts()){
                    if(p.getProductName().equals(product.getProductName())){
                        p.setOnline(false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Store getStore(String storeName) {
        for(Store store : storeList){
            if(store.getStoreName().equals(storeName)){
                return store;
            }
        }
        return null;
    }

    public List<Store> mapFilterStores(String category, double minRate, double maxRate, String priceCat) {
            List<Store> results = new ArrayList<>();
            for(Store store : storeList){
                if(matchesFilter(store, category, minRate, maxRate, priceCat)) {
                    results.add(store);
                }
            }
            return results;
    }

    public boolean matchesFilter(Store store, String category, double minRate, double maxRate, String priceCat) {
        boolean result = true;
        if (category != null && !store.getFoodCategory().equalsIgnoreCase(category)) {
            return false;
        }
        if (store.getStars() < minRate || store.getStars() > maxRate) {
            return false;
        }
        if (priceCat != null && !store.getPriceCategory().equalsIgnoreCase(priceCat)) {
            return false;
        }
        return result;
    }



    public void rateStore(Store store, int rating){
        for(Store s : storeList){
            if(s.getStoreName().equals(store.getStoreName())){
                s.applyRating(rating);
                System.out.println("Rate stored " + s.getStars());
                return;
            }
        }
    }

    public synchronized List<Store> showAllStores(){
        return new ArrayList<>(storeList);
    }

    public synchronized List<Store> showStores(Customer customer){
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


    public List<AbstractMap.SimpleEntry<String, Integer>> mapProductSales(String storeName) {
        List<AbstractMap.SimpleEntry<String, Integer>> results = new ArrayList<>();
        Store store = getStore(storeName);
        if (store != null) {
            for (Product product : store.getProducts()) {
                results.add(new AbstractMap.SimpleEntry<>(
                product.getProductName(),
                product.getTotalSales()
                ));
            }
        }
        return results;
    }

    public List<AbstractMap.SimpleEntry<String, Integer>> mapProductCategorySales() {
        List<AbstractMap.SimpleEntry<String, Integer>> results = new ArrayList<>();
        for (Store store : storeList) {
            for (Product product : store.getProducts()) {
                results.add(new AbstractMap.SimpleEntry<>(
                        product.getProductType(),
                        product.getTotalSales()
                ));
            }
        }
        return results;
    }

    public List<AbstractMap.SimpleEntry<String, Integer>> mapShopCategorySales() {
        List<AbstractMap.SimpleEntry<String, Integer>> results = new ArrayList<>();
        for (Store store : storeList) {
            for (Product product : store.getProducts()) {
                results.add(new AbstractMap.SimpleEntry<>(
                        store.getFoodCategory(),
                        product.getTotalSales()
                ));
            }
        }
        return results;
    }
}

