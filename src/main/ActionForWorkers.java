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
                while (true) { //trexe sinexeia wspou na labeis request
                    WorkerFunctions request = (WorkerFunctions) in.readObject(); //lhpsh request
                    processRequest(request);  //epeksergasia request
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

    public void processRequest(WorkerFunctions request) {
        /*
            Dexetai ena WorkerFunctions antikeimeno kai afou diabazei to operation kanei tis
            antistoixes energeies kai anathetei thn diadikasia se enan worker ston opoio kanei hash gia thn epilogh tou
         */
        try{
            String operation = request.getOperation();
            if(operation.equals("ADD_STORE")) {    //Gia methodo addStore tou manager
                Store store = (Store) request.getObject();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if (master.isAlive(primary)) {   //An einai zontanos o primary
                    primary.receiveTask(() -> {
                        boolean added = primary.addStore(store);
                        try {
                            if (added) {
                                out.writeObject(store);
                                replica.receiveTask(() -> {     //Oti allagh se store tou primary worker ginetai sync me auto tou replica
                                    replica.syncStore(primary.getStore(store.getStoreName()));   //gia na exoun idia dedomena
                                });
                            } else {
                                out.writeObject("Store is already registered");
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {    //An den einai zontanos o primary anti gia sync apo primary o replica lambanei kateutheian to task
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
            }else if (operation.equals("GET_OFFLINE_PRODUCTS")) {
                Store store = (Store) request.getObject();
                Worker primary = workers.get(Master.getWorkerIndicesForStore(store.getStoreName(), workers.size()).get(0));
                List<Product> offlineProducts = primary.getOfflineProducts(store);
                out.writeObject(offlineProducts);
            }else if (operation.equals("REACTIVATE_PRODUCT")) {
                Store store = (Store) request.getObject();
                Product product = (Product) request.getObject2();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());

                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));

                if (master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                        String result = primary.reactivateProduct(store, product);
                        try {
                            out.writeObject(result);
                            replica.receiveTask(() -> {
                                replica.syncStore(primary.getStore(store.getStoreName()));
                            });
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    replica.receiveTask(() -> {
                        String result = replica.reactivateProduct(store, product);
                        try {
                            out.writeObject(result);
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }else if(operation.equals("ADD_PRODUCT")) {  //gia methodo addProductToStore tou manager
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
                                replica.receiveTask(() -> {
                                    replica.syncStore(primary.getStore(store.getStoreName()));
                                });
                            }else{
                                out.writeObject("Product " + product.getProductName() + " was registered successfully");
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
            } else if(operation.equals("REMOVE_PRODUCT")) {  //gia methodo removeProductFromStore tou manager
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
                                replica.receiveTask(() -> {
                                    replica.syncStore(primary.getStore(store.getStoreName()));
                                });
                            }else{
                                out.writeObject("Product is already offline");
                            }
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
            }else if(operation.equals("MODIFY_STOCK")) {  //gia mrethodo modifyAvailability tou manager
                Store store = (Store) request.getObject();
                Product product = (Product) request.getObject2();
                int quantity = request.getNum();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if(master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                    primary.modifyStock(store, product, quantity);
                        replica.receiveTask(() -> {
                            replica.syncStore(primary.getStore(store.getStoreName()));
                        });
                    try {
                        out.writeObject(store.getStoreName());
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    });
                }else{
                    replica.receiveTask(() -> {
                        replica.modifyStock(store, product, quantity);
                        try{
                            out.writeObject(store.getStoreName());
                            out.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                };
            }else if(operation.equals("SHOW_STORES")) {  //gia methodo showNearbyStores tou customer
                Customer customer = (Customer)request.getObject();
                List<Store> stores = new ArrayList<Store>();
                List<Thread> workerThreads = getThreads(stores,customer);
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
            }else if(operation.equals("SHOW_ALL_STORES")) { //xrhsimopoieitai se polles methodous, deixnei ola ta stores ths efarmoghs
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
            }else if (operation.equals("FILTER_STORES")) {  //gia th methodo filterStores tou customer
                String foodCategory = request.getName();
                double lowerStars = request.getDouble1();
                double upperStars = request.getDouble2();
                String priceCategory = request.getName2();

                List<Store> results = master.filterStores(foodCategory, lowerStars, upperStars, priceCategory);
                out.writeObject(results);
            }else if(operation.equals("APPLY_RATING")){ //gia th methodo rateStore tou customer
                Store store = (Store)request.getObject();
                int rating = request.getNum();
                List<Integer> assign = Master.getWorkerIndicesForStore(store.getStoreName(), workers.size());
                Worker primary = workers.get(assign.get(0));
                Worker replica = workers.get(assign.get(1));
                if (master.isAlive(primary)) {
                    primary.receiveTask(() -> {
                        primary.rateStore(store, rating);
                        replica.receiveTask(() -> {
                            replica.syncStore(primary.getStore(store.getStoreName()));
                        });
                        try {
                            out.writeObject("Successful");
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
                // Oi leitourgies pou xrhsimopoioun map/reduce anatithentai kateutheian ston master
            }else if (operation.equals("PRODUCT_SALES")) {  // manager/salesPerProduct case 1
                String storeName = (String) request.getName();
                Map<String, Integer> results = this.master.reduceProductSales(storeName);
                out.writeObject(results);
            }else if (operation.equals("PRODUCT_CATEGORY_SALES")) { //manager/salesPerProduct case 2
                String productCategory = request.getName();
                Map<String, Integer> results = this.master.reduceProductCategorySales(productCategory);
                out.writeObject(results);
            }else if (operation.equals("SHOP_CATEGORY_SALES")) { //manager/salesPerProduct case 3
                String shopCategory = request.getName();
                Map<String, Integer> results = this.master.reduceShopCategorySales(shopCategory);
                out.writeObject(results);
            }else if(operation.equals("RESERVE_PRODUCT")){  //customer/buyProducts krathsh proiontos
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
                                replica.receiveTask(() -> {
                                    replica.syncStore(primary.getStore(store.getStoreName()));
                                });
                            } else {
                                out.writeObject("Reservation failed");
                            }
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
            }else if(operation.equals("COMPLETE_PURCHASE")){ //customer/buyProducts oloklhrwsh paraggelias
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
                                replica.receiveTask(() -> {
                                    replica.syncStore(primary.getStore(store.getStoreName()));
                                });
                            } else {
                                out.writeObject("Purchase failed");
                            }
                            out.flush();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
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

            }else if(operation.equals("ROLLBACK_PURCHASE")){  //customer/buyProducts roolback paraggelias
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
                                replica.receiveTask(() -> {
                                    replica.syncStore(primary.getStore(store.getStoreName()));
                                });
                            } else {
                                out.writeObject("Revert unsuccessful");
                            }
                            out.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
        /*
            Epistrefei me synchronized tropo ta threads pou periexoun ola ta katasthmata,
            xrhsimopoieitai gia methodous pou xreiazetai na emfanistoun ola ta katasthmata
         */
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

    private List<Thread> getThreads(List<Store> stores, Customer customer) {
        /*
        Idia methodos me allh upografh apo thn apo allh getThread gia na mporei na epistrefei
        ta katasthmata se aktina 5km apo ton pelath anti gia ola ta katasthmata ths efarmoghs
         */
        Set<String> storeNames = Collections.synchronizedSet(new HashSet<>());
        List<Thread> workerThreads = new ArrayList<>();
        for (Worker worker : workers) {
            Thread t = new Thread(() -> {
                List<Store> workerStores = worker.showStores(customer);
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
