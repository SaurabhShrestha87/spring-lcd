package com.example.demo.service.individual;

import com.example.demo.model.*;
import com.example.demo.model.draw.Shape;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
import com.example.demo.service.DrawService;
import com.example.demo.service.GifFrameExtractorService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IndividualPanelsService {
    private static final Logger logger = LoggerFactory.getLogger(IndividualPanelsService.class);
    private final Map<Integer, Boolean> threadStatusMap = new ConcurrentHashMap<>();
    public ThreadState threadState = ThreadState.READY;
    @Autowired
    LendRepository lendRepository;
    @Autowired
    PanelRepository panelRepository;
    @Autowired
    SerialCommunication serialCommunication;
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

    public String getData() {
        return threadState.toString();
    }

    public void createThreads() {
        int panelCount = serialCommunication.getSize();
        try {
            Thread[] threads = new Thread[panelCount]; // make '3' threads for 3 panels.
            for (int i = 0; i < panelCount; i++) {
                int finalIndex = i;
                final int index = i;
                List<Lend> runningLends = lendRepository.findAllByPanelIdAndStatus(serialCommunication.panelIdFromIndex(finalIndex), LendStatus.RUNNING);
                Runnable runnable = () -> {
                    doAction(runningLends, index);
                };
                threads[index] = new Thread(runnable);
                threadStatusMap.put(i, false);
                threads[i].start(); //running a runnable on each thread..
            }
            // Wait for all threads to complete before returning
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doAction(List<Lend> runningLends, int index) {
        for (Lend runningLend : runningLends) { // all lends for panel #i,
            Profile profile = runningLend.getProfile();
            List<Information> profileInformation = profile.getInformation();
            for (Information information : profileInformation) {
                execute(information, index);
            } // running all profile from each lend on panel #i,
        }
    }


//    public void startRunningThread(String threadName) {
//        Thread thread = threadMap.get(threadName);
//        if (thread != null && !threadStatusMap.get(threadName)) {
//            thread.start();
//            threadStatusMap.put(threadName, true);
//        }
//    }
//
//    public void pauseThread(String threadName) {
//        Thread thread = threadMap.get(threadName);
//        if (thread != null) {
//            threadStatusMap.put(threadName, true);
//        }
//    }
//
//    public void resumeThread(String threadName) {
//        Thread thread = threadMap.get(threadName);
//        if (thread != null) {
//            threadStatusMap.put(threadName, false);
//        }
//    }
//
//    public void stopThread(String threadName) {
//        Thread thread = threadMap.get(threadName);
//        if (thread != null) {
//            thread.interrupt();
//            threadMap.remove(threadName);
//            threadStatusMap.remove(threadName);
//        }
//    }

    public void startAllThreads() {
        createThreads();
    }

    public String execute(Information information, int panelIndex) {
        if (information.getType() == InfoType.VIDEO) {
            return runVideo(information.getUrl(), Long.valueOf(information.getDuration()), panelIndex);
        } else if (information.getType() == InfoType.GIF) {
            return runGif(information.getUrl(), Long.valueOf(information.getDuration()), panelIndex);
        } else if (information.getType() == InfoType.IMAGE) {
            return runImage(information.getUrl(), Long.valueOf(information.getDuration()), panelIndex);
        } else {
            return "Some Error Occurred during executeSync";
        }
    }

    public String execute(List<Shape> shapes, int PanelIndex) {
        runCmdForShape(shapes, PanelIndex);
        return "Shape uploaded successfully AT " + PanelIndex;
    }

    public void clearAllScreens() {
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

    public String runImage(String filePath, Long duration, int panelByIndex) {
        File file = new File(filePath);
        try {
            serialCommunication.runSerial(new FileInputStream(file), panelByIndex);
            Thread.sleep(duration * 1000);
        } catch (FileNotFoundException | InterruptedException e) {
            logger.error("runCmdForImage : " + e);
        }
        return "Finished : No Error (IMAGE)";
    }

    public String runGif(String gifFilePath, Long duration, int panelByIndex) {
        GifFrameExtractorService gifFrameExtractorService = new GifFrameExtractorService();
        GifFrameExtractorService.GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
            try {
                if (serialCommunication != null) {
                    serialCommunication.runSerial(FileUtils.asInputStream(frame), panelByIndex);
                }
            } catch (IOException e) {
                logger.error("gifFrameExtractorCallback : " + e);
            }
        };
        return gifFrameExtractorService.extractGifFrames2(gifFilePath, gifFrameExtractorCallback, duration);
    }

    public String runVideo(String videoFilePath, Long duration, int panelByIndex) {
        VideoFrameExtractorService videoFrameExtractorService = new VideoFrameExtractorService();
        VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
            try {
                if (serialCommunication != null && frame != null) {
                    serialCommunication.runSerial(FileUtils.asInputStream(frame), panelByIndex);
                }
            } catch (IOException e) {
                logger.error("videoFrameExtractorCallback : " + e);
            }
        };
        return videoFrameExtractorService.extractVideoFrames2(videoFilePath, 15, videoFrameExtractorCallback, duration);
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
