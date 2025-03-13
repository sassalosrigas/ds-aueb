public class Product {
    String product_name, product_type;
    int available_amount;
    double price;

    public Product(String product_name, String product_type, int available_amount, double price) {
        this.product_name = product_name;
        this.product_type = product_type;
        this.available_amount = available_amount;
        this.price = price;
    }

    public String getProductName() {
        return product_name;
    }

    public String getProductType() {
        return product_type;
    }

    public int getAvailableAmount() {
        return available_amount;
    }

    public double getPrice() {
        return price;
    }

    public void setProductName(String product_name) {
        this.product_name = product_name;
    }

    public void setProductType(String product_type) {
        this.product_type = product_type;
    }

    public void setAvailableAmount(int available_amount) {
        this.available_amount = available_amount;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}