package com.example.facerecognition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FacerecognitionApplication {

    public static void main(String[] args) {

        // FORÇA O JAVA A NÃO RODAR EM MODO HEADLESS
        System.setProperty("java.awt.headless", "false");

        SpringApplication.run(FacerecognitionApplication.class, args);
    }
}