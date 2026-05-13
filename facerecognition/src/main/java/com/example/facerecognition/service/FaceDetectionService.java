package com.example.facerecognition.service;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
@Slf4j
public class FaceDetectionService {

    private final CascadeClassifier classifier;

    public FaceDetectionService() {

        try {

            InputStream is = new ClassPathResource(
                    "haarcascades/haarcascade_frontalface_default.xml"
            ).getInputStream();

            File temp = File.createTempFile("cascade", ".xml");

            try (FileOutputStream fos = new FileOutputStream(temp)) {
                fos.write(is.readAllBytes());
            }

            classifier = new CascadeClassifier(temp.getAbsolutePath());

            if (classifier.empty()) {
                throw new RuntimeException("Erro ao carregar cascade.");
            }

            log.info("Cascade carregado com sucesso.");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RectVector detect(Mat frameGray) {

        RectVector faces = new RectVector();

        equalizeHist(frameGray, frameGray);

        classifier.detectMultiScale(
                frameGray,
                faces,
                1.1,
                5,
                0,
                new Size(100, 100),
                new Size(500, 500)
        );

        return faces;
    }
}