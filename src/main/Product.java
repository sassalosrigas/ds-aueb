package main;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {
    String productName, productType;
    int availableAmount;
    double price;
    private boolean showOnline;
    private int totalSales;

    public Product(@JsonProperty("ProductName") String productName,
                   @JsonProperty("ProductType") String productType,
                   @JsonProperty("Available Amount") int availableAmount,
                   @JsonProperty("Price") double price) {
        this.productName = productName;
        this.productType = productType;
        this.availableAmount = availableAmount;
        this.price = price;
        this.showOnline = true;
    }

    public Product() {
    }

    @JsonIgnore
    public void setOnline(boolean showOnline){
        this.showOnline = showOnline;
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

    public int getTotalSales() {
        return totalSales;
    }

    public void addSales(int quantity) {
        totalSales += quantity;
    }

    public boolean isOnline() {
        return showOnline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return productName.equals(product.productName) &&
                productType.equals(product.productType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, productType);
    }
}