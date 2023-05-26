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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.example.demo.model.ExtractionState.*;

/**
 * Service class for managing individual panels.
 * <p>
 * Handles frame extraction from various sources (video, GIF, image) and sends the frames to the panels.
 */
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

    /**
     * Creates and starts threads for frame extraction and sending frames to the panels.
     */
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
                List<Lend> runningLends = lendRepository.findAllByPanelIdAndStatusAndType(serialCommunication.panelIdFromIndex(finalIndex), LendStatus.RUNNING, DisplayType.CONTIGUOUS);
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

    /**
     * Performs the frame extraction and sending action for each running lend on a panel.
     *
     * @param runningLends the list of running lends on a panel
     * @param index        the index of the panel
     */
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

    /**
     * Pauses the frame extraction process.
     * Pauses all frame extractor services if they exist.
     */
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

    /**
     * Stops the frame extraction process.
     * Stops all frame extractor services if they exist.
     */
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

    /**
     * Resumes the frame extraction process.
     * Resumes all paused frame extractor services if they exist.
     */
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

    /**
     * Starts the frame extraction process.
     * If the extraction state is STOPPED, creates threads for each panel and starts the extraction process.
     * If the extraction state is PAUSED, resumes the extraction process.
     */
    public void start() {
        if (extractionState == STOPPED) {
            createThreads();
        }
        if (extractionState == PAUSED) {
            resume();
        }
    }

    /**
     * Executes the provided shapes on the specified panel.
     *
     * @param shapes     List of shapes to be executed.
     * @param panelIndex Index of the panel where the shapes should be executed.
     * @return A success message indicating that the shapes were uploaded successfully.
     */
    public String execute(List<Shape> shapes, int panelIndex) {
        runCmdForShape(shapes, panelIndex);
        return "Shape uploaded successfully AT " + panelIndex;
    }

    /**
     * Clears all screens by stopping the frame extraction process and sending a clear command to the serial communication.
     * Any IOException thrown during the process is wrapped in a RuntimeException.
     */
    public void clearAllScreens() {
        stop();
        try {
            serialCommunication.clearAll();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs a command for a shape on the specified panel.
     * Sleeps for a fixed duration between executing each shape.
     *
     * @param shapes       List of shapes to be executed.
     * @param panelByIndex Index of the panel where the shapes should be executed.
     */
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
