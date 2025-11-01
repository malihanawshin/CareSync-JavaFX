package com.example.healthcareapp;

import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String password = "recept123";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("Hash Length: " + hash.length());
    }
}

