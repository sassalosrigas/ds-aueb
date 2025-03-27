package main;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ActionForWorkers extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    static List<Store> storeList = new ArrayList<>();
    private Worker worker;

    public ActionForWorkers(Socket connection, Worker worker) {
        this.worker = worker;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try{
            Store store = (Store) in.readObject();
            System.out.println(store.getStoreName());
            storeList.add(store);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
