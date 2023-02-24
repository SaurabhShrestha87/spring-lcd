package com.example.demo.utils;

import com.example.demo.model.DeviceType;
import com.example.demo.model.draw.Shape;
import com.example.demo.service.DrawService;
import com.example.demo.service.GifFrameExtractorService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.VideoFrameExtractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RunShellCommandFromJava {
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    private final DeviceType device;
    @Autowired
    protected SerialCommunication serialCommunication;
    GifFrameExtractorService.GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
        try {
            if (serialCommunication != null) {
                serialCommunication.runSerial(FileUtils.asInputStream(frame));
            }
        } catch (IOException e) {
            logger.error("gifFrameExtractorCallback : " + e);
        }
    };
    VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
        try {
            if (serialCommunication != null && frame != null) {
                serialCommunication.runSerial(FileUtils.asInputStream(frame));
            }
        } catch (IOException e) {
            logger.error("videoFrameExtractorCallback : " + e);
        }
    };
    private Thread executionThread;
    private GifFrameExtractorService gifFrameExtractorService;

    public RunShellCommandFromJava(DeviceType device) {
        this.device = device;
        serialCommunication = new SerialCommunication(device);
    }

    public void clearScreen() {
        clearExecutions();
        if (!OSValidator.isWindows()) {
            try {
                serialCommunication.serial.write("Q/n");
                serialCommunication.serial.write("Q/n");
                serialCommunication.serial.write("Q/n");
                serialCommunication.serial.write("Q/n");
            } catch (IOException e) {
                logger.error("clearScreen ERROR : " + e);
            }
        }
    }

    public void runCmdForImage(String filePath, Long duration, ThreadCompleteListener listener) {
        clearExecutions();
        File file = new File(filePath);
        try {
            serialCommunication.runSerial(new FileInputStream(file));
            Thread.sleep(duration);
        } catch (FileNotFoundException e) {
            logger.error("runCmdForImage : " + e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        listener.onThreadComplete(false, LocalTime.now() + " ~~~~ {File : " + filePath + "}, {Panel : " + device.toString()); // The thread was not interrupted
    }

    public void runCmdForGif(String gifFilePath, Long duration, ThreadCompleteListener listener) {
        clearExecutions();
        executionThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                gifFrameExtractorService = new GifFrameExtractorService();
                gifFrameExtractorService.extractGifFrames(gifFilePath, gifFrameExtractorCallback);
            }
        });
        executionThread.start();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> executionThread.interrupt(), duration, TimeUnit.SECONDS);
        new Thread(() -> {
            try {
                executionThread.join();
                listener.onThreadComplete(false, LocalTime.now() + " ~~~~ {File : " + gifFilePath + "}, {Panel : " + device.toString()); // The thread was not interrupted
            } catch (InterruptedException e) {
                // Handle the exception
                listener.onThreadComplete(true, LocalTime.now() + " ~~~~ {File : " + gifFilePath + "}, {Panel : " + device.toString()); // The thread was interrupted
            }
        }).start();
    }

    public void runCmdForVideo(String videoFilePath, Long duration, ThreadCompleteListener listener) {
        clearExecutions();
        executionThread = new Thread(() -> {
            VideoFrameExtractorService gifFrameExtractorService = new VideoFrameExtractorService();
            gifFrameExtractorService.extractVideoFrames(videoFilePath, 60, videoFrameExtractorCallback);
        });
        executionThread.start();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> executionThread.interrupt(), duration, TimeUnit.SECONDS);
        new Thread(() -> {
            try {
                executionThread.join();
                listener.onThreadComplete(false, LocalTime.now() + " ~~~~ {File : " + videoFilePath + "}, {Panel : " + device.toString()); // The thread was not interrupted
            } catch (InterruptedException e) {
                // Handle the exception
                listener.onThreadComplete(true, LocalTime.now() + " ~~~~ {File : " + videoFilePath + "}, {Panel : " + device.toString()); // The thread was interrupted
            }
        }).start();
    }

    private void clearExecutions() {
        if (gifFrameExtractorService != null) {
            gifFrameExtractorService.stop();
        }
        if (executionThread != null && executionThread.isAlive()) {
            executionThread.interrupt();
        }
    }

    public void runCmdForShape(List<Shape> shapes) {
        clearExecutions();
        String s = "";
        for (Shape shape : shapes) {
            if (shape.getType().equalsIgnoreCase("square")) {
                s = DrawService.rect(shape);
            } else if (shape.getType().equalsIgnoreCase("circle")) {
                s = DrawService.circle(shape);
            }
            serialCommunication.runSerial(s);
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public interface ThreadCompleteListener {
        void onThreadComplete(boolean interrupted, String s);
    }
}
