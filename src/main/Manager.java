package main;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Manager{


    public static void addStore(Scanner in) {
        try {
            System.out.println("Please provide the file with the data: ");
            String filepath = in.nextLine();
            Store newStore = JsonHandler.readStoreFromJson(filepath);
            newStore.setFilepath(filepath);
            try{
                Socket socket = new Socket("localhost", 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
                out.writeObject(new WorkerFunctions("ADD_STORE",newStore));
                out.flush();
                Object response = inp.readObject();
                if(response instanceof Store){
                    System.out.println("Server response: " + ((Store) response).getStoreName());
                }
                out.close();
                inp.close();
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

    public static Product addProduct(Scanner in){
        try{
            System.out.println("Give product name:");
            String productName = in.nextLine();
            System.out.println("Give product type:");
            String prodType = in.nextLine();
            System.out.println("Give product price:");
            int price = in.nextInt();
            System.out.println("Give available amount:");
            int amount = in.nextInt();
            return new Product(productName, prodType, price, amount);

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeProductFromStore(Scanner in){
        try{
            System.out.println("Give store's name:");
            String storeName = in.nextLine();
            System.out.println("Give product's name:");
            String productName = in.nextLine();
            try{
                Socket socket = new Socket("localhost", 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
                out.writeObject(new WorkerFunctions("REMOVE_PRODUCT",storeName, productName));
                out.flush();
                Object response = inp.readObject();
                if(response instanceof Store){
                    System.out.println("Server response: " + ((Store) response).getStoreName());
                }
                out.close();
                inp.close();
                socket.close();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void addProductToStore(Scanner in){
        try{
            System.out.println("Give the store's name: ");
            String storeName = in.nextLine();
            Product product = addProduct(in);
            try{
                Socket socket = new Socket("localhost", 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
                out.writeObject(new WorkerFunctions("ADD_PRODUCT",storeName, product));
                out.flush();
                Object response = inp.readObject();
                if(response instanceof Store){
                    System.out.println("Server response: " + ((Store) response).getStoreName());
                }
                out.close();
                inp.close();
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

    public static void salesPerProduct(Scanner in){
        try{
            List<Store> stores = JsonHandler.readStoresFromJson("C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");
            System.out.println("Choose store to see sales per product:");
            int counter = 1;
            for(Store s: stores){
                System.out.println(counter + ". " + s.getStoreName());
                counter++;
            }
            int choice = in.nextInt();
            Store currentStore = stores.get(choice-1);
            for(Product p: currentStore.getProducts()){
                System.out.println("Product name: "  + p.getProductName() + " Total sales: " + 0);
            }
            JsonHandler.writeStoreToJson(currentStore, "store.json");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void modifyAvailability(Scanner in){
        try{
            System.out.println("Give the store's name:");
            String storeName = in.nextLine();
            System.out.println("Give the product's name:");
            String productName = in.nextLine();
            System.out.println("Give new quantity:");
            int quantity = in.nextInt();
            try{
                Socket socket = new Socket("localhost", 8080);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
                out.writeObject(new WorkerFunctions("MODIFY_STOCK",storeName, productName, quantity));
                out.flush();
                Object response = inp.readObject();
                if(response instanceof Store){
                    System.out.println("Server response: " + ((Store) response).getStoreName());
                }
                out.close();
                inp.close();
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

    public static void avgPrice(Scanner in){
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