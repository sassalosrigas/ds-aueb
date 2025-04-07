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
            ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SHOW_STORES", this));
            out.flush();
            Object response = inp.readObject();
            if(response instanceof ArrayList){
                System.out.println("Server response: ");
                for(Store store : (ArrayList<Store>)response){
                    System.out.println(store.getStoreName());
                }
            }
            out.close();
            inp.close();
            socket.close();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void filterStores(Scanner in){
        try{
            String category = "", price = "";
            System.out.println("Choose food category");
            System.out.println("1. pizzeria");
            System.out.println("2. souvlakeri");
            System.out.println("3. taverna");
            int choice = in.nextInt();
            if(choice == 1){
                category = "pizzeria";
            }else if(choice == 2){
                category = "souvlakeri";
            }else if(choice == 3){
                category = "taverna";
            }
            System.out.println("Choose stars");
            System.out.println("Lower boundary");
            double lower = in.nextDouble();
            System.out.println("Upper boundary");
            double upper = in.nextDouble();
            System.out.println("Price category");
            System.out.println("1. $");
            System.out.println("2. $$");
            System.out.println("3. $$$");
            choice = in.nextInt();
            if(choice == 1){
                price = "$";
            }else if(choice == 2){
                price = "$$";
            }else if(choice == 3){
                price = "$$$";
            }
            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inp = new ObjectInputStream(socket.getInputStream());
            out.writeObject(new WorkerFunctions("SEARCH_STORES",category, lower, upper, price));
            out.flush();
            Object response = inp.readObject();
            if(response instanceof ArrayList){
                System.out.println("Server response: ");
                for(Store store : (ArrayList<Store>)response){
                    System.out.println(store.getStoreName());
                }
            }
            out.close();
            inp.close();
            socket.close();
        } catch (Exception e) {
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