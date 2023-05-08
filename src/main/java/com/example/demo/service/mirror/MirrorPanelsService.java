package com.example.demo.service.mirror;

import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.decoder.GifFrameExtractorService;
import com.example.demo.service.decoder.ImageFrameExtractorService;
import com.example.demo.service.decoder.ImageFrameExtractorService.ImageFrameExtractorCallback;
import com.example.demo.service.decoder.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.example.demo.model.ExtractionState.*;

@Service
@RequiredArgsConstructor
public class MirrorPanelsService {
    private static final Logger logger = LoggerFactory.getLogger(MirrorPanelsService.class);
    @Autowired
    LendRepository lendRepository;
    @Autowired
    SerialCommunication serialCommunication;
    VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
        sendBufferedImageToPanels(frame);
    };
    GifFrameExtractorService.GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
        sendBufferedImageToPanels(frame);
    };
    ImageFrameExtractorCallback imageFrameExtractorCallback = new ImageFrameExtractorCallback() {
        @Override
        public void onFrameExtracted(InputStream frame, Long frameDelay) {

        }

        @Override
        public void onMirrorFrameExtracted(InputStream[] frame, Long frameDelay) {
            sendImageToPanels(frame, serialCommunication.getSize());
        }
    };
    private GifFrameExtractorService gifFrameExtractorService = null;
    private VideoFrameExtractorService videoFrameExtractorService = null;
    private ImageFrameExtractorService imageFrameExtractorService = null;
    public ExtractionState extractionState = STOPPED;

    public void pause() {
        if (extractionState != STOPPED) {
            extractionState = PAUSED;
        }
        if (gifFrameExtractorService != null) {
            gifFrameExtractorService.pause();
        }
        if (videoFrameExtractorService != null) {
            videoFrameExtractorService.pause();
        }
        if (imageFrameExtractorService != null) {
            imageFrameExtractorService.pause();
        }
    }

    public void start() {
        if (extractionState == STOPPED) {
            run();
        }
        if (extractionState == PAUSED) {
            resume();
        }
        extractionState = RUNNING;
    }

    public void stop() {
        if (gifFrameExtractorService != null) {
            gifFrameExtractorService.stop();
        }
        if (videoFrameExtractorService != null) {
            videoFrameExtractorService.stop();
        }
        if (imageFrameExtractorService != null) {
            imageFrameExtractorService.stop();
        }
        extractionState = STOPPED;
    }

    public void resume() {
        if (gifFrameExtractorService != null) {
            gifFrameExtractorService.resume();
        }
        if (videoFrameExtractorService != null) {
            videoFrameExtractorService.resume();
        }
        if (imageFrameExtractorService != null) {
            imageFrameExtractorService.resume();
        }
    }

    public void run() {
        extractionState = RUNNING;
        List<Lend> runningLends = lendRepository.findAllByTypeAndStatus(DisplayType.MIRROR, LendStatus.RUNNING);
        for (Lend runningLend : runningLends) {
            if (extractionState == STOPPED) break;
            List<Information> profileInformation = runningLend.getProfile().getInformation();
            if(runningLend.getPanel().getStatus().equals(PanelStatus.INACTIVE))
                break;
            for (Information information : profileInformation) {
                if (extractionState == STOPPED) break;
                if (information.getType() == InfoType.VIDEO) {
                    videoFrameExtractorService = new VideoFrameExtractorService();
                    videoFrameExtractorService.start_vid_extraction(information.getUrl(), 15, videoFrameExtractorCallback, Long.valueOf(information.getDuration()));

                } else if (information.getType() == InfoType.GIF) {
                    gifFrameExtractorService = new GifFrameExtractorService();
                    gifFrameExtractorService.start_gif_extraction(information.getUrl(), gifFrameExtractorCallback, Long.valueOf(information.getDuration()));
                } else if (information.getType() == InfoType.IMAGE) {
                    imageFrameExtractorService = new ImageFrameExtractorService();
                    imageFrameExtractorService.extractMirrorImageFrames(information.getUrl(), imageFrameExtractorCallback, Long.valueOf(information.getDuration()), serialCommunication.getSize());
                }
            }
        }
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

}
