package com.example.demo;

import com.example.demo.utils.FileUtils;
import com.example.demo.utils.GifDecoder;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestClass {
    public static void main(String[] args) throws IOException {

    }

    @Test
    public void gifConversionTest() throws IOException {
        List<String> gifFrames = new ArrayList<>();
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
        System.out.println(Timestamp.from(Instant.now()));
        GifDecoder d = new GifDecoder();
        d.read("D:\\upload\\giftest.gif");
        int n = d.getFrameCount();
        System.out.println("getFrameCount : " + n);
        for (int i = 0; i < n; i++) {
            BufferedImage bFrame = d.getFrame(i);// frame i
            int delay = d.getDelay(i);  // display duration of frame in milliseconds
            File iframe = new File(FileUtils.createFileDir("giftest" + "_frame_" + i + ".png"));
            ImageIO.write(bFrame, "png", iframe);
            gifFrames.add(iframe.getAbsolutePath());
            System.out.println("iframe getAbsolutePath!" + iframe.getAbsolutePath());
        }
        int i = 0;
        for (String gifFrame : gifFrames) {
            i++;
            System.out.println("gifFrame at " + i + " : " + gifFrame);
        }
    }
}
