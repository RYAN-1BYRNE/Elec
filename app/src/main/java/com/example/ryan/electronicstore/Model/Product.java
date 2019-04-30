package com.example.ryan.electronicstore.Model;



public class Product {

    private String Author;
    private String Image;
    private String MenuId;
    private String Name;
    private String Price;
    private String Barcode;
    private String WarehouseQty;


    public Product() {
    }


    public Product(String author, String image, String menuId, String name, String price, String barcode, String warehouseQty) {
        Author = author;
        Image = image;
        MenuId = menuId;
        Name = name;
        Price = price;
        Barcode = barcode;
        WarehouseQty = warehouseQty;

    }

    public String getWarehouseQty() {
        return WarehouseQty;
    }

    public void setWarehouseQty(String warehouseQty) {
        WarehouseQty = warehouseQty;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getMenuId() {
        return MenuId;
    }

    public void setMenuId(String menuId) {
        MenuId = menuId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }
}
