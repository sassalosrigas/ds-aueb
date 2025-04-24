package main;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class Manager{


    public static void addStore(Scanner input) {
        /*
            Diabazei ena store mesw JSON arxeiou kai to arxikopoiei
         */
        try {
            System.out.println("Please provide the file with the data: ");
            String filepath = input.nextLine();
            Store newStore = JsonHandler.readStoreFromJson(filepath);
            newStore.setFilepath(filepath);
            try{
                Socket socket = new Socket("localhost", 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                out.writeObject(new WorkerFunctions("ADD_STORE",newStore));
                out.flush();
                Object response = in.readObject();
                if(response instanceof Store){
                    System.out.println("Store " + newStore.getStoreName() + " has been added successfully");
                }else{
                    System.out.println(response);
                }
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid input. Please enter the correct data type.");
        } catch (java.util.NoSuchElementException e) {
            System.out.println("No input found. Please provide all required inputs.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    public static Product addProduct(Scanner input){
        /*
            Diadikasia eisodou kai dhmiourgias proiontos
         */
        try{
            System.out.println("Give product name:");
            String productName = input.nextLine();
            System.out.println("Give product type:");
            String prodType = input.nextLine();
            System.out.println("Give product price:");
            int price = input.nextInt();
            System.out.println("Give available amount:");
            int amount = input.nextInt();
            return new Product(productName, prodType, price, amount);

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeProductFromStore(Scanner input){
        /*
            Epilogh proiontos apo uparxonta kai thesimo tou ws offline
         */
        try{
            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
            out.flush();
            Object response = in.readObject();
            if(response instanceof ArrayList){
                ArrayList<Store> stores = (ArrayList<Store>) response;
                System.out.println("Choose store to remove product from: ");
                for(int i=0;i<stores.size();i++){
                    System.out.println(i+1 + ". " + stores.get(i).getStoreName());
                }
                System.out.println("0. Exit");
                int choice = input.nextInt();
                if(choice >= 1 && choice <= stores.size()){
                    Store store = stores.get(choice-1);
                    System.out.println("Choose product to be removed: ");
                    for(int i=0;i<store.getProducts().size();i++){
                        System.out.println(i+1 + ". " + store.getProducts().get(i).getProductName() + ": " + (store.getProducts().get(i).isOnline() ? "Online" : "Offline") );
                    }
                    System.out.println("0. Exit");
                    choice = input.nextInt();
                    if(choice >= 1 && choice <= store.getProducts().size()){
                        Product product = store.getProducts().get(choice-1);
                        out.writeObject(new WorkerFunctions("REMOVE_PRODUCT",store, product));
                        out.flush();
                        Object response2 = in.readObject();
                        if(response2 instanceof String){
                            System.out.println("Server response: " + response2);
                        }
                    }else if(choice != 0){
                        System.out.println("Invalid input");
                    }
                }else if(choice != 0){
                    System.out.println("Invalid input");
                }
                out.close();
                in.close();
                socket.close();
            }
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }


    public static void addProductToStore(Scanner input) {
        try {
            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            // Get all stores
            out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
            out.flush();
            Object response = in.readObject();

            if (response instanceof ArrayList) {
                ArrayList<Store> stores = (ArrayList<Store>) response;

                // Store selection
                System.out.println("Choose store to add product to: ");
                for (int i = 0; i < stores.size(); i++) {
                    System.out.println(i + 1 + ". " + stores.get(i).getStoreName());
                }
                System.out.println("0. Exit");
                int storeChoice = input.nextInt();
                input.nextLine(); // Consume newline

                if (storeChoice >= 1 && storeChoice <= stores.size()) {
                    Store store = stores.get(storeChoice - 1);

                    // Get offline products
                    out.writeObject(new WorkerFunctions("GET_OFFLINE_PRODUCTS", store));
                    out.flush();
                    Object offlineResponse = in.readObject();

                    if (offlineResponse instanceof List) {
                        List<Product> offlineProducts = (List<Product>) offlineResponse;

                        if (!offlineProducts.isEmpty()) {
                            System.out.println("\nChoose offline product to reactivate (0 to add new):");
                            for (int i = 0; i < offlineProducts.size(); i++) {
                                Product p = offlineProducts.get(i);
                                System.out.printf("%d. %s (%s)\n",
                                        i + 1, p.getProductName(), p.getProductType());
                            }

                            int productChoice = input.nextInt();
                            input.nextLine(); // Consume newline

                            if (productChoice > 0 && productChoice <= offlineProducts.size()) {
                                // Reactivate existing product
                                Product toReactivate = offlineProducts.get(productChoice - 1);
                                toReactivate.setOnline(true);

                                // Get updated stock
                                System.out.println("Enter new available amount:");
                                int newAmount = input.nextInt();
                                input.nextLine(); // Consume newline
                                toReactivate.setAvailableAmount(newAmount);

                                out.writeObject(new WorkerFunctions("REACTIVATE_PRODUCT", store, toReactivate));
                                out.flush();
                                Object reactivationResponse = in.readObject();
                                System.out.println("Product reactivated: " + reactivationResponse);
                                return;
                            } else if (productChoice != 0) {
                                System.out.println("Invalid choice, creating new product");
                            }
                        }
                    }

                    // Create new product
                    System.out.println("\nCreating new product for " + store.getStoreName());
                    Product product = addProduct(input);
                    out.writeObject(new WorkerFunctions("ADD_PRODUCT", store, product));
                    out.flush();
                    Object response2 = in.readObject();
                    System.out.println("Server response: " + response2);
                }
            }

            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void salesPerProduct(Scanner input){
        try {
            System.out.println("Choose Report Type");
            System.out.println("1. Sales per product in a store");
            System.out.println("2. Sales by product category");
            System.out.println("3. Sales by shop category");
            int choice = input.nextInt();
            input.nextLine();

            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.flush();

            switch (choice) {
                case 1:
                    //System.out.println("Enter store name:");
                    //String storeName = input.nextLine();
                    out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
                    out.flush();
                    Object response = in.readObject();
                    if(response instanceof ArrayList){
                        ArrayList<Store> stores = (ArrayList<Store>) response;
                        System.out.println("Choose store to see sales from: ");
                        int counter = 0;
                        for(Store store : stores){
                            System.out.println(++counter + ". " + store.getStoreName());
                        }
                        System.out.println("0. Exit");
                        choice = input.nextInt();
                        if(choice >= 1 && choice <= stores.size()){
                            Store store = stores.get(choice-1);
                            out.writeObject(new WorkerFunctions("PRODUCT_SALES", store.getStoreName()));
                            out.flush();
                            Map<String, Integer> results = (Map<String, Integer>) in.readObject();
                            System.out.println("Sales for " +store.getStoreName()+": ");
                            results.forEach((product,sales)->
                                    System.out.printf("%-20s: %d%n", product, sales));
                        }else if(choice != 0){
                            System.out.println("Invalid input");
                        }
                        break;
                    }
                case 2:
                    out.writeObject(new WorkerFunctions("PRODUCT_CATEGORY_SALES"));
                    out.flush();
                    Map<String, Integer> productCatRes = (Map<String, Integer>) in.readObject();
                    System.out.println("Sales by Product Category: ");
                    productCatRes.forEach((category,sales)->
                            System.out.printf("%-20s: %d%n", category, sales));
                    System.out.println("Total: "+ productCatRes.values().stream().mapToInt(Integer::intValue).sum());
                    break;
                case 3:
                    out.writeObject(new WorkerFunctions("SHOP_CATEGORY_SALES"));
                    out.flush();
                    Map<String, Integer> shopCatRes = (Map<String, Integer>) in.readObject();
                    System.out.println("Sales by Shop Category: ");
                    shopCatRes.forEach((category,sales)->
                            System.out.printf("%-20s: %d%n", category, sales));
                    System.out.println("Total: "+ shopCatRes.values().stream().mapToInt(Integer::intValue).sum());
                    break;
            }

            out.close();
            in.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void modifyAvailability(Scanner input){
        /*
            Allagh tou diathesimou stock enos proiontos
         */
        try{
            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
            out.flush();
            Object response = in.readObject();
            if(response instanceof ArrayList){
                ArrayList<Store> stores = (ArrayList<Store>) response;
                System.out.println("Choose store: ");
                for(int i = 0;i<stores.size();i++){
                    System.out.println(i+1+ ". " + stores.get(i).getStoreName());
                }
                System.out.println("0 Exit");
                int choice = input.nextInt();
                if(choice >= 1 && choice <= stores.size()){
                    Store store = stores.get(choice-1);
                    System.out.println("Choose product to modify quantity: ");
                    int counter = 0;
                    for(Product p: store.getProducts()){
                        System.out.println(++counter + ": " + p.getProductName() + " Current quantity: " + p.getAvailableAmount());
                    }
                    choice = input.nextInt();
                    if(choice >= 1 && choice <= store.getProducts().size()){
                        Product product = store.getProducts().get(choice-1);
                        System.out.println("Give new quantity:");
                        int quantity = input.nextInt();
                        out.writeObject(new WorkerFunctions("MODIFY_STOCK",store, product, quantity));
                        out.flush();
                        response = in.readObject();
                        if(response instanceof Store){
                            System.out.println("Server response: " + ((Store) response).getStoreName());
                        }
                    }
                }else if(choice!=0){
                    System.out.println("Invalid input");
                }
                out.close();
                in.close();
                socket.close();

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void avgPrice(){
        try{
            List<Store> stores = JsonHandler.readStoresFromJson("store.json");
            int totalPrice = 0;
            for(Store s: stores){
                for(Product p: s.getProducts()){
                    totalPrice += p.getPrice();
                }
            }
            System.out.println("Average price: " + totalPrice/stores.size());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}