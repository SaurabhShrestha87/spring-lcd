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
    private static final Logger logger = LoggerFactory.getLogger(SerialLoopService.class);
    @Autowired
    protected SerialCommunication serialCommunication;
    private ScheduledExecutorService executorService;
    private volatile InputStream currentInputStream;
    private List<GifFrame> gifFrames;
    private int delay = 40;
    private volatile boolean isPaused;
    private volatile boolean replayGif;
    private int replayGifCount = 0;

    public SerialLoopService(DeviceType device) {
        executorService =  Executors.newScheduledThreadPool(1);
        serialCommunication = new SerialCommunication(device);
    }

    public void setCurrentInputStream(InputStream inputStream) {
        this.currentInputStream = inputStream;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void pause() {
        this.isPaused = true;
    }

    public void resume(Boolean replayGif) {
        this.isPaused = false;
        this.replayGif = replayGif;
    }

    public void setGifFrames(List<GifFrame> gifFrames) {
        this.gifFrames = gifFrames;
    }

    public void sendImageOnly(InputStream is) {
        serialCommunication.runSerial(is);
    }
    public void start(boolean replayGif) {
        this.replayGif = replayGif;
        executorService.scheduleWithFixedDelay(this::myTask, 0, delay, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executorService.shutdown();
        serialCommunication.clearScreen();
    }

    private void myTask() {
        if (!isPaused) {
            if (replayGif) {
                currentInputStream = gifFrames.get(replayGifCount).inputStream;
                serialCommunication.runSerial(currentInputStream);
                replayGifCount++;
                if (replayGifCount > gifFrames.size()) {
                    replayGifCount = 0;
                }
                logger.info("Replaying Gif \n Count : " + replayGifCount);
            } else {
                logger.info("currentInputStream");
                serialCommunication.runSerial(currentInputStream);
            }
        }
        logger.info("myTask paused");
    }
}
