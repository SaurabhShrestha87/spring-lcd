package com.example.demo.service.mirror;

import com.example.demo.model.DeviceType;
import com.example.demo.model.Panel;
import com.example.demo.service.GifFrameExtractorService;
import com.example.demo.service.SerialCommunication;
import com.example.demo.service.VideoFrameExtractorService;
import com.example.demo.utils.FileUtils;
import com.example.demo.utils.OSValidator;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@NoArgsConstructor
public class MirrorDecodingService {
    private static final Logger logger = LoggerFactory.getLogger(MirrorDecodingService.class);
    List<SerialCommunication> serialList = new ArrayList<>();

    public void clearScreen() {
        if (!OSValidator.isWindows()) {
            try {
                for (SerialCommunication serialCommunication : serialList) {
                    serialCommunication.serial.write("Q/n");
                    serialCommunication.serial.write("Q/n");
                    serialCommunication.serial.write("Q/n");
                    serialCommunication.serial.write("Q/n");
                }
            } catch (IOException e) {
                logger.error("clearScreen ERROR : " + e);
            }
        }
    }

    private void init(List<Panel> activePanels) {
        serialList.clear();
        for (Panel activePanel : activePanels) {
            SerialCommunication serialCommunication = new SerialCommunication(DeviceType.fromString(activePanel.getDevice()));
            serialList.add(serialCommunication);
        }
    }

    public String decodeImage(String filePath, Long duration, List<Panel> activePanels) {
        init(activePanels);
        File file = new File(filePath);
        try {
            sendImageToPanels(new FileInputStream(file));
            Thread.sleep(duration * 1000);
        } catch (FileNotFoundException | InterruptedException e) {
            logger.error("runCmdForImage : " + e);
        }
        return "Finished : No Error (IMAGE)";
    }

    public String decodeGif(String gifFilePath, Long duration, List<Panel> activePanels) {
        init(activePanels);
        GifFrameExtractorService.GifFrameExtractorCallback gifFrameExtractorCallback = (frame, frameDelay) -> {
            sendBufferedImageToPanels(frame);
        };
        GifFrameExtractorService gifFrameExtractorService = new GifFrameExtractorService();
        return gifFrameExtractorService.extractGifFrames2(gifFilePath, gifFrameExtractorCallback, duration);
    }

    public String decodeVideo(String videoFilePath, Long duration, List<Panel> activePanels) {
        init(activePanels);
        VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
            sendBufferedImageToPanels(frame);
        };
        VideoFrameExtractorService videoFrameExtractorService = new VideoFrameExtractorService();
        return videoFrameExtractorService.extractVideoFrames2(videoFilePath, 15, videoFrameExtractorCallback, duration);
    }

    public void sendImageToPanels(InputStream inputStream) {
        int panelCount = serialList.size();
        try {
            Thread[] threads = new Thread[panelCount];
            for (int i = 0; i < panelCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    serialList.get(index).runSerial(inputStream);// Send same panel data to each Arduino via SPI
                });
                threads[i].start();
            }
            // Wait for all threads to complete before returning
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendBufferedImageToPanels(BufferedImage bufferedImage) {
        int panelCount = serialList.size();
        try {
            Thread[] threads = new Thread[panelCount];
            for (int i = 0; i < panelCount; i++) {
                final int index = i;
                threads[i] = new Thread(() -> {
                    try {
                        serialList.get(index).runSerial(FileUtils.asInputStream(bufferedImage));// Send the panel data to each Arduino via SPI
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                threads[i].start();
            }
            // Wait for all threads to complete before returning
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
