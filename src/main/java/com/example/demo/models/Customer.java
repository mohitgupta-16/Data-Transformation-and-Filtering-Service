package com.example.demo.models;

public class Customer {
    private int id;
    private String name;
    private String email;
    private int age;
    private String subscriptionStatus;
    private boolean isAdult;
    private boolean isSubscribed;

    public Customer() {}

    public Customer(int id, String name, String email, int age, String subscriptionStatus) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.subscriptionStatus = subscriptionStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public boolean isAdult() {
        return isAdult;
    }

    public void setAdult(boolean adult) {
        isAdult = adult;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }
}
