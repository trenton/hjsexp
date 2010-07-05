package com.amazon.hjs.domain;

/**
 *
 */
public class Customer {
    Long id;

    String email;
    String name;
    String phone;

    public Customer() {
        this(null, null, null);
    }

    public Customer(String email, String name, String phone) {
        this.id = null;
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", email, id);
    }
}
