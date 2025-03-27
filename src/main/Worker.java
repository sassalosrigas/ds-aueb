package main;

import java.io.*;
import java.net.*;

public class Worker {
    private final int workerId;

    public Worker(int workerId) {
        this.workerId = workerId;
    }

    public String proccessRequest(String request) {
        System.out.println("Worker");
        return "";
    }
}
