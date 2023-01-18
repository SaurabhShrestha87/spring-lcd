package com.example.demo.utils;

import com.pi4j.io.serial.Serial;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageToByteArray {
    String path = "";

    public ImageToByteArray(String path) {
        this.path = path;
    }
    public byte[] run(){
        File fnew=new File(path);
        BufferedImage originalImage= null;
        byte[] imageInByte = null;
        try {
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos );
            imageInByte=baos.toByteArray();
            originalImage = ImageIO.read(fnew);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return imageInByte;
    }
}
