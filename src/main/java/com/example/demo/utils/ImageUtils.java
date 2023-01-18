package com.example.demo.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageUtils {
    String path = "";

    public byte[] imageToByteArray(String path) throws IOException {
        BufferedImage bufferimage = ImageIO.read(new File(path));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(bufferimage, getExtensionFromFileName(path), output );
        byte [] data = output.toByteArray();
        return data;
        }

    public File imageToByteArrayFile(String path) throws IOException {
        byte[] demBytes = null; // Instead of null, specify your bytes here.
        File outputFile = new File(getNameWithoutExtension(path));
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(demBytes); // Write the bytes and you're done.
        return outputFile;
    }

    public static String getNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
    public static String getExtensionFromFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? ".png" : fileName.substring(1, dotIndex);
    }
}
