package main;

import java.util.*;
import java.util.stream.Collectors;

import static main.Master.getWorkerIndicesForStore;

public class Worker extends Thread {
    private final int workerId;
    private List<Store> storeList = new ArrayList<>();
    private Runnable task = null;
    private Map<String, List<PendingPurchase>> pendingPurchases = new HashMap<>();
    private Queue<Runnable> pendingTasks = new LinkedList<>();
    private boolean isAlive = true;

    public Worker(int workerId) {
        this.workerId = workerId;
    }

    private boolean running = true;

    private static class PendingPurchase{
        /*
            Bohthitikh klash gia thn diadikasia paraggelias proiontwn
         */
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
            Runnable task = null;
            synchronized (this) {
                while(pendingTasks.isEmpty() && running) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (!running) break;
                task = pendingTasks.poll();
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

    public boolean ping() {
        return isAlive; // sends life signal :)
    }

    public void kill(){ isAlive = false; }


    public int getWorkerId() {
        return workerId;
    }

    public synchronized void receiveTask(Runnable task) {
        pendingTasks.add(task);
        notify();
    }

    public List<Store> getAllStores() {
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
        /*
            Prosthikh katasthmatow sthn lista
         */
        if(store!=null && !storeList.contains(store)) {
            storeList.add(store);
            store.calculatePriceCategory();
            return true;
        }
        return false;
    }

    public boolean hasStore(String storeName) {
        /*
            Elegxos uparkshs katasthmatos sthn lista
         */
        for(Store store : storeList){
            if(store.getStoreName().equals(storeName)){
                return true;
            }
        }
        return false;
    }

    public boolean hasProduct(String storeName, String productName) {
        /*
            Elegxos uparkshs proiontos sthn lista
         */
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


    public synchronized boolean reserveProduct(Store store, Product product,Customer customer, int quantity) {
        /*
            Proswrinh krathsh proiontos mextri na oloklhrwthei/akirothei paraggelia
         */
        Store currStore = getStore(store.getStoreName());
        synchronized(currStore){    //Kleidwma store gia na mhn mporei na ginei modifies tautoxrona apo allou
            for(Product p : currStore.getProducts()){
                if(p.getProductName().equals(product.getProductName())){
                    if(p.getAvailableAmount() >= quantity && p.getAvailableAmount() > 0){
                        p.setAvailableAmount(p.getAvailableAmount() - quantity);
                        pendingPurchases        //Prosthese to proion sthn ekkremh paraggelia me unique ID storeName(koino) mazi me username pelath(pou einai monadiko)
                                .computeIfAbsent(store.getStoreName() + customer.getUsername(), k -> new ArrayList<>())
                                .add(new PendingPurchase(product.getProductName(), quantity));
                        return true;
                    }

                }
            }
        }
        return false;
    }

    public synchronized boolean rollbackPurchase(String storeName, String username) {
        /*
            Diadikasia epanaforas katasthmatos sthn arxikh tou katastash meta
            apo akirwsh paraggelias
         */
        Store store = getStore(storeName);
        if(store == null || !pendingPurchases.containsKey(storeName)) {
            return false;
        }
        synchronized (store){   //Kleidwma store
            List<PendingPurchase> pending = pendingPurchases.get(storeName+username);
            for (PendingPurchase pp : pending) {
                for (Product p : store.getProducts()) {
                    if (p.getProductName().equals(pp.productName)) {
                        p.setAvailableAmount(p.getAvailableAmount() + pp.quantity); //Prosthesh quantity pou eixe desmeuthei prosorina apo reservation
                        if (p.getAvailableAmount() > 0 && !p.isOnline()) {
                            p.setOnline(true);   //Epanefere ta proionta pou eixan ksemeinei apo stock online
                        }
                    }
                }
            }

            pendingPurchases.remove(storeName+username); //Afairesh apo ekkremh paraggelia
            return true;
        }
    }

    public synchronized void syncStore(Store primaryStore) {
        Store replicaStore = getStore(primaryStore.getStoreName());
        if (replicaStore == null) return;

        synchronized (replicaStore) {
            for (Product primaryProduct : primaryStore.getProducts()) {
                Product replicaProduct = replicaStore.getProduct(primaryProduct.getProductName());
                if (replicaProduct != null) {
                    replicaProduct.setAvailableAmount(primaryProduct.getAvailableAmount());
                    replicaProduct.setTotalSales(primaryProduct.getTotalSales());
                    replicaProduct.setOnline(primaryProduct.isOnline());
                }
            }

            replicaStore.setStars(primaryStore.getStars());
            replicaStore.calculatePriceCategory();
        }
    }

    public synchronized boolean completePurchase(String storeName, String username) {
        Store store = getStore(storeName);
        if(store == null || !pendingPurchases.containsKey(storeName+username)) {
            return false;
        }
        synchronized (store) {
            List<PendingPurchase> pending = pendingPurchases.get(storeName+username);
            for (PendingPurchase pp : pending) {
                for (Product p : store.getProducts()) {
                    System.out.println(p.getProductName() + " " + p.getAvailableAmount());
                    if (p.getProductName().equals(pp.productName)) {
                        p.addSales(pp.quantity);
                            if (p.getAvailableAmount() == 0) {
                                p.setOnline(false);
                            }
                    }
                }
            }

            pendingPurchases.remove(storeName+username);
            return true;
        }
    }


    public synchronized void modifyStock(Store store, Product product, int quantity) {
        for(Store s: storeList){
            if(s.equals(store)){
                for(Product p : s.getProducts()){
                    if(p.equals(product)){
                        p.setAvailableAmount(quantity);
                        System.out.println("Modified stock: " + p.getProductName() + " " + p.getAvailableAmount());
                    }
                }
            }
        }
    }

    public boolean addProduct(Store store, Product product) {
        for(Store s : storeList){
            if(s.equals(store)){
                synchronized (s){
                    for(Product p : s.getProducts()){
                        if(p.getProductName().equals(product.getProductName())){
                            if(!p.isOnline()){
                                p.setOnline(true);
                            }
                            s.calculatePriceCategory();
                            return true;
                        }
                    }
                    s.getProducts().add(product);
                }
            }
        }
        return false;
    }

    public synchronized String reactivateProduct(Store store, Product product) {
        Store localStore = getStore(store.getStoreName());
        if (localStore == null) return "Store not found";

        for (Product p : localStore.getProducts()) {
            if (p.getProductName().equals(product.getProductName())) {
                p.setOnline(true);
                p.setAvailableAmount(product.getAvailableAmount());
                return "Reactivated product: " + p.getProductName();
            }
        }
        return "Product not found";
    }

    public boolean removeProduct(Store store, Product product) {
        for(Store s : storeList){
            if(s.equals(store)){
                synchronized (s){
                    for(Product p: s.getProducts()){
                        if(p.getProductName().equals(product.getProductName())){
                            p.setOnline(false);
                            s.calculatePriceCategory();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public synchronized List<Product> getOfflineProducts(Store store) {
        Store localStore = getStore(store.getStoreName());
        if (localStore == null) return Collections.emptyList();

        return localStore.getProducts().stream()
                .filter(p -> !p.isOnline())
                .collect(Collectors.toList());
    }

    public Store getStore(String storeName) {
        for(Store store : storeList){
            if(store.getStoreName().equals(storeName)){
                return store;
            }
        }
        return null;
    }

    public List<Store> mapFilterStores(String category, double minRate, double maxRate, String priceCat, List<Worker> workers) {
        return storeList.stream()
                .filter(store -> shouldIncludeStore(this, workers, store.getStoreName()))
                .filter(store -> matchesFilter(store, category, minRate, maxRate, priceCat))
                .collect(Collectors.toList());
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
                synchronized (s){
                    s.applyRating(rating);
                    System.out.println("Rate stored " + s.getStars());
                    return;
                }
            }
        }
    }

    public List<Store> showAllStores(){return new ArrayList<>(storeList);
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

    public boolean shouldIncludeStore(Worker worker,List<Worker> workers, String storeName) {
        List<Integer> indices = getWorkerIndicesForStore(storeName, workers.size());
        Worker primary = workers.get(indices.get(0));

        return worker.getWorkerId() == primary.getWorkerId() ||
                (!primary.isAlive() && worker.getWorkerId() == indices.get(1));
    }

    public List<AbstractMap.SimpleEntry<String,Integer>> mapProductSales(String storeName, List<Worker> workers){
        return storeList.stream()
                .filter(store -> store.getStoreName().equals(storeName))
                .filter(store -> shouldIncludeStore(this, workers, store.getStoreName()))  // Only keep matching store
                .flatMap(store -> store.getProducts().stream()
                        .map(p -> new AbstractMap.SimpleEntry<>(
                                p.getProductName(),
                                p.getTotalSales()  // Emit actual sales numbers
                        ))
                )
                .collect(Collectors.toList());
    }



    public List<AbstractMap.SimpleEntry<String, Integer>> mapProductCategorySales(List<Worker> workers) {
        return storeList.stream().
                filter(store -> shouldIncludeStore(this, workers,store.getStoreName())).
                flatMap(store -> store.getProducts().stream()
                .map(p -> new AbstractMap.SimpleEntry<>(
                        p.getProductType(),p.getTotalSales()
                )))
                .collect(Collectors.toList());
    }

    public List<AbstractMap.SimpleEntry<String,Integer>> mapShopCategorySales(List<Worker> workers){
        return storeList.stream().
                filter(store -> shouldIncludeStore(this, workers, store.getStoreName()))
                .flatMap(store->store.getProducts().stream().
                map(p -> new AbstractMap.SimpleEntry<>(
                        store.getFoodCategory(),
                        p.getTotalSales()
                ))).collect(Collectors.toList());
    }

}

