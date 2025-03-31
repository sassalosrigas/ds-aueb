package main;

import java.io.Serializable;

public class WorkerFunctions implements Serializable {
    String operation;
    Object object;
    String name;
    String name2;
    int num;

    public WorkerFunctions(String operation, Object object) {
        this.operation = operation;
        this.object = object;
    }

    public WorkerFunctions(String operation,String name, Object object) {
        this.operation = operation;
        this.object = object;
        this.name = name;
    }

    public WorkerFunctions(String operation,String name, String name2) {
        this.operation = operation;
        this.name2 = name2;
        this.name = name;
    }

    public WorkerFunctions(String operation,String name, String name2, int num) {
        this.operation = operation;
        this.name2 = name2;
        this.name = name;
        this.num = num;
    }

    public String getOperation() {
        return this.operation;
    }

    public Object getObject() {
        return this.object;
    }

    public String getName() {
        return this.name;
    }

    public String getName2(){
        return this.name2;
    }

    public int getNum() {
        return this.num;
    }
}
