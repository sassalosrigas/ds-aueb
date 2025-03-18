package main.java;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import static main.java.JsonHandler.readStoreFromJson;
import static main.java.JsonHandler.readStoresFromJson;


public class Main {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        System.out.println("Choose mode: (1) manager, (2) client");
        JsonHandler json = new JsonHandler();
        try{
            List<Store> s = readStoresFromJson("C:\\Users\\dodor\\OneDrive\\Υπολογιστής\\ds_aueb\\ds-aueb\\src\\main\\java\\store.json");
            for(Store store: s){
                System.out.println(store.getStoreName());
                for(Product p: store.getProducts()){
                    System.out.println(p.getProductName());
                }
            }
            Manager.addStore();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //Manager.addStore();
        in.close();

    }
}
