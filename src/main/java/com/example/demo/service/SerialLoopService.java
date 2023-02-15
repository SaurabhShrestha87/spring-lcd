package com.example.demo.service;

import com.example.demo.model.DeviceType;
import com.example.demo.utils.RunShellCommandFromJava.GifDecoder.GifFrame;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@NoArgsConstructor
public class SerialLoopService {
    public static final int DEFAULTDELAY = 1; // in milliseconds
    private static final Logger logger = LoggerFactory.getLogger(SerialLoopService.class);
    @Autowired
    protected SerialCommunication serialCommunication;
    VideoDecoder decoder = null;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private InputStream currentInputStream;
    private List<GifFrame> gifFrames;
    private volatile boolean isPaused;
    private volatile boolean replayGif;
    private int GIFCOUNT = 0;

    public SerialLoopService(DeviceType device) {
        serialCommunication = new SerialCommunication(device);
    }

    private void logAll() {
        logger.warn("\nisPaused : " + isPaused +  "\n" + "replayGif : " + replayGif);
        logger.warn("\nexecutorService.isShutdown() : " + executorService.isShutdown());
        logger.warn("\nGIFCOUNT : " + GIFCOUNT);
        logger.warn("\ngifFrames.isnull() : " + (gifFrames == null));
        logger.warn("\ndecoder.isnull() : " + (decoder == null));
        logger.warn("\nDEFAULTDELAY : " + DEFAULTDELAY);
        logger.warn("\n\n\n\n\n\n\n");
    }

    private synchronized void tryToWait(int extraDelay) throws InterruptedException {
        logger.info("tryToWait");
        long delayDiff = extraDelay - DEFAULTDELAY;
        if (delayDiff > 0) {
            wait(delayDiff);
        }
    }

    public synchronized void sendImageOnly(InputStream is) {
        reset();
        serialCommunication.runSerial(is);
    }

    public synchronized void setCurrentInputStream(InputStream inputStream) {
        logger.info("setCurrentInputStream");
        this.currentInputStream = inputStream;
    }


    public synchronized void reset() {
        logger.info("reset");
        gifFrames = null;
        GIFCOUNT = 0;
        isPaused = false;
        replayGif = false;
        currentInputStream = null;
        executorService.shutdown();
        serialCommunication.clearScreen();
        if (decoder != null) {
            decoder.stop();
        }
    }

    public synchronized void pauseGif() {
        logger.info("pauseGif");
        this.isPaused = true;
    }

    public synchronized void resumeGif() {
        logger.info("resumeGif");
        this.isPaused = false;
        this.replayGif = true;
        // Incase executorService is not running, we will restart it
        if (executorService.isShutdown()) {
            startGif(true);
        }
    }

    public void setGifFrames(List<GifFrame> gifFrames) {
        logger.info("setGifFrames");
        this.gifFrames = gifFrames;
    }

    public synchronized void startGif(boolean isReplayGif) {
        logger.info("startGif");
        reset();
        this.replayGif = isReplayGif;
        try {
            if (executorService.isShutdown()) {
                executorService = Executors.newScheduledThreadPool(1);
            } else {
                executorService.shutdown();
                startGif(isReplayGif);
            } // reset executor service
            executorService.scheduleWithFixedDelay(this::loopGif, 0, DEFAULTDELAY, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("scheduleWithFixedDelay ERROR : " + e);
        }
    }

    public void startVideo(String videoFilePath) {
        logger.info("startVideo");
        reset();
        decoder = new VideoDecoder(videoFilePath);
        try {
            if (executorService.isShutdown()) {
                executorService = Executors.newScheduledThreadPool(1);
            } else {
                executorService.shutdown();
                startVideo(videoFilePath);
            } // restart executorService
            executorService.scheduleWithFixedDelay(this::loopVideo, 0, DEFAULTDELAY, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("scheduleWithFixedDelay ERROR : " + e);
        }
    }

    private synchronized void loopVideo() {
        logger.info("loopVideo");
        if (!isPaused) {
            currentInputStream = decoder.extractNextFrame();
            serialCommunication.runSerial(currentInputStream);
        }
    }

    private synchronized void loopGif() {
        logger.info("loopGif");
        if (!isPaused) {
            if (replayGif) {
                logger.info("is replayGif COUNT : " + GIFCOUNT);
                currentInputStream = gifFrames.get(GIFCOUNT).inputStream;
                serialCommunication.runSerial(currentInputStream);
                try {
                    tryToWait(gifFrames.get(GIFCOUNT).delay);
                } catch (InterruptedException e) {
                    throw new RuntimeException("ERROR : " + e);
                }
                if (GIFCOUNT == gifFrames.size() - 1) {
                    GIFCOUNT = 0;
                } else {
                    GIFCOUNT++;
                }
            } else {
                logger.info("is NOT replayGif");
                serialCommunication.runSerial(currentInputStream);
            }
        }
    }

}
