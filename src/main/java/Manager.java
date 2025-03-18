package main.java;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Manager{


    public static void addStore() {
        Scanner in = new Scanner(System.in);
        try {
            System.out.println("Give name:");
            String storeName = in.nextLine();

            System.out.println("Give latitude:");
            double latitude = in.nextDouble();
            in.nextLine();  // Consume leftover newline

            System.out.println("Give longitude:");
            double longitude = in.nextDouble();
            in.nextLine();  // Consume leftover newline

            System.out.println("Give food category: ");
            String foodCategory = in.nextLine();

            System.out.println("Give stars: ");
            int stars = in.nextInt();
            in.nextLine();

            System.out.println("Give number of votes: ");
            int numOfVotes = in.nextInt();
            in.nextLine();  // Consume leftover newline

            System.out.println("Give store logo: ");
            String storeLogo = in.nextLine();

            List<Product> products = new ArrayList<>();

            System.out.println("How many products do you want to add?: ");
            int answer = in.nextInt();
            for(int i = 0; i < answer; i++){
                in.nextLine();
                products.add(addProduct(in));
            }
            Store newStore = new Store(storeName, latitude, longitude, foodCategory, stars, numOfVotes, storeLogo, products);

            JsonHandler.writeStoreToJson(newStore, "C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");

        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid input. Please enter the correct data type.");
        } catch (java.util.NoSuchElementException e) {
            System.out.println("No input found. Please provide all required inputs.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            in.close();
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
            List<Store> stores = JsonHandler.readStoresFromJson("C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");
            System.out.println("Choose store to remove product from:");
            int counter = 1;
            for(Store s: stores){
                System.out.println(counter + ". " + s.getStoreName());
                counter++;
            }
            int choice = in.nextInt();
            Store currentStore = stores.get(choice-1);
            System.out.println("Choose product to remove:");
            counter = 1;
            for(Product p: currentStore.getProducts()){
                System.out.println(counter + ". " + p.getProductName());
                counter++;
            }
            choice = in.nextInt();
            currentStore.getProducts().remove(choice-1);
            JsonHandler.writeStoreToJson(currentStore, "C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addProductToStore(Scanner in){
        try{
            List<Store> stores = JsonHandler.readStoresFromJson("C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");
            System.out.println("Choose store to add product to:");
            int counter = 1;
            for(Store s: stores){
                System.out.println(counter + ". " + s.getStoreName());
                counter++;
            }
            int choice = in.nextInt();
            Store currentStore = stores.get(choice-1);
            in.nextLine();
            currentStore.getProducts().add(addProduct(in));
            JsonHandler.writeStoreToJson(currentStore, "C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");

        } catch (IOException e) {
            throw new RuntimeException(e);
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
            choice = in.nextInt();
            JsonHandler.writeStoreToJson(currentStore, "C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void modifyAvailability(Scanner in){
        try{
            List<Store> stores = JsonHandler.readStoresFromJson("C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");
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
            Product currentProduct = currentStore.getProducts().get(choice-1);
            System.out.println("Give new stock:");
            int newStock = in.nextInt();
            currentStore.getProducts().get(choice-1).setAvailableAmount(newStock);
            JsonHandler.writeStoreToJson(currentStore, "C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}