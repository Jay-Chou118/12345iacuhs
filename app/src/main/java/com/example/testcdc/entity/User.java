package com.example.testcdc.entity;

public class User {
    public int id;
    public String name;

    public User(String name) {
        this.name = name;
    }
    public User()
    {

    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
