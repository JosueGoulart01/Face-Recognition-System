package com.example.facerecognition.config;

import com.example.facerecognition.service.FaceRecognitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenCVRunner implements CommandLineRunner {

    private final FaceRecognitionService faceRecognitionService;

    @Override
    public void run(String... args) {

        log.info("=================================");
        log.info(" SISTEMA DE RECONHECIMENTO FACIAL ");
        log.info("=================================");
        log.info("C -> Cadastrar rosto");
        log.info("Q -> Sair");

        // Inicia em thread separada
        faceRecognitionService.startRecognition();
    }
}