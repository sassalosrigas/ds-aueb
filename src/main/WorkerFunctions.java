package main;

import java.io.Serializable;

public class WorkerFunctions implements Serializable {
    /*
        Bohthitikh klash gia analhpsh requests apo to ActionForWorkers me eidika constructor gia kathe
        leitourgia pou mporei na prokipsei sthn efarmogh
     */
    String operation;
    Object object, object2, object3;
    String name;
    String name2;
    int num;
    double double1,double2;

    public WorkerFunctions(String operation, Object object) {
        this.operation = operation;
        this.object = object;
    }

    public WorkerFunctions(String operation, Object object, Object object2) {
        this.operation = operation;
        this.object = object;
        this.object2 = object2;
    }

    public WorkerFunctions(String operation, String name, double double1, double double2, String name2) {
        this.operation = operation;
        this.name = name;
        this.double1 = double1;
        this.double2 = double2;
        this.name2 = name2;
    }

    public WorkerFunctions(String operation, String name) {
        this.operation = operation;
        this.name = name;
    }

    public WorkerFunctions(String operation) {
        this.operation = operation;
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

    public WorkerFunctions(String operation, Object object, int num){
        this.operation = operation;
        this.object = object;
        this.num = num;
    }

    public WorkerFunctions(String operation, Object object,Object object2, int num){
        this.operation = operation;
        this.object = object;
        this.object2 = object2;
        this.num = num;
    }

    public WorkerFunctions(String operation, Object object,Object object2,Object object3, int num){
        this.operation = operation;
        this.object = object;
        this.object2 = object2;
        this.object3 = object3;
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

    public Object getObject3() {
        return object3;
    }

    public String getName2(){
        return this.name2;
    }

    public int getNum() {
        return this.num;
    }

    public double getDouble1() {
        return this.double1;
    }
    public double getDouble2() {
        return this.double2;
    }

    public Object getObject2() {
        return this.object2;
    }
}
