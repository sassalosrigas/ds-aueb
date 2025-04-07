package main;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Product implements Serializable {
    String productName, productType;
    int availableAmount;
    double price;
    boolean showOnline;

    public Product(@JsonProperty("ProductName") String productName,
                   @JsonProperty("ProductType") String productType,
                   @JsonProperty("Available Amount") int availableAmount,
                   @JsonProperty("Price") double price) {
        this.productName = productName;
        this.productType = productType;
        this.availableAmount = availableAmount;
        this.price = price;
        this.showOnline = false;
    }

    public Product() {
    }

    @JsonIgnore
    public boolean getOnline(){
        return this.showOnline;
    }

    public void setOnline(boolean online){
        this.showOnline = online;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductType() {
        return productType;
    }

    public int getAvailableAmount() {
        return availableAmount;
    }

    public double getPrice() {
        return price;
    }

    public void setProductName(String product_name) {
        this.productName = product_name;
    }

    public void setProductType(String product_type) {
        this.productType = product_type;
    }

    public void setAvailableAmount(int available_amount) {
        this.availableAmount = available_amount;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}