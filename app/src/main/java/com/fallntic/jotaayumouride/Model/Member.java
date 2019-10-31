package com.fallntic.jotaayumouride.Model;

import java.io.Serializable;

public class Member implements Serializable {

    public String email;
    public String phoneNumber;
    public String token;

    public Member() {

    }

    public Member(String email, String phoneNumber, String token) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.token = token;
    }
}