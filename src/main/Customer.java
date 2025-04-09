package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Customer implements Serializable {
    private String username,password;
    private double longitude,latitude;

    public Customer(String username, String password, double latitude, double longitude){
        this.username = username;
        this.password = password;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public void showNearbyStores(){
        try{
            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_STORES", this));
            out.flush();
            Object response = in.readObject();
            if(response instanceof ArrayList){
                System.out.println("Server response: ");
                for(Store store : (ArrayList<Store>)response){
                    System.out.println(store.getStoreName());
                }
            }
            out.close();
            in.close();
            socket.close();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void buyProducts(Scanner input){
        try{
            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_STORES", this));
            out.flush();
            Object response = in.readObject();
            if(response instanceof ArrayList){
                System.out.println("Server response: ");
                int counter = 1;
                for(Store store : (ArrayList<Store>)response){
                    System.out.println(counter +". "+ store.getStoreName());
                    counter++;
                }
                System.out.println("Choose store");
                int choice = input.nextInt();
                if(choice >= 1 && choice <= ((ArrayList<?>) response).size()){
                    Store store = ((ArrayList<Store>) response).get(choice-1);
                    while(true){
                        counter = 1;
                        System.out.println("Choose product");
                        for(Product product : store.getProducts()){
                            if (product.isOnline()) {
                                System.out.println(counter + ". " + product.getProductName());
                                counter++;
                            }    
                        }
                        System.out.println("0. Complete purchase");
                        choice = input.nextInt();
                        if(choice >= 1 && choice <= store.getProducts().size()){
                            System.out.println("Choose quantity");
                            int quantity = input.nextInt();
                            out.writeObject(new WorkerFunctions("BUY_PRODUCT", store.getProducts().get(choice-1),store, quantity));
                            out.flush();
                            Object response2 = in.readObject();
                            if(response2 instanceof Store){
                                store = (Store) response2;
                            }
                        }else{
                            break;
                        }
                    }
                    out.close();
                    in.close();
                    socket.close();
                }
            }

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void filterStores(Scanner in){
        try {
            System.out.println("Choose Food Category (leave empty for any):");
            String category = in.nextLine();
            System.out.println("Minimum Rating (1-5)");
            double minRate = in.nextDouble();
            System.out.println("Maximum Rating (1-5)");
            double maxRate = in.nextDouble();
            in.nextLine();
            System.out.println("Price Category (leave empty for any):");
            String priceCat = in.nextLine();

            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());

            out.writeObject(new WorkerFunctions("FILTER_STORES",
                    category.isEmpty() ? null : category,
                    minRate, maxRate,
                    priceCat.isEmpty() ? null : priceCat));
            out.flush();

            List<Store> results = (List<Store>) inp.readObject();
            System.out.println("Found " + results.size() + " results");
            results.forEach(store ->
                System.out.println(store.getStoreName() + "(" + store.getFoodCategory() + ") - Rating: " + store.getStars()
                        + " - Price: " + store.getPriceCategory()));
            out.close();
            inp.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rateStore(Scanner in){
        try{
            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_ALL_STORES"));
            out.flush();
            Object response = inp.readObject();
            if(response instanceof ArrayList){
                System.out.println("Server response: ");
                System.out.println("Choose store to rate");
                for(Store store : (ArrayList<Store>)response){
                    System.out.println(store.getStoreName());
                }
                int choice = in.nextInt();
                if(choice >= 1 && choice <= ((ArrayList<?>) response).size()){
                    Store store = ((ArrayList<Store>) response).get(choice-1);
                    System.out.println("Original rating: " + store.getStars());
                    System.out.println("Give rating");
                    int rating = in.nextInt();
                    out.writeObject(new WorkerFunctions("APPLY_RATING",store, rating));
                    out.flush();
                    Object response2 = inp.readObject();
                    if(response2 instanceof String){
                        System.out.println(response2);
                    }
                }
                out.close();
                inp.close();
                socket.close();
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}