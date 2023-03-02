package com.example.demo.utils;

import com.example.demo.model.DeviceType;
import com.example.demo.model.ThreadResult;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RunShellCommandFromJava {
    private static final Logger logger = LoggerFactory.getLogger(RunShellCommandFromJava.class);
    private final DeviceType device;
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

    public CompletableFuture<ThreadResult> runCmdForImage(String filePath, Long duration) {
        CompletableFuture<ThreadResult> future = new CompletableFuture<>();
        clearExecutions();
        File file = new File(filePath);
        try {
            serialCommunication.runSerial(new FileInputStream(file));
            Thread.sleep(duration * 1000);
        } catch (FileNotFoundException e) {
            logger.error("runCmdForImage : " + e);
        } catch (InterruptedException e) {
            logger.error("Error : " + e);
        }
        ThreadResult result = new ThreadResult(false, LocalTime.now() + "{File : " + filePath + ", duration: " + duration + ",Panel: " + device.toString() + "}", device.getText());
        future.complete(result);
        return future;
    }

    public String runCmdForImage2(String filePath, Long duration) {
        clearExecutions();
        File file = new File(filePath);
        try {
            serialCommunication.runSerial(new FileInputStream(file));
            Thread.sleep(duration * 1000);
        } catch (FileNotFoundException | InterruptedException e) {
            logger.error("runCmdForImage : " + e);
        }
        return "Finished : No Error (IMAGE)";
    }

    public CompletableFuture<ThreadResult> runCmdForGif(String gifFilePath, Long duration) {
        CompletableFuture<ThreadResult> future = new CompletableFuture<>();
        clearExecutions();
        executionThread = new Thread(() -> {
            logger.info("GifFrameExtractorService : STARTED");
            GifFrameExtractorService gifFrameExtractorService = new GifFrameExtractorService();
            gifFrameExtractorService.extractGifFrames(gifFilePath, gifFrameExtractorCallback);
            logger.info("GifFrameExtractorService : FINISHED");
        });
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> executionThread.interrupt(), duration, TimeUnit.SECONDS);
        executionThread.start();
        new Thread(() -> {
            try {
                executionThread.join();
                future.complete(new ThreadResult(false, LocalTime.now() + "{File : " + gifFilePath + ", duration: " + duration + ",Panel: " + device.toString() + "}", device.getText())); // The thread was not interrupted
            } catch (InterruptedException e) {
                // Handle the exception
                future.completeExceptionally(e); // The thread was interrupted
            }
        }).start();
        return future;
    }

    public String runCmdForGif2(String gifFilePath, Long duration) {
        clearExecutions();
        GifFrameExtractorService gifFrameExtractorService = new GifFrameExtractorService();
        return gifFrameExtractorService.extractGifFrames2(gifFilePath, gifFrameExtractorCallback, duration);
    }

    public CompletableFuture<ThreadResult> runCmdForVideo(String videoFilePath, Long duration) {
        CompletableFuture<ThreadResult> future = new CompletableFuture<>();
        clearExecutions();
        executionThread = new Thread(() -> {
            logger.info("VideoFrameExtractorService : STARTED");
            VideoFrameExtractorService videoFrameExtractorService = new VideoFrameExtractorService();
            videoFrameExtractorService.extractVideoFrames(videoFilePath, 15, videoFrameExtractorCallback);
            logger.info("VideoFrameExtractorService : FINISHED");
        });
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(() -> executionThread.interrupt(), duration, TimeUnit.SECONDS);
        executionThread.start();
        new Thread(() -> {
            try {
                executionThread.join();
                future.complete(new ThreadResult(false, LocalTime.now() + "{File : " + videoFilePath + ", duration: " + duration + ",Panel: " + device.toString() + "}", device.getText())); // The thread was not interrupted
            } catch (InterruptedException e) {
                // Handle the exception
                future.completeExceptionally(e); // The thread was interrupted
            }
        }).start();
        return future;
    }

    public String runCmdForVideo2(String videoFilePath, Long duration) {
        clearExecutions();
        VideoFrameExtractorService videoFrameExtractorService = new VideoFrameExtractorService();
        return videoFrameExtractorService.extractVideoFrames2(videoFilePath, 15, videoFrameExtractorCallback, duration);
    }


    private void clearExecutions() {
        if (executionThread != null) {
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
                logger.error("Error : " + e);
            }
        }
    }
}
