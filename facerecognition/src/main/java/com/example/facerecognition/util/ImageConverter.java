package com.example.facerecognition.util;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.javacpp.BytePointer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imencode;

public class ImageConverter {

    private ImageConverter() {}

    public static BufferedImage matToBufferedImage(Mat mat) {

        try {

                BytePointer bytePointer = new BytePointer();

            imencode(".jpg", mat, bytePointer);

            byte[] bytes =
                    new byte[(int) bytePointer.limit()];

            bytePointer.get(bytes);

            return ImageIO.read(
                    new ByteArrayInputStream(bytes)
            );

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}