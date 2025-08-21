package org.example.lufthansaurlsystem;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Base64;

@SpringBootApplication
@EnableScheduling
public class LufthansaUrlSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LufthansaUrlSystemApplication.class, args);
//        PasswordEncoder encoder = new BCryptPasswordEncoder();
//        // Replace this with the password you want to hash
//        String rawPassword = "password1";
//        String hashedPassword = encoder.encode(rawPassword);
//
//        System.out.println("Raw password: " + rawPassword);
//        System.out.println("BCrypt hashed password: " + hashedPassword);
    }

}
