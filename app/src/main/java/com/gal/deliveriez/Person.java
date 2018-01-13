package com.gal.deliveriez;

/**
 * Created by Gal on 16/10/2017.
 */

public class Person {

    private String name,position,email;
    private boolean authorized;

    public Person(){}

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }


    public String getPosition() {
        return position;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAuthorized() {
        return authorized;
    }
}
