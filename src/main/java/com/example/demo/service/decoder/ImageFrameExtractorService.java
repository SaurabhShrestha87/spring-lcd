package com.example.demo.service.decoder;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents a service for extracting frames from images and processing them.
 */
@NoArgsConstructor
public class ImageFrameExtractorService {
    private static final Logger logger = LoggerFactory.getLogger(ImageFrameExtractorService.class);
    private boolean isPaused = false; // Flag to indicate if extraction is paused
    private boolean isStopped = false; // Flag to indicate if extraction is stopped

    /**
     * Extracts frames from an image file and invokes the callback with each frame.
     *
     * @param filePath The path of the image file to extract frames from.
     * @param callback The callback interface for handling extracted frames.
     * @param duration The duration in seconds for which frames should be extracted.
     */
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

    /**
     * Extracts frames from an image file and invokes the callback with mirrored frames.
     *
     * @param filePath The path of the image file to extract frames from.
     * @param callback The callback interface for handling extracted frames.
     * @param duration The duration in seconds for which frames should be extracted.
     * @param count    The number of mirrored frames to extract.
     */
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

    /**
     * Pauses the extraction of frames.
     */
    public void pause() {
        isPaused = true;
    }

    /**
     * Resumes the extraction of frames.
     */
    public void resume() {
        isPaused = false;
    }

    /**
     * Stops the extraction of frames.
     */
    public void stop() {
        isStopped = true;
        isPaused = false;
    }

    /**
     * The callback interface for handling extracted frames.
     */
    public interface ImageFrameExtractorCallback {
        /**
         * Invoked when a frame is extracted from an image.
         *
         * @param frame      The InputStream representing the extracted frame.
         * @param frameDelay The delay associated with the frame in seconds.
         */
        void onFrameExtracted(InputStream frame, Long frameDelay);

        /**
         * Invoked when a set of mirrored frames is extracted from an image.
         * @param frames     An array of InputStreams representing the mirrored frames.
         * @param frameDelay The delay associated with the frames in seconds.
         */
        void onMirrorFrameExtracted(InputStream[] frames, Long frameDelay);
    }
}
