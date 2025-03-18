package main.java;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Product {
    String productName, productType;
    int availableAmount;
    double price;

    public Product(@JsonProperty("ProductName") String productName,
                   @JsonProperty("ProductType") String productType,
                   @JsonProperty("Available Amount") int availableAmount,
                   @JsonProperty("Price") double price) {
        this.productName = productName;
        this.productType = productType;
        this.availableAmount = availableAmount;
        this.price = price;
    }

    public Product() {
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