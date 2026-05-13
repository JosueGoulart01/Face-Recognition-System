package com.example.facerecognition.view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class FaceRecognitionUI extends JFrame {

    private final JLabel cameraLabel =
            new JLabel();

    private final JLabel statusLabel =
            new JLabel("Sistema iniciado");

    private final JLabel detectedLabel =
            new JLabel("Pessoa: ---");

    private final JLabel facesCountLabel =
            new JLabel("Rostos detectados: 0");

    private final JButton registerButton =
            new JButton("Cadastrar Pessoa");

    private final JButton captureButton =
            new JButton("Capturar Foto");

    private final JButton exitButton =
            new JButton("Encerrar");

    public FaceRecognitionUI() {

        setTitle("Reconhecimento Facial");

        setSize(1200, 800);

        setLayout(new BorderLayout());

        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // =====================================================
        // CAMERA
        // =====================================================

        cameraLabel.setHorizontalAlignment(
                JLabel.CENTER
        );

        cameraLabel.setOpaque(true);

        cameraLabel.setBackground(Color.BLACK);

        add(
                cameraLabel,
                BorderLayout.CENTER
        );

        // =====================================================
        // INFO PANEL
        // =====================================================

        JPanel infoPanel =
                new JPanel();

        infoPanel.setLayout(
                new FlowLayout(
                        FlowLayout.CENTER,
                        20,
                        10
                )
        );

        statusLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        detectedLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        facesCountLabel.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        16
                )
        );

        infoPanel.add(statusLabel);

        infoPanel.add(detectedLabel);

        infoPanel.add(facesCountLabel);

        // =====================================================
        // BUTTONS PANEL
        // =====================================================

        JPanel buttonsPanel =
                new JPanel();

        buttonsPanel.setLayout(
                new FlowLayout(
                        FlowLayout.CENTER,
                        20,
                        10
                )
        );

        registerButton.setPreferredSize(
                new Dimension(180, 40)
        );

        captureButton.setPreferredSize(
                new Dimension(180, 40)
        );

        exitButton.setPreferredSize(
                new Dimension(180, 40)
        );

        captureButton.setEnabled(false);

        buttonsPanel.add(registerButton);

        buttonsPanel.add(captureButton);

        buttonsPanel.add(exitButton);

        // =====================================================
        // BOTTOM PANEL
        // =====================================================

        JPanel bottomPanel =
                new JPanel();

        bottomPanel.setLayout(
                new GridLayout(2, 1)
        );

        bottomPanel.add(infoPanel);

        bottomPanel.add(buttonsPanel);

        add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        setVisible(true);
    }

    // =====================================================
    // UPDATE CAMERA
    // =====================================================

    public void updateCamera(
            BufferedImage image
    ) {

        if (image == null) {
            return;
        }

        ImageIcon icon =
                new ImageIcon(image);

        cameraLabel.setIcon(icon);
    }

    // =====================================================
    // UPDATE STATUS
    // =====================================================

    public void updateStatus(
            String status
    ) {

        statusLabel.setText(
                "Status: " + status
        );
    }

    // =====================================================
    // UPDATE DETECTED PERSON
    // =====================================================

    public void updateDetectedPerson(
            String person
    ) {

        detectedLabel.setText(
                "Pessoa: " + person
        );
    }

    // =====================================================
    // UPDATE FACE COUNT
    // =====================================================

    public void updateFaceCount(
            int count
    ) {

        facesCountLabel.setText(
                "Rostos detectados: " + count
        );
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getCaptureButton() {
        return captureButton;
    }

    public JButton getExitButton() {
        return exitButton;
    }
}