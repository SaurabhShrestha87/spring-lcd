package com.example.demo.service.contigous;

import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.decoder.GifFrameExtractorService;
import com.example.demo.service.decoder.GifFrameExtractorService.GifFrameExtractorCallback;
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
import static com.example.demo.service.decoder.VideoFrameExtractorService.VideoFrameExtractorCallback;

@Service
@RequiredArgsConstructor
public class ContigousPanelsService {
    private static final Logger logger = LoggerFactory.getLogger(ContigousPanelsService.class);
    @Autowired
    SerialCommunication serialCommunication;
    @Autowired
    LendRepository lendRepository;
    VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
        sendBufferedImageToPanels(frame);
    };
    GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
        sendBufferedImageToPanels(frame);
    };
    ImageFrameExtractorCallback imageFrameExtractorCallback = new ImageFrameExtractorCallback() {
        @Override
        public void onFrameExtracted(InputStream frame, Long frameDelay) {
            sendImageToPanels(frame);
        }

        @Override
        public void onMirrorFrameExtracted(InputStream[] frame, Long frameDelay) {

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

    public void resume() {
        extractionState = RUNNING;
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

    public void start() {
        if (extractionState == STOPPED) {
            run();
        }
        if (extractionState == PAUSED) {
            resume();
        }
    }

    public void stop() {
        extractionState = STOPPED;
        if (gifFrameExtractorService != null) {
            gifFrameExtractorService.stop();
        }
        if (videoFrameExtractorService != null) {
            videoFrameExtractorService.stop();
        }
        if (imageFrameExtractorService != null) {
            imageFrameExtractorService.stop();
        }
    }

    public void run() {
        extractionState = RUNNING;
        List<Lend> runningLends = lendRepository.findAllByTypeAndStatus(DisplayType.CONTIGUOUS, LendStatus.RUNNING);
        for (Lend runningLend : runningLends) {
            if (extractionState == STOPPED) break;
            List<Information> profileInformation = runningLend.getProfile().getInformation();
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
                    imageFrameExtractorService.extractImageFrames(information.getUrl(), imageFrameExtractorCallback, Long.valueOf(information.getDuration()));
                }
            }
        }
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

    public void clearAllScreens() {
        clearScreen();
    }

    public void clearScreen() {
        if (!OSValidator.isWindows()) {
            try {
                serialCommunication.clearAll();
            } catch (IOException e) {
                logger.error("clearScreen ERROR : " + e);
            }
        }
    }
}
