package com.example.ryan.electronicstore.Model;



public class Order {

    private String ProductId;
    private String ProductName;
    private String Quantity;
    private String Price;
    private String QtyInWarehouse;

    public Order() {
    }

    public Order(String productId, String productName, String quantity, String price, String qtyInWarehouse) {
        ProductId = productId;
        ProductName = productName;
        Quantity = quantity;
        Price = price;
        QtyInWarehouse = qtyInWarehouse;

    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getQtyInWarehouse() {
        return QtyInWarehouse;
    }

    public void setQtyInWarehouse(String qtyInWarehouse) {
        QtyInWarehouse = qtyInWarehouse;
    }
}
