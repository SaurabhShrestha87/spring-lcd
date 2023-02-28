package com.example.demo.service.contigous;

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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@NoArgsConstructor
public class ContigousDecodingService {
    private static final Logger logger = LoggerFactory.getLogger(ContigousDecodingService.class);
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
            try {
                sendImageToPanels(FileUtils.asInputStream(frame));
            } catch (IOException e) {
                logger.error("gifFrameExtractorCallback : " + e);
            }
        };
        GifFrameExtractorService gifFrameExtractorService = new GifFrameExtractorService();
        return gifFrameExtractorService.extractGifFrames2(gifFilePath, gifFrameExtractorCallback, duration);
    }

    public String decodeVideo(String videoFilePath, Long duration, List<Panel> activePanels) {
        init(activePanels);
        VideoFrameExtractorService.VideoFrameExtractorCallback videoFrameExtractorCallback = (frame, COUNT) -> {
            if (frame != null) {
                try {
                    sendImageToPanels(FileUtils.asInputStream(frame));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        VideoFrameExtractorService videoFrameExtractorService = new VideoFrameExtractorService();
        return videoFrameExtractorService.extractVideoFrames2(videoFilePath, 15, videoFrameExtractorCallback, duration);
    }

    public void sendImageToPanels(InputStream inputStream) {
        int numPanels = serialList.size();
        try {
            // Read the entire InputStream into a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] imageData = baos.toByteArray();
            // Calculate the length of each portion based on the number of panels
            int portionLength = imageData.length / numPanels;
            // Create new byte arrays for each portion
            byte[][] panelData = new byte[numPanels][];
            for (int i = 0; i < numPanels; i++) {
                panelData[i] = new byte[portionLength];
            }
            // Copy the appropriate portion of the image data into each panel data array
            for (int i = 0; i < numPanels; i++) {
                int start = i * portionLength;
                System.arraycopy(imageData, start, panelData[i], 0, portionLength);
            }
            Thread[] threads = new Thread[numPanels];
            for (int i = 0; i < numPanels; i++) {
                final byte[] data = panelData[i];
                final int index = i;
                threads[i] = new Thread(() -> {
                    serialList.get(index).runSerial(data);
                });
                threads[i].start();
            }
            // Wait for all threads to complete before returning
            for (Thread thread : threads) {
                thread.join();
            }
            // Send the panel data to each Arduino via SPI
        } catch (IOException e) {
            logger.error("sendImageToPanels: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
