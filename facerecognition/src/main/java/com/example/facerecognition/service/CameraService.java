package com.example.facerecognition.service;

import lombok.extern.slf4j.Slf4j;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CameraService {

    private OpenCVFrameGrabber grabber;

    public void startCamera() {

        try {

            if (grabber != null) {
                return;
            }

            grabber = new OpenCVFrameGrabber(0);

            grabber.setImageWidth(640);
            grabber.setImageHeight(480);

            grabber.setFrameRate(30);

            grabber.start();

            log.info("Webcam aberta.");

        } catch (Exception e) {

            throw new RuntimeException(
                    "Erro ao abrir webcam",
                    e
            );
        }
    }

    public OpenCVFrameGrabber getGrabber() {
        return grabber;
    }

    public void stopCamera() {

        try {

            if (grabber != null) {

                grabber.stop();
                grabber.release();

                grabber = null;

                log.info("Webcam fechada.");
            }

        } catch (FrameGrabber.Exception e) {

            log.error("Erro ao fechar webcam", e);
        }
    }
}