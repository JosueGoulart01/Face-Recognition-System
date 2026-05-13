package com.example.facerecognition.service;

import com.example.facerecognition.model.Person;
import com.example.facerecognition.repository.PersonRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;

import org.bytedeco.javacv.*;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;

import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.event.KeyEvent;

import java.io.File;
import java.nio.IntBuffer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaceRecognitionService {

    private final PersonRepository repository;
    private final FaceDetectionService detectionService;
    private final CameraService cameraService;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Map<Integer, String> labelsMap =
            new HashMap<>();

    private LBPHFaceRecognizer recognizer;

    private volatile boolean running = false;

    private static final String DATASET_PATH =
            "data/faces/";

    private static final int FACE_SIZE = 200;

    private static final double CONFIDENCE_THRESHOLD = 70;
    private volatile boolean modelTrained = false;

    // =====================================================
    // START
    // =====================================================

    public void startRecognition() {

        if (running) {
            return;
        }

        running = true;

        executor.submit(this::runCamera);
    }

    // =====================================================
    // STOP
    // =====================================================

    public void stopRecognition() {

        running = false;

        cameraService.stopCamera();

        log.info("Reconhecimento encerrado.");
    }

    // =====================================================
    // INIT LBPH
    // =====================================================

    private void initializeRecognizer() {

        recognizer = LBPHFaceRecognizer.create(
                1,
                8,
                8,
                8,
                CONFIDENCE_THRESHOLD
        );

        recognizer.setThreshold(CONFIDENCE_THRESHOLD);
    }

    // =====================================================
    // TREINAMENTO
    // =====================================================

    private synchronized void trainModel() {

    initializeRecognizer();

    File datasetDir = new File(DATASET_PATH);

    if (!datasetDir.exists()) {
        datasetDir.mkdirs();
    }

    List<Person> persons = repository.findAll();

    if (persons.isEmpty()) {

        log.warn("Nenhuma pessoa cadastrada.");

        modelTrained = false;

        return;
    }

    MatVector photos = new MatVector();

    List<Integer> labels = new ArrayList<>();

    labelsMap.clear();

    for (Person person : persons) {

        Integer label = person.getLabel();

        labelsMap.put(label, person.getName());

        File personDir =
                new File(DATASET_PATH + label);

        if (!personDir.exists()) {
            continue;
        }

        File[] images = personDir.listFiles();

        if (images == null) {
            continue;
        }

        for (File img : images) {

            Mat photo = imread(
                    img.getAbsolutePath(),
                    IMREAD_GRAYSCALE
            );

            if (photo.empty()) {
                continue;
            }

            resize(
                    photo,
                    photo,
                    new Size(FACE_SIZE, FACE_SIZE)
            );

            equalizeHist(photo, photo);

            GaussianBlur(
                    photo,
                    photo,
                    new Size(3, 3),
                    0
            );

            photos.push_back(photo);

            labels.add(label);
        }
    }

    if (photos.size() == 0) {

        log.warn("Nenhuma imagem válida.");

        modelTrained = false;

        return;
    }

    Mat labelsMat =
            new Mat(labels.size(), 1, CV_32SC1);

    IntBuffer buffer =
            labelsMat.createBuffer();

    for (int i = 0; i < labels.size(); i++) {
        buffer.put(i, labels.get(i));
    }

    recognizer.train(photos, labelsMat);

    modelTrained = true;

    log.info(
            "Modelo treinado com {} imagens.",
            photos.size()
    );
        }

    // =====================================================
    // LOOP CAMERA
    // =====================================================

    private void runCamera() {

        try {

            trainModel();

            cameraService.startCamera();

            OpenCVFrameGrabber grabber =
                    cameraService.getGrabber();

            OpenCVFrameConverter.ToMat converter =
                    new OpenCVFrameConverter.ToMat();

            CanvasFrame canvas =
                    new CanvasFrame(
                            "Reconhecimento Facial",
                            CanvasFrame.getDefaultGamma() / 2.2
                    );

            canvas.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            canvas.setAlwaysOnTop(true);

            log.info("Iniciando loop da câmera...");

            while (running && canvas.isVisible()) {

                Frame capturedFrame =
                        grabber.grab();

                if (capturedFrame == null) {

                    log.warn("Frame nulo recebido.");

                    continue;
                }

                Mat frame =
                        converter.convert(capturedFrame);

                if (frame == null || frame.empty()) {

                    log.warn("Frame vazio.");

                    continue;
                }

                Mat gray = new Mat();

                cvtColor(
                        frame,
                        gray,
                        COLOR_BGR2GRAY
                );

                RectVector faces =
                        detectionService.detect(gray);

                for (int i = 0; i < faces.size(); i++) {

                    Rect rect = faces.get(i);

                    Mat face =
                            new Mat(gray, rect);

                    resize(
                            face,
                            face,
                            new Size(FACE_SIZE, FACE_SIZE)
                    );

                    equalizeHist(face, face);

                    IntPointer label =
                            new IntPointer(1);

                    DoublePointer confidence =
                            new DoublePointer(1);

                    String personName =
                            "Desconhecido";

                    Scalar color =
                            new Scalar(0, 0, 255, 0);

                    if (modelTrained) {

                        recognizer.predict(
                                face,
                                label,
                                confidence
                        );

                        int predicted =
                                label.get(0);

                        double conf =
                                confidence.get(0);

                        if (conf < CONFIDENCE_THRESHOLD) {

                            personName =
                                    labelsMap.getOrDefault(
                                            predicted,
                                            "Desconhecido"
                                    );

                            personName +=
                                    " | " +
                                    String.format("%.2f", conf);

                            color =
                                    new Scalar(0, 255, 0, 0);
                        }
                    }

                    rectangle(
                            frame,
                            rect,
                            color,
                            2,
                            LINE_8,
                            0
                    );

                    putText(
                            frame,
                            personName,
                            new Point(
                                    rect.x(),
                                    rect.y() - 10
                            ),
                            FONT_HERSHEY_SIMPLEX,
                            0.7,
                            color
                    );
                }

                canvas.showImage(
                        converter.convert(frame)
                );

                KeyEvent key = null;

                try {

                    key = canvas.waitKey(20);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();

                    running = false;
                }

                if (key != null &&
                        key.getKeyCode() == KeyEvent.VK_C) {

                    registerNewFace(gray, faces);
                }

                if (key != null &&
                        key.getKeyCode() == KeyEvent.VK_Q) {

                    running = false;
                }
            }

            canvas.dispose();

            cameraService.stopCamera();

        } catch (Exception e) {

            log.error("Erro na câmera", e);
        }
    }

    // =====================================================
    // CADASTRO
    // =====================================================

    private synchronized void registerNewFace(
            Mat gray,
            RectVector faces
    ) {

        if (faces.size() == 0) {
            return;
        }

        String name =
                JOptionPane.showInputDialog(
                        "Nome:"
                );

        if (name == null || name.isBlank()) {
            return;
        }

        Person person = repository.save(
                Person.builder()
                        .name(name)
                        .build()
        );

        Integer label =
                person.getId().intValue();

        person.setLabel(label);

        repository.save(person);

        File personDir =
                new File(DATASET_PATH + label);

        personDir.mkdirs();

        Rect rect = faces.get(0);

        for (int i = 0; i < 15; i++) {

            Mat face =
                    new Mat(gray, rect);

            resize(
                    face,
                    face,
                    new Size(FACE_SIZE, FACE_SIZE)
            );

            equalizeHist(face, face);

            String path =
                    personDir.getAbsolutePath()
                            + "/"
                            + UUID.randomUUID()
                            + ".jpg";

            imwrite(path, face);
        }

        labelsMap.put(label, name);

        trainModel();

        log.info("Pessoa cadastrada: {}", name);
    }

    

}