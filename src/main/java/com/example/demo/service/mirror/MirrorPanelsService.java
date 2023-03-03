package com.example.demo.service.mirror;

import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
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
public class MirrorPanelsService {
    @Autowired
    LendRepository lendRepository;

    @Autowired
    SerialCommunication serialCommunication;

    public String stop() {
        return "Todo : stop";
    }

    public String start() {
        StringBuilder log = new StringBuilder();
        List<Lend> runningLends = lendRepository.findAllByTypeAndStatus(DisplayType.MIRROR, LendStatus.RUNNING);
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

    private static final Logger logger = LoggerFactory.getLogger(MirrorPanelsService.class);

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

    public void clearScreen() {
        if (!OSValidator.isWindows()) {
            try {
                serialCommunication.clearAll();
            } catch (IOException e) {
                logger.error("Error : " + e);
            }
        }
    }

    public String decodeImage(String filePath, Long duration) {
        File file = new File(filePath);
        try {
            int count = serialCommunication.getSize();
            InputStream[] inputStreams = new InputStream[count];
            for (int i = 0; i < count; i++) {
                inputStreams[i] = new FileInputStream(file);
            }
            sendImageToPanels(inputStreams, count);
            for (InputStream inputStream : inputStreams) {
                inputStream.close();
            }
            Thread.sleep(duration * 1000);
        } catch (InterruptedException | IOException e) {
            logger.error("Error : " + e);
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

    public void sendImageToPanels(InputStream[] inputStream, int panelCount) {
        InputStream[] inputStreams = new InputStream[panelCount];
        try {
            Thread[] threads = new Thread[panelCount];
            for (int i = 0; i < panelCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    serialCommunication.runSerial(inputStream[index], index);// Send same panel data to each Arduino via SPI
                });
                threads[i].start();
            }
            // Wait for all threads to complete before returning
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            logger.error("Error : " + e);
        }
    }

    public void sendBufferedImageToPanels(BufferedImage bufferedImage) {
        int panelCount = serialCommunication.getSize();
        InputStream[] inputStreams = new InputStream[panelCount];
        try {
            Thread[] threads = new Thread[panelCount];
            for (int i = 0; i < panelCount; i++) {
                final int index = i;
                threads[index] = new Thread(() -> {
                    try {
                        InputStream is = FileUtils.asInputStream(bufferedImage);
                        inputStreams[index] = is;
                        serialCommunication.runSerial(is, index);// Send the panel data to each Arduino via SPI
                    } catch (IOException e) {
                        logger.error("Error : " + e);
                    }
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
            for (InputStream inputStream : inputStreams) {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    logger.error("sendBufferedImageToPanels Error: " + e);
                }
            }
        }
    }
}
