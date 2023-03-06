package com.example.demo.service.decoder;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@NoArgsConstructor
public class ImageFrameExtractorService {
    private static final Logger logger = LoggerFactory.getLogger(ImageFrameExtractorService.class);
    private boolean isPaused = false;
    private boolean isStopped = false;

    public void extractImageFrames(String filePath, ImageFrameExtractorCallback callback, Long duration) {
        try {
            InputStream is = new FileInputStream(new File(filePath));
            callback.onFrameExtracted(is, duration);
            for (int i = 0; i < duration * 1000; i = i + 1000) {
                if (isPaused) {
                    i = i - 1000;
                }
                if (isStopped) {
                    break;
                }
                Thread.sleep(1000);
            }
            is.close();
        } catch (InterruptedException | IOException e) {
            logger.error("runCmdForImage Error: " + e);
        }
    }

    public void extractMirrorImageFrames(String filePath, ImageFrameExtractorCallback callback, Long duration, int count) {
        try {
            InputStream[] inputStreams = new InputStream[count];
            for (int i = 0; i < count; i++) {
                inputStreams[i] = new FileInputStream(filePath);
            }
            callback.onMirrorFrameExtracted(inputStreams, duration);
            for (InputStream inputStream : inputStreams) {
                inputStream.close();
            }
            for (int i = 0; i < duration * 1000; i = i + 1000) {
                if (isPaused) {
                    i = i - 1000;
                }
                if (isStopped) {
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException | IOException e) {
            logger.error("Error : " + e);
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public void stop() {
        isStopped = true;
        isPaused = false;
    }

    public interface ImageFrameExtractorCallback {
        void onFrameExtracted(InputStream frame, Long frameDelay);

        void onMirrorFrameExtracted(InputStream[] frame, Long frameDelay);
    }
}
