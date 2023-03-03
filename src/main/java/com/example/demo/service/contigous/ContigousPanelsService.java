package com.example.demo.service.contigous;

import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.service.GifFrameExtractorService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContigousPanelsService {
    private static final Logger logger = LoggerFactory.getLogger(ContigousPanelsService.class);
    @Autowired
    LendRepository lendRepository;
    String logs = "";
    int logCOUNT = 0;

    private void makeLogs(String log) {
        logs = logs + log + "\n";
        logCOUNT++;
        if (logCOUNT > 100) {
            logs = "";
        }
    }

    public String getLogs() {
        return logs;
    }

    public String stop() {
        return "Todo : stop";
    }

    public String start() {
        StringBuilder log = new StringBuilder();
        List<Lend> runningLends = lendRepository.findAllByTypeAndStatus(DisplayType.CONTIGUOUS, LendStatus.RUNNING);
        for (Lend runningLend : runningLends) {
            log.append(doAction(runningLend.getProfile()));
        }
        return log.toString();
    }

    private String doAction(Profile profile) {
        StringBuilder log = new StringBuilder();
        List<Information> profileInformation = profile.getInformation();
        for (Information information : profileInformation) {
            log.append(execute(information));
        }
        return log.toString();
    }

    public String execute(Information information) {
        if (information.getType() == InfoType.VIDEO) {
            return decodeVideo(information.getUrl(), Long.valueOf(information.getDuration()));
        } else if (information.getType() == InfoType.GIF) {
            return decodeGif(information.getUrl(), Long.valueOf(information.getDuration()));
        } else if (information.getType() == InfoType.IMAGE) {
            return decodeImage(information.getUrl(), Long.valueOf(information.getDuration()));
        } else {
            return "Some Error Occurred during executeSync";
        }
    }

    public void clearAllScreens() {
        clearScreen();
    }

    @Autowired
    SerialCommunication serialCommunication;

    public void clearScreen() {
        if (!OSValidator.isWindows()) {
            try {
                serialCommunication.clearAll();
            } catch (IOException e) {
                logger.error("clearScreen ERROR : " + e);
            }
        }
    }

    public String decodeImage(String filePath, Long duration) {
        File file = new File(filePath);
        try {
            InputStream is = new FileInputStream(file);
            sendImageToPanels(is);
            Thread.sleep(duration * 1000);
            is.close();
        } catch (InterruptedException | IOException e) {
            logger.error("runCmdForImage Error: " + e);
        }
        return "Finished : No Error (IMAGE)";
    }

    public String decodeGif(String gifFilePath, Long duration) {
        GifFrameExtractorService.GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
            sendBufferedImageToPanels(frame);
        };
        GifFrameExtractorService gifFrameExtractorService = new GifFrameExtractorService();
        return gifFrameExtractorService.extractGifFrames2(gifFilePath, gifFrameExtractorCallback, duration);
    }

    public String decodeVideo(String videoFilePath, Long duration) {
        VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
            sendBufferedImageToPanels(frame);
        };
        VideoFrameExtractorService videoFrameExtractorService = new VideoFrameExtractorService();
        return videoFrameExtractorService.extractVideoFrames2(videoFilePath, 15, videoFrameExtractorCallback, duration);
    }

    public void sendImageToPanels(InputStream inputStream) {
        int panelCount = serialCommunication.getSize();
        InputStream[] list = FileUtils.splitInputStreamHorizontally(inputStream, panelCount);
        try {
            Thread[] threads = new Thread[panelCount];
            for (int i = 0; i < panelCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    serialCommunication.runSerial(list[index], index);// Send the panel data to each Arduino via SPI
                });
                threads[i].start();
            }
            // Wait for all threads to complete before returning
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            logger.error("Error : " + e);
        } finally {
            for (InputStream inputStreams : list) {
                try {
                    inputStreams.close();
                } catch (IOException e) {
                    logger.error("Error closing input streams: " + e.getMessage());
                }
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.error("Error closing input stream: " + e.getMessage());
            }
        }
    }

    public void sendBufferedImageToPanels(BufferedImage bufferedImage) {
        int panelCount = serialCommunication.getSize();
        try {
            InputStream[] list = FileUtils.splitBufferedImageHorizontally(bufferedImage, panelCount);
            try {
                Thread[] threads = new Thread[panelCount];
                for (int i = 0; i < panelCount; i++) {
                    final int index = i;
                    threads[i] = new Thread(() -> {
                        serialCommunication.runSerial(list[index], index);// Send the panel data to each Arduino via SPI
                    });
                    threads[i].start();
                }
                // Wait for all threads to complete before returning
                for (Thread thread : threads) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                logger.error("Error : " + e);
            } finally {
                for (InputStream inputStream : list) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        logger.error("sendBufferedImageToPanels Error: " + e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error" + e);
        }
    }
}
