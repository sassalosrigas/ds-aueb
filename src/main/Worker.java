package main;

import java.io.*;
import java.net.*;

public class Worker {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080);  // Connect to Master
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            Store dummyStore = new Store("Test Store", 37.99, 23.73, "Pizzeria", 5, 100, "logo.png", null);
            out.writeObject(dummyStore);
            out.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
