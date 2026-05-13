package com.example.facerecognition.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.bytedeco.javacv.OpenCVFrameGrabber;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
public class CameraService {

    private OpenCVFrameGrabber grabber;

    private boolean started = false;

    // =====================================================
    // START CAMERA
    // =====================================================

    public synchronized void startCamera() {

        try {

            if (started) {
                return;
            }

            grabber = new OpenCVFrameGrabber(0);

            grabber.start();

            started = true;

            log.info("Webcam aberta.");

        } catch (Exception e) {

            log.error("Erro ao abrir webcam", e);
        }
    }

    // =====================================================
    // STOP CAMERA
    // =====================================================

    public synchronized void stopCamera() {

        try {

            if (!started) {
                return;
            }

            started = false;

            if (grabber != null) {

                grabber.stop();

                grabber.release();

                grabber = null;
            }

            log.info("Webcam fechada.");

        } catch (Exception e) {

            log.error("Erro ao fechar webcam", e);
        }
    }
}