package com.example.demo.service.individual;

import com.example.demo.model.*;
import com.example.demo.model.draw.Shape;
import com.example.demo.repository.LendRepository;
import com.example.demo.service.DrawService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.decoder.GifFrameExtractorService;
import com.example.demo.service.decoder.ImageFrameExtractorService;
import com.example.demo.service.decoder.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.example.demo.model.ExtractionState.*;

@Service
@RequiredArgsConstructor
public class IndividualPanelsService {
    private static final Logger logger = LoggerFactory.getLogger(IndividualPanelsService.class);
    public ExtractionState extractionState = STOPPED;
    @Autowired
    LendRepository lendRepository;
    @Autowired
    SerialCommunication serialCommunication;
    private VideoFrameExtractorService[] videoFrameExtractorServices = null;
    private ImageFrameExtractorService[] imageFrameExtractorServices = null;
    private GifFrameExtractorService[] gifFrameExtractorServices = null;

    private void createThreads() {
        extractionState = RUNNING;
        int panelCount = serialCommunication.getSize();
        videoFrameExtractorServices = new VideoFrameExtractorService[panelCount];
        gifFrameExtractorServices = new GifFrameExtractorService[panelCount];
        imageFrameExtractorServices = new ImageFrameExtractorService[panelCount];
        try {
            Thread[] threads = new Thread[panelCount]; // make '3' threads for 3 panels.
            for (int i = 0; i < panelCount; i++) {
                int finalIndex = i;
                final int index = i;
                List<Lend> runningLends = lendRepository.findAllByPanelIdAndStatus(serialCommunication.panelIdFromIndex(finalIndex), LendStatus.RUNNING);
                Runnable runnable = () -> doAction(runningLends, index);
                threads[index] = new Thread(runnable);
                threads[i].start(); //running a runnable on each thread..
            }
            // Wait for all threads to complete before returning for (Thread thread : threads) {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doAction(List<Lend> runningLends, int index) {
        for (Lend runningLend : runningLends) { // all lends for panel #i,
            while (extractionState == PAUSED) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("extractionState == PAUSED ERROR :" + e);
                }
            }
            if (extractionState == STOPPED) { // check if the threads have been stopped
                break;
            }
            Profile profile = runningLend.getProfile();
            List<Information> profileInformation = profile.getInformation();
            for (Information information : profileInformation) {
                while (extractionState == PAUSED) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.error("extractionState == PAUSED ERROR :" + e);
                    }
                }
                if (extractionState == STOPPED) { // check if the threads have been stopped
                    break;
                }
                if (information.getType() == InfoType.VIDEO) {
                    videoFrameExtractorServices[index] = new VideoFrameExtractorService();
                    VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
                        try {
                            if (serialCommunication != null && frame != null) {
                                serialCommunication.runSerial(FileUtils.asInputStream(frame), index);
                            }
                        } catch (IOException e) {
                            logger.error("videoFrameExtractorCallback : " + e);
                        }
                    };
                    videoFrameExtractorServices[index].start_vid_extraction(information.getUrl(), 15, videoFrameExtractorCallback, Long.valueOf(information.getDuration()));
                } else if (information.getType() == InfoType.GIF) {
                    gifFrameExtractorServices[index] = new GifFrameExtractorService();
                    GifFrameExtractorService.GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
                        try {
                            if (serialCommunication != null) {
                                serialCommunication.runSerial(FileUtils.asInputStream(frame), index);
                            }
                        } catch (IOException e) {
                            logger.error("gifFrameExtractorCallback : " + e);
                        }
                    };
                    gifFrameExtractorServices[index].start_gif_extraction(information.getUrl(), gifFrameExtractorCallback, Long.valueOf(information.getDuration()));
                } else if (information.getType() == InfoType.IMAGE) {
                    imageFrameExtractorServices[index] = new ImageFrameExtractorService();
                    ImageFrameExtractorService.ImageFrameExtractorCallback imageFrameExtractorCallback = new ImageFrameExtractorService.ImageFrameExtractorCallback() {
                        @Override
                        public void onFrameExtracted(InputStream frame, Long frameDelay) {
                            serialCommunication.runSerial(frame, index);
                        }

                        @Override
                        public void onMirrorFrameExtracted(InputStream[] frame, Long frameDelay) {
                        }
                    };
                    imageFrameExtractorServices[index].extractImageFrames(information.getUrl(), imageFrameExtractorCallback, Long.valueOf(information.getDuration()));
                }
            }
        }
    }

    public void pause() {
        if (extractionState != STOPPED) {
            extractionState = PAUSED;
        }
        if (gifFrameExtractorServices != null) {
            for (GifFrameExtractorService gifFrameExtractorService : gifFrameExtractorServices) {
                if (gifFrameExtractorService != null) {
                    gifFrameExtractorService.pause();
                }
            }
        }
        if (videoFrameExtractorServices != null) {
            for (VideoFrameExtractorService videoFrameExtractorService : videoFrameExtractorServices) {
                if (videoFrameExtractorService != null) {
                    videoFrameExtractorService.pause();
                }
            }
        }
        if (imageFrameExtractorServices != null) {
            for (ImageFrameExtractorService imageFrameExtractorService : imageFrameExtractorServices) {
                if (imageFrameExtractorService != null) {
                    imageFrameExtractorService.pause();
                }
            }
        }
    }

    public void stop() {
        extractionState = STOPPED;
        if (gifFrameExtractorServices != null) {
            for (GifFrameExtractorService gifFrameExtractorService : gifFrameExtractorServices) {
                if (gifFrameExtractorService != null) {
                    gifFrameExtractorService.stop();
                }
            }
        }
        if (videoFrameExtractorServices != null) {
            for (VideoFrameExtractorService videoFrameExtractorService : videoFrameExtractorServices) {
                if (videoFrameExtractorService != null) {
                    videoFrameExtractorService.stop();
                }
            }
        }
        if (imageFrameExtractorServices != null) {
            for (ImageFrameExtractorService imageFrameExtractorService : imageFrameExtractorServices) {
                if (imageFrameExtractorService != null) {
                    imageFrameExtractorService.stop();
                }
            }
        }
    }

    private void resume() {
        extractionState = RUNNING;
        if (gifFrameExtractorServices != null) {
            for (GifFrameExtractorService gifFrameExtractorService : gifFrameExtractorServices) {
                if (gifFrameExtractorService != null) {
                    gifFrameExtractorService.resume();
                }
            }
        }
        if (videoFrameExtractorServices != null) {
            for (VideoFrameExtractorService videoFrameExtractorService : videoFrameExtractorServices) {
                if (videoFrameExtractorService != null) {
                    videoFrameExtractorService.resume();
                }
            }
        }
        if (imageFrameExtractorServices != null) {
            for (ImageFrameExtractorService imageFrameExtractorService : imageFrameExtractorServices) {
                if (imageFrameExtractorService != null) {
                    imageFrameExtractorService.resume();
                }
            }
        }
    }

    public void start() {
        if (extractionState == STOPPED) {
            logger.info("STARTING");
            createThreads();
        }
        if (extractionState == PAUSED) {
            logger.info("RESUMING");
            resume();
        }
    }

    public String execute(List<Shape> shapes, int PanelIndex) {
        runCmdForShape(shapes, PanelIndex);
        return "Shape uploaded successfully AT " + PanelIndex;
    }

    public void clearAllScreens() {
        stop();
        try {
            serialCommunication.clearAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearScreen(int panelByIndex) {
        if (!OSValidator.isWindows()) {
            try {
                serialCommunication.clearPanelAtIndex(panelByIndex);
            } catch (IOException e) {
                logger.error("clearScreen ERROR : " + e);
            }
        }
    }

    public void runCmdForShape(List<Shape> shapes, int panelByIndex) {
        String s = "";
        for (Shape shape : shapes) {
            if (shape.getType().equalsIgnoreCase("square")) {
                s = DrawService.rect(shape);
            } else if (shape.getType().equalsIgnoreCase("circle")) {
                s = DrawService.circle(shape);
            }
            serialCommunication.runSerial(s, panelByIndex);
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                logger.error("Error : " + e);
            }
        }
    }
}
