package com.example.demo.service.decoder;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class represents a service for extracting frames from GIF files and processing them.
 */
@NoArgsConstructor
public class GifFrameExtractorService {
    private static final Logger logger = LoggerFactory.getLogger(GifFrameExtractorService.class);
    private final ArrayList<BufferedImage> frames = new ArrayList<>(); // List to store extracted frames
    private final ArrayList<Integer> delays = new ArrayList<>(); // List to store delays between frames
    private boolean isPaused = false; // Flag to indicate if extraction is paused
    private boolean isStopped = false; // Flag to indicate if extraction is stopped

    /**
     * Starts the extraction of frames from a GIF file.
     *
     * @param filePath The path of the GIF file to extract frames from.
     * @param callback The callback interface for handling extracted frames.
     * @param duration The duration in seconds for which frames should be extracted.
     */
    public void start_gif_extraction(String filePath, GifFrameExtractorCallback callback, Long duration) {
        duration = duration * 1000; // Convert duration to milliseconds
        ArrayList<BufferedImage> frames = new ArrayList<>(); // Local list to store extracted frames
        ArrayList<Integer> delays = new ArrayList<>(); // Local list to store delays between frames
        long totalDelay = 0; // Total delay between frames
        try {
            clearFrames();
            File gifFile = new File(filePath);
            ImageInputStream inputStream = ImageIO.createImageInputStream(gifFile);
            ImageReader reader = ImageIO.getImageReaders(inputStream).next();
            reader.setInput(inputStream);
            int numFrames = reader.getNumImages(true);
            for (int i = 0; i < numFrames; i++) {
                if (isStopped) {
                    return;
                }
                if (isPaused) {
                    i--;
                    Thread.sleep(1000);
                    continue;
                }
                if (duration.compareTo(totalDelay) < 0) {
                    return;
                }
                BufferedImage frame = reader.read(i);
                int delay = getDelay(reader, i) + 100; // Get delay between frames and add 100ms buffer
                frames.add(frame);
                delays.add(delay);
                totalDelay += delay;
                callback.onFrameExtracted(frame, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    logger.error("extractGifFrames 1 Error : " + e); // If the thread is interrupted, stop the extraction
                    return;
                }
            }
            reader.dispose();
            inputStream.close();
            int index = 0;
            while (true) {
                if (isStopped) {
                    return;
                }
                if (isPaused) {
                    Thread.sleep(1000);
                    continue;
                }
                if (duration.compareTo(totalDelay) < 0) {
                    return;
                }
                BufferedImage frame = frames.get(index);
                int delay = delays.get(index);
                callback.onFrameExtracted(frame, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    logger.error("extractGifFrames 1 Error : " + e); // If the thread is interrupted, stop the extraction
                    return;
                }
                index++;
                if (index >= frames.size()) {
                    index = 0;
                }
                totalDelay += delay;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pauses the extraction of frames.
     */
    public void pause() {
        isPaused = true;
    }

    /**
     * Resumes the extraction of frames.
     */
    public void resume() {
        isPaused = false;
    }

    /**
     * Stops the extraction of frames.
     */
    public void stop() {
        isStopped = true;
        isPaused = false;
    }

    /**
     * Retrieves the delay between frames in a GIF image.
     *
     * @param reader     The ImageReader instance for reading the GIF image.
     * @param imageIndex The index of the image/frame in the GIF image.
     * @return The delay between frames in milliseconds.
     * @throws IOException If an I/O error occurs during the retrieval of metadata.
     */
    private int getDelay(ImageReader reader, int imageIndex) throws IOException {
        int delay = 0;
        IIOMetadata metadata = reader.getImageMetadata(imageIndex);
        String[] metadataNames = metadata.getMetadataFormatNames();
        for (String metadataName : metadataNames) {
            Node root = metadata.getAsTree(metadataName);
            NodeList childNodes = root.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if ("GraphicControlExtension".equals(node.getNodeName())) {
                    NamedNodeMap attributes = node.getAttributes();
                    Node delayNode = attributes.getNamedItem("delayTime");
                    if (delayNode != null) {
                        delay = Integer.parseInt(delayNode.getNodeValue()) * 10;
                        break;
                    }
                }
            }
        }
        return delay;
    }

    /**
     * Clears the list of frames and delays.
     */
    public void clearFrames() {
        frames.clear();
        delays.clear();
    }

    /**
     * The callback interface for handling extracted frames.
     */
    public interface GifFrameExtractorCallback {
        /**
         * Invoked when a frame is extracted from the GIF image.
         *
         * @param frame      The extracted BufferedImage representing a frame.
         * @param frameDelay The delay associated with the frame in milliseconds.
         */
        void onFrameExtracted(BufferedImage frame, int frameDelay);
    }
}