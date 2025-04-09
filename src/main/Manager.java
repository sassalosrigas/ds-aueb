package main;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Manager{


    public static void addStore(Scanner input) {
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
                int choice = input.nextInt();
                Store store = stores.get(choice-1);
                for(int i=0;i<store.getProducts().size();i++){
                    System.out.println(i+1 + ". " + store.getProducts().get(i).getProductName() + ": " + (store.getProducts().get(i).isOnline() ? "Online" : "Offline") );
                }
                System.out.println("Choose product to be removed: ");
                choice = input.nextInt();
                Product product = store.getProducts().get(choice-1);
                out.writeObject(new WorkerFunctions("REMOVE_PRODUCT",store, product));
                out.flush();
                Object response2 = in.readObject();
                if(response2 instanceof String){
                    System.out.println("Server response: " + response2);
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


        public static void addProductToStore(Scanner input){
        try{
            try{
                Socket socket = new Socket("localhost", 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
                out.flush();
                Object response = in.readObject();
                if(response instanceof ArrayList){
                    ArrayList<Store> stores = (ArrayList<Store>) response;
                    System.out.println("Choose store to add product to: ");
                    for(int i = 0;i<stores.size();i++){
                        System.out.println(i+1+ ". " + stores.get(i).getStoreName());
                    }
                    int choice = input.nextInt();
                    if(choice >= 1 && choice <= stores.size()){
                        Store store = stores.get(choice);
                        input.nextLine();
                        Product product = addProduct(input);
                        out.writeObject(new WorkerFunctions("ADD_PRODUCT",store.getStoreName(), product));
                        out.flush();
                        Object response2 = in.readObject();
                        if(response2 instanceof Product){
                            System.out.println("Server response: " + ((Product) response2).getProductName());
                        }else{
                            System.out.println(response2);
                        }
                    }
                    out.close();
                    in.close();
                    socket.close();

                }
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

    public static void salesPerProduct(Scanner input){
        try {
            System.out.println("Choose Report Type");
            System.out.println("1. Sales per product in a store");
            System.out.println("2. Sales by product category");
            System.out.println("3. Sales by shop category");
            int choice = input.nextInt();
            input.nextLine(); // Consume newline

            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.flush();

            switch (choice) {
                case 1:
                    System.out.println("Enter store name:");
                    String storeName = input.nextLine();
                    out.writeObject(new WorkerFunctions("PRODUCT_SALES", storeName));
                    out.flush();
                    Map<String, Integer> results = (Map<String, Integer>) in.readObject();
                    System.out.println("Sales for " +storeName+": ");
                    results.forEach((product,sales)->
                            System.out.printf("%-20s: %d%n", product, sales));
                    break;
                case 2:
                    break;
                case 3:
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
        try{
            System.out.println("Give the store's name:");
            String storeName = input.nextLine();
            System.out.println("Give the product's name:");
            String productName = input.nextLine();
            System.out.println("Give new quantity:");
            int quantity = input.nextInt();
            try{
                Socket socket = new Socket("localhost", 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                out.writeObject(new WorkerFunctions("MODIFY_STOCK",storeName, productName, quantity));
                out.flush();
                Object response = in.readObject();
                if(response instanceof Store){
                    System.out.println("Server response: " + ((Store) response).getStoreName());
                }
                out.close();
                in.close();
                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /*
    public static void modifyAvailability(Scanner in){
        try{
            //List<Store> stores = JsonHandler.readStoresFromJson("C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");
            List<Store> stores = JsonHandler.readStoresFromJson("store.json");
            System.out.println("Choose store to remove product from:");
            int counter = 1;
            for(Store s: stores){
                System.out.println(counter + ". " + s.getStoreName());
                counter++;
            }
            int choice = in.nextInt();
            Store currentStore = stores.get(choice-1);
            System.out.println("Choose product to modify stock:");
            counter = 1;
            for(Product p: currentStore.getProducts()){
                System.out.println(counter + ". " + p.getProductName());
                counter++;
            }
            choice = in.nextInt();
            System.out.println("Give new stock:");
            int newStock = in.nextInt();
            currentStore.getProducts().get(choice-1).setAvailableAmount(newStock);
            //JsonHandler.writeStoreToJson(currentStore, "C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");
            JsonHandler.writeStoreToJson(currentStore, "store.json");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

     */

    public static void avgPrice(){
        try{
            //List<Store> stores = JsonHandler.readStoresFromJson("C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");
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