package com.example.facerecognition.controller;

import com.example.facerecognition.service.FaceRecognitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faces")
@RequiredArgsConstructor
public class FaceRecognitionController {

    private final FaceRecognitionService service;

    @PostMapping("/start")
    public String start() {

        service.startRecognition();

        return "Reconhecimento iniciado.";
    }

    @PostMapping("/stop")
    public String stop() {

        service.stopRecognition();

        return "Reconhecimento parado.";
    }
}