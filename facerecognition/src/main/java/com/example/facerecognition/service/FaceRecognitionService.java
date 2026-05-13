package com.example.facerecognition.service;

import com.example.facerecognition.model.Person;
import com.example.facerecognition.repository.PersonRepository;
import com.example.facerecognition.view.FaceRecognitionUI;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;

import org.springframework.stereotype.Service;

import javax.swing.*;

import java.awt.image.BufferedImage;

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

    private final FaceRecognitionUI ui =
            new FaceRecognitionUI();

    private LBPHFaceRecognizer recognizer;

    private volatile boolean running = false;

    private volatile boolean modelTrained = false;

    private volatile boolean captureRequested = false;

    private volatile boolean registerMode = false;

    private String currentPose = "";

    private int currentPoseIndex = 0;

    private Person currentPerson;

    private File currentPersonDir;

    private static final String[] POSES = {
            "Olhe para frente",
            "Vire para esquerda",
            "Vire para direita"
    };

    private static final String DATASET_PATH =
            "data/faces/";

    private static final int FACE_SIZE = 200;

    private static final double CONFIDENCE_THRESHOLD = 70;

    private static final int DETECTION_INTERVAL = 3;

    private static final int IMAGES_PER_POSE = 3;

    // =====================================================
    // START
    // =====================================================

    public void startRecognition() {

        if (running) {
            return;
        }

        configureUI();

        running = true;

        SwingUtilities.invokeLater(() -> {

            ui.setVisible(true);
        });

        executor.submit(this::runCamera);
    }

    // =====================================================
    // CONFIG UI
    // =====================================================

    private void configureUI() {

        ui.getRegisterButton().addActionListener(e -> {

            if (registerMode) {
                return;
            }

            startRegisterFlow();
        });

        ui.getCaptureButton().addActionListener(e -> {

            if (!registerMode) {
                return;
            }

            captureRequested = true;
        });

        ui.getExitButton().addActionListener(e -> {

            stopRecognition();
        });

        ui.getCaptureButton().setEnabled(false);
    }

    // =====================================================
    // STOP
    // =====================================================

    public void stopRecognition() {

        if (!running) {
            return;
        }

        running = false;

        cameraService.stopCamera();

        SwingUtilities.invokeLater(ui::dispose);

        log.info("Sistema encerrado.");
    }

    // =====================================================
    // INIT RECOGNIZER
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
    // TRAIN MODEL
    // =====================================================

    private synchronized void trainModel() {

        initializeRecognizer();

        File datasetDir = new File(DATASET_PATH);

        if (!datasetDir.exists()) {
            datasetDir.mkdirs();
        }

        List<Person> persons = repository.findAll();

        if (persons.isEmpty()) {

            modelTrained = false;

            log.warn("Nenhuma pessoa cadastrada.");

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

                photos.push_back(photo);

                labels.add(label);
            }
        }

        if (photos.size() == 0) {

            modelTrained = false;

            log.warn("Nenhuma imagem válida.");

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
    // CAMERA LOOP
    // =====================================================

    private void runCamera() {

    OpenCVFrameGrabber grabber = null;

    try {

        trainModel();

        cameraService.startCamera();

        grabber = cameraService.getGrabber();

        if (grabber == null) {

            log.error("Webcam não inicializada.");

            return;
        }

        OpenCVFrameConverter.ToMat converter =
                new OpenCVFrameConverter.ToMat();

        Java2DFrameConverter java2DConverter =
                new Java2DFrameConverter();

        RectVector detectedFaces =
                new RectVector();

        int frameCounter = 0;

        log.info("Loop da câmera iniciado.");

        while (running) {

            Frame capturedFrame;

            try {

                capturedFrame = grabber.grab();

            } catch (Exception e) {

                if (!running) {
                    break;
                }

                log.error(
                        "Erro ao capturar frame.",
                        e
                );

                break;
            }

            if (!running) {
                break;
            }

            if (capturedFrame == null) {
                continue;
            }

            Mat frame =
                    converter.convert(capturedFrame);

            if (frame == null || frame.empty()) {
                continue;
            }

            Mat gray = new Mat();

            cvtColor(
                    frame,
                    gray,
                    COLOR_BGR2GRAY
            );

            frameCounter++;

            // =====================================================
            // DETECÇÃO OTIMIZADA
            // =====================================================

            if (frameCounter % DETECTION_INTERVAL == 0) {

                detectedFaces =
                        detectionService.detect(gray);
            }

            final int facesCount =
                    (int) detectedFaces.size();

            final String statusText =
                    registerMode
                            ? currentPose
                            : "Reconhecimento ativo";

            SwingUtilities.invokeLater(() -> {

                ui.updateFaceCount(facesCount);

                ui.updateStatus(statusText);
            });

            // =====================================================
            // RECONHECIMENTO
            // =====================================================

            for (int i = 0; i < detectedFaces.size(); i++) {

                Rect rect =
                        detectedFaces.get(i);

                Mat face =
                        new Mat(gray, rect);

                resize(
                        face,
                        face,
                        new Size(FACE_SIZE, FACE_SIZE)
                );

                equalizeHist(face, face);

                String detectedName =
                        "Desconhecido";

                Scalar color =
                        new Scalar(0, 0, 255, 0);

                if (modelTrained) {

                    IntPointer label =
                            new IntPointer(1);

                    DoublePointer confidence =
                            new DoublePointer(1);

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

                        detectedName =
                                labelsMap.getOrDefault(
                                        predicted,
                                        "Desconhecido"
                                );

                        detectedName +=
                                " | " +
                                String.format(
                                        "%.2f",
                                        conf
                                );

                        color =
                                new Scalar(
                                        0,
                                        255,
                                        0,
                                        0
                                );
                    }
                }

                final String finalDetectedName =
                        detectedName;

                SwingUtilities.invokeLater(() -> {

                    ui.updateDetectedPerson(
                            finalDetectedName
                    );
                });

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
                        detectedName,
                        new Point(
                                rect.x(),
                                rect.y() - 10
                        ),
                        FONT_HERSHEY_SIMPLEX,
                        0.7,
                        color
                );
            }

            // =====================================================
            // CAPTURA GUIADA
            // =====================================================

            if (registerMode
                    && captureRequested
                    && detectedFaces.size() > 0) {

                captureRequested = false;

                Rect rect =
                        detectedFaces.get(0);

                capturePoseImages(
                        gray,
                        rect
                );
            }

            // =====================================================
            // ATUALIZA UI
            // =====================================================

            Frame uiFrame =
                    converter.convert(frame);

            BufferedImage image =
                    java2DConverter.getBufferedImage(
                            uiFrame
                    );

            SwingUtilities.invokeLater(() -> {

                ui.updateCamera(image);
            });

            // =====================================================
            // PEQUENO DELAY
            // =====================================================

            Thread.sleep(15);
        }

    } catch (Exception e) {

        log.error("Erro na câmera", e);

    } finally {

        running = false;

        try {

            cameraService.stopCamera();

        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {

            ui.dispose();
        });

        log.info("Thread da câmera encerrada.");
    }
}

    // =====================================================
    // START REGISTER
    // =====================================================

    private void startRegisterFlow() {

        String name =
                JOptionPane.showInputDialog(
                        null,
                        "Digite o nome da pessoa"
                );

        if (name == null || name.isBlank()) {
            return;
        }

        currentPerson =
                repository.save(
                        Person.builder()
                                .name(name)
                                .build()
                );

        Integer label =
                currentPerson.getId().intValue();

        currentPerson.setLabel(label);

        repository.save(currentPerson);

        currentPersonDir =
                new File(DATASET_PATH + label);

        currentPersonDir.mkdirs();

        currentPoseIndex = 0;

        currentPose = POSES[currentPoseIndex];

        registerMode = true;

        ui.getCaptureButton().setEnabled(true);

        ui.updateStatus(currentPose);

        log.info(
                "Cadastro iniciado: {}",
                name
        );
    }

    // =====================================================
    // CAPTURE IMAGES
    // =====================================================

    private synchronized void capturePoseImages(
            Mat gray,
            Rect rect
    ) {

        try {

            for (int i = 0; i < IMAGES_PER_POSE; i++) {

                Mat face =
                        new Mat(gray, rect);

                resize(
                        face,
                        face,
                        new Size(FACE_SIZE, FACE_SIZE)
                );

                equalizeHist(face, face);

                GaussianBlur(
                        face,
                        face,
                        new Size(3, 3),
                        0
                );

                String path =
                        currentPersonDir.getAbsolutePath()
                                + "/"
                                + currentPose.replace(" ", "_")
                                + "_"
                                + UUID.randomUUID()
                                + ".jpg";

                imwrite(path, face);

                log.info(
                        "Imagem salva: {}",
                        path
                );

                Thread.sleep(400);
            }

            currentPoseIndex++;

            // =====================================================
            // NEXT POSE
            // =====================================================

            if (currentPoseIndex < POSES.length) {

                currentPose =
                        POSES[currentPoseIndex];

                SwingUtilities.invokeLater(() -> {

                    ui.updateStatus(currentPose);

                    JOptionPane.showMessageDialog(
                            null,
                            currentPose
                    );
                });

            } else {

                registerMode = false;

                ui.getCaptureButton()
                        .setEnabled(false);

                ui.updateStatus(
                        "Treinando modelo..."
                );

                new Thread(() -> {

                    trainModel();

                    SwingUtilities.invokeLater(() -> {

                        ui.updateStatus(
                                "Reconhecimento ativo"
                        );

                        JOptionPane.showMessageDialog(
                                null,
                                "Cadastro concluído!"
                        );
                    });

                }).start();

                log.info("Cadastro concluído.");
            }

        } catch (Exception e) {

            log.error(
                    "Erro ao capturar imagem",
                    e
            );
        }
    }
}