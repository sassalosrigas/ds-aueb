package main;

import java.io.Serializable;

public class WorkerFunctions implements Serializable {
    String operation;
    Object object;
    String name;

    public WorkerFunctions(String operation, Object object) {
        this.operation = operation;
        this.object = object;
    }

    public WorkerFunctions(String operation,String name, Object object) {
        this.operation = operation;
        this.object = object;
        this.name = name;
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
}
