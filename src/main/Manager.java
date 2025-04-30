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
                int counter = 0;
                for(Store store : stores){
                    System.out.println(++counter + ". " + store.getStoreName());
                }
                System.out.println("0. Exit");
                int choice = input.nextInt();
                if(choice >= 1 && choice <= stores.size()){
                    Store store = stores.get(choice-1);
                    System.out.println("Choose product to be removed: ");
                    counter = 0;
                    for(Product p: store.getProducts()){
                        System.out.println(++counter + ". " + p.getProductName() + ": " + (p.isOnline() ? "Online" : "Offline"));
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

            out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
            out.flush();
            Object response = in.readObject();

            if (response instanceof ArrayList) {
                ArrayList<Store> stores = (ArrayList<Store>) response;

                System.out.println("Choose store to add product to: ");
                int counter = 0;
                for(Store store : stores){
                    System.out.println(++counter + ". " + store.getStoreName());
                }
                System.out.println("0. Exit");
                int storeChoice = input.nextInt();
                input.nextLine();

                if (storeChoice >= 1 && storeChoice <= stores.size()) {
                    Store store = stores.get(storeChoice - 1);

                    /* emfanise kai ta anenerga proionta se periptwsh pou o manager thelei na
                    ta kanei pali energa anti na kanei input neo antikeimeno
                     */
                    out.writeObject(new WorkerFunctions("GET_OFFLINE_PRODUCTS", store));
                    out.flush();
                    Object offlineResponse = in.readObject();

                    if (offlineResponse instanceof List) {
                        List<Product> offlineProducts = (List<Product>) offlineResponse;

                        if (!offlineProducts.isEmpty()) {  //periptwsh pou iparxoun offline proionta
                            System.out.println("\nChoose offline product to reactivate:");
                            counter = 0;
                            for (Product p : offlineProducts) {
                                System.out.println(++counter + ". " + p.getProductName() + ": " + p.getProductType());
                            }
                            System.out.println(++counter + ". Add new product");
                            System.out.println("0. Exit");
                            int productChoice = input.nextInt();
                            input.nextLine();

                            if (productChoice > 0 && productChoice <= offlineProducts.size()) {

                                Product toReactivate = offlineProducts.get(productChoice - 1);
                                toReactivate.setOnline(true);

                                System.out.println("Enter new available amount:");
                                int newAmount = input.nextInt();
                                input.nextLine();
                                toReactivate.setAvailableAmount(newAmount);

                                out.writeObject(new WorkerFunctions("REACTIVATE_PRODUCT", store, toReactivate));
                                out.flush();
                                Object reactivationResponse = in.readObject();
                                System.out.println(reactivationResponse);
                                return;
                            }else if(productChoice == 0){
                                System.out.println("\nCreating new product for " + store.getStoreName());
                                Product product = addProduct(input);
                                out.writeObject(new WorkerFunctions("ADD_PRODUCT", store, product));
                                out.flush();
                                Object response2 = in.readObject();
                                System.out.println("Server response: " + response2);
                            }else{
                                System.out.println("Exiting process");
                            }
                        }else{  //periptwsh pou den iparxoun offline proionta
                            System.out.println("1. Add product");
                            System.out.println("0. Exit");
                            int choice = input.nextInt();
                            input.nextLine();
                            if(choice == 1){
                                System.out.println("\nCreating new product for " + store.getStoreName());
                                Product product = addProduct(input);
                                out.writeObject(new WorkerFunctions("ADD_PRODUCT", store, product));
                                out.flush();
                                Object response2 = in.readObject();
                                System.out.println("Server response: " + response2);
                            }else{
                                System.out.println("Exiting process");
                            }
                        }
                    }

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
        /*
            Methodos gia epilogh statistikou kai emfanish tou
         */
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
                    /*
                    Dialakse katasthma kai epestrepse tis pwlhseis kathe proiontos tou
                    sigkekrimenou katasthmatos
                     */
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
                    /*
                    Kane input kathgoria proiontos kai emfanise ta katasthmata pou thn periexoun
                    kai poses pwlhseis exoun apo auth
                     */
                    System.out.println("Give product category");
                    String prodCategory = input.nextLine();
                    out.writeObject(new WorkerFunctions("PRODUCT_CATEGORY_SALES", prodCategory));
                    out.flush();
                    Map<String, Integer> productCatRes = (Map<String, Integer>) in.readObject();
                    System.out.println("Sales by Product Category: ");
                    productCatRes.forEach((category,sales)->
                            System.out.printf("%-20s: %d%n", category, sales));
                    System.out.println("Total: "+ productCatRes.values().stream().mapToInt(Integer::intValue).sum());
                    break;
                case 3:
                    /*
                    Kane input kathgoria katasthmatos kai emfanise ta katasthmata pou anhkoun se auth
                    kai tis sinolikes pwlhseis kathe katasthmatos
                     */
                    System.out.println("Give shop category");
                    String foodCategory = input.nextLine();
                    out.writeObject(new WorkerFunctions("SHOP_CATEGORY_SALES", foodCategory));
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
                int counter = 0;
                for(Store store : stores){
                    System.out.println(++counter + ". " + store.getStoreName());
                }
                System.out.println("0 Exit");
                int choice = input.nextInt();
                if(choice >= 1 && choice <= stores.size()){
                    Store store = stores.get(choice-1);
                    System.out.println("Choose product to modify quantity: ");
                    counter = 0;
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
}