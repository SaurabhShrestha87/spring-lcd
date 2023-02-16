package com.example.demo.service;

import com.example.demo.model.DeviceType;
import com.example.demo.utils.RunShellCommandFromJava.GifDecoder.GifFrame;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Service
@NoArgsConstructor
public class SerialLoopService {
    public static final int DEFAULTDELAY = 1; // in milliseconds
    private static final Logger logger = LoggerFactory.getLogger(SerialLoopService.class);
    @Autowired
    protected SerialCommunication serialCommunication;
    VideoDecoder decoder = null;
    private TaskScheduler taskScheduler;
    private ScheduledFuture<?> scheduledFuture;
    private InputStream currentInputStream;
    private List<GifFrame> gifFrames;
    private volatile boolean isPaused;
    private boolean isReplayGif;

    private int GIFCOUNT = 0;

    public SerialLoopService(DeviceType device) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.initialize();
        this.taskScheduler = threadPoolTaskScheduler;
        this.scheduledFuture = null;
        serialCommunication = new SerialCommunication(device);
    }

    private synchronized void tryToWait(int extraDelay) {
        logger.info("tryToWait");
        long delayDiff = extraDelay - DEFAULTDELAY;
        if (delayDiff > 0) {
            try {
                wait(delayDiff);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendImageOnly(InputStream is) {
        reset();
        serialCommunication.runSerial(is);
    }

    public void setCurrentInputStream(InputStream inputStream) {
        logger.info("setCurrentInputStream");
        this.currentInputStream = inputStream;
    }

    public void reset() {
        logger.info("reset");
        GIFCOUNT = 0;
        isPaused = false;
        currentInputStream = null;
        serialCommunication.clearScreen();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        if (decoder != null) {
            decoder.stop();
        }
    }

    public void resumeGif(List<GifFrame> gifFrames) {
        this.isPaused = false;
        this.gifFrames = gifFrames;
        startGif(true);
    }

    public void startGif(boolean isReplayGif) {
        logger.info("startGif");
        reset();
        this.isReplayGif = isReplayGif;
        scheduledFuture = taskScheduler.scheduleWithFixedDelay(this::loopGif, DEFAULTDELAY);
    }

    private void loopGif() {
        logger.info("loopGif");
        if (!isPaused) {
            if (isReplayGif) {
                logger.info("IS replayGif, COUNT : " + GIFCOUNT);
                currentInputStream = gifFrames.get(GIFCOUNT).inputStream;
                serialCommunication.runSerial(currentInputStream);
                tryToWait(gifFrames.get(GIFCOUNT).delay);
                if (GIFCOUNT == gifFrames.size() - 1) {
                    GIFCOUNT = 0;
                } else {
                    GIFCOUNT++;
                }
            } else {
                logger.info("IS NOT replayGif");
                serialCommunication.runSerial(currentInputStream);
            }
        }
    }

    public void startVideo(String videoFilePath) {
        logger.info("startVideo");
        reset();
        decoder = new VideoDecoder(videoFilePath);
        scheduledFuture = taskScheduler.scheduleWithFixedDelay(this::loopVideo, DEFAULTDELAY);
    }

    private void loopVideo() {
        logger.info("loopVideo");
        if (!isPaused) {
            currentInputStream = decoder.extractNextFrame();
            serialCommunication.runSerial(currentInputStream);
        }
    }
}
