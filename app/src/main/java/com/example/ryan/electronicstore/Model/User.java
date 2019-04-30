package com.example.ryan.electronicstore.Model;


public class User {

    private String UserName;
    private String Password;
    private String IsStaff;
    private String IsAdmin;
    private String Email;
    private String Address;


    public User() {

    }

    public User(String userName, String password,String email,String address) {
        UserName = userName;
        Password = password;
        Email = email;
        Address = address;
        IsStaff = "false";
        IsAdmin = "false";
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getIsAdmin() {
        return IsAdmin;
    }

    public void setIsAdmin(String isAdmin) {
        IsAdmin = isAdmin;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
