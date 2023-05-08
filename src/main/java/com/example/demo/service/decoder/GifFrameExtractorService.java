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

@NoArgsConstructor
public class GifFrameExtractorService {
    private static final Logger logger = LoggerFactory.getLogger(GifFrameExtractorService.class);
    private final ArrayList<BufferedImage> frames = new ArrayList<>();
    private final ArrayList<Integer> delays = new ArrayList<>();
    private boolean isPaused = false;
    private boolean isStopped = false;

    public void start_gif_extraction(String filePath, GifFrameExtractorCallback callback, Long duration) {
        duration = duration * 1000;
        ArrayList<BufferedImage> frames = new ArrayList<>();
        ArrayList<Integer> delays = new ArrayList<>();
        long totalDelay = 0;
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
                int delay = getDelay(reader, i) + 100;
                frames.add(frame);
                delays.add(delay);
                totalDelay += delay;
                callback.onFrameExtracted(frame, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    logger.error("extractGifFrames 1 Error : " + e);// If the thread is interrupted, stop the extraction
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
                    logger.error("extractGifFrames 1  Error : " + e);// If the thread is interrupted, stop the extraction
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

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public void stop() {
        isStopped = true;
        isPaused = false;
    }

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

    public void clearFrames() {
        frames.clear();
        delays.clear();
    }

    public interface GifFrameExtractorCallback {
        void onFrameExtracted(BufferedImage frame, int frameDelay);
    }
}
