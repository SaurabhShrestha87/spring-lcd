package com.example.demo.utils;

import com.example.demo.model.InfoType;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import com.example.demo.service.VideoFrameExtractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
    public static List<Panel> getPanelsList() {
        List<Panel> panelList = new ArrayList<>();
        RegexFileFilter regexFileFilter = new RegexFileFilter("ttyACM*");
        File dir = new File("/dev");
        if (OSValidator.isWindows()) {
            dir = new File("E:\\dev\\");
        }
        if (!dir.isDirectory()) throw new IllegalStateException("Unknown Directory!");
        for (File file : dir.listFiles(regexFileFilter)) {
            String panel_id = file.getName().substring(6);
            panelList.add(new Panel(Long.parseLong(panel_id), file.getName(), "30x118", PanelStatus.ACTIVE, null));
        }
        return panelList;
    }

    public static String createFileDir(String fileName) {
        return OSValidator.isWindows() ? "E:\\upload\\" + fileName : "/home/pi/Application/Uploads/" + fileName;
    }

    public static InfoType getFileType(String fileName) {
        if (fileName.endsWith("gif") || fileName.endsWith("GIF")) {
            return InfoType.GIF;
        }
        if (fileName.endsWith("png")) {
            return InfoType.IMAGE;
        }
        return InfoType.VIDEO;
    }

    public static void inputStreamToFIle(InputStream initialStream, long COUNT) throws IOException {
        File targetFile = new File("E:\\upload\\output\\VideoFRAME_" + COUNT + ".png");
        OutputStream outStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = initialStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        initialStream.close();
        outStream.close();
    }

    public static InputStream asInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static InputStream[] splitInputStreamHorizontally(InputStream inputStream, int n) {
        BufferedImage originalImage = null;
        try {
            originalImage = ImageIO.read(inputStream);
        } catch (IOException e) {
            System.out.println("splitInputStreamHorizontally Error: " + e);
        }
        int originalWidth = originalImage.getWidth();
        int splitWidth = originalWidth / n;
        int outputHeight = 118;
        int outputWidth = 30;

        InputStream[] splitStreams = new InputStream[n];

        for (int i = 0; i < n; i++) {
            BufferedImage splitImage = originalImage.getSubimage(i * splitWidth, 0, splitWidth, outputHeight);
            BufferedImage resizedImage = new BufferedImage(outputWidth, outputHeight, BufferedImage.SCALE_DEFAULT);
            resizedImage.getGraphics().drawImage(splitImage, 0, 0, outputWidth, outputHeight, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(resizedImage, "png", baos);
            } catch (IOException e) {
                System.out.println("splitInputStreamHorizontally Error: " + e);
            }
            InputStream splitStream = new ByteArrayInputStream(baos.toByteArray());
            splitStreams[i] = splitStream;
        }
        return splitStreams;
    }

    public static InputStream[] splitBufferedImageHorizontally(BufferedImage originalImage, int n) throws Exception {
        int originalWidth = originalImage.getWidth();
        int splitWidth = originalWidth / n;
        int outputHeight = 118;
        int outputWidth = 30;
        InputStream[] splitStreams = new InputStream[n];
        if (outputHeight > originalImage.getHeight() || outputWidth > originalImage.getWidth())
            throw new Exception("Bad image"); // Bad image
        for (int i = 0; i < n; i++) {
            try {
                BufferedImage splitImage = originalImage.getSubimage(i * splitWidth, 0, splitWidth, outputHeight);
                BufferedImage resizedImage = new BufferedImage(outputWidth, outputHeight, BufferedImage.SCALE_DEFAULT);
                resizedImage.getGraphics().drawImage(splitImage, 0, 0, outputWidth, outputHeight, null);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    ImageIO.write(resizedImage, "png", baos);
                } catch (IOException e) {
                    System.out.printf("splitBufferedImageHorizontally ERROR.. originalImage : " + originalImage);
                    logger.error("Error : " + e);
                }
                InputStream splitStream = new ByteArrayInputStream(baos.toByteArray());
                splitStreams[i] = splitStream;
            } catch (RuntimeException e) {
                System.out.printf("splitBufferedImageHorizontally ERROR.. originalImage : " + originalImage);
                logger.error("Error : " + e);
            }

        }
        return splitStreams;
    }

    public static void saveInputStreamsAsImages(InputStream[] inputStreamList, String outputDirectory, String baseFileName) throws IOException {
        int i = 1;
        for (InputStream inputStream : inputStreamList) {
            BufferedImage image = ImageIO.read(inputStream);
            String fileName = baseFileName + "_" + i + ".png";
            File outputFile = new File(outputDirectory, fileName);
            ImageIO.write(image, "png", outputFile);
            i++;
        }
    }
    public static void saveInputStreamAsImages(InputStream inputStream, String outputDirectory, String baseFileName) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        String fileName = baseFileName + ".png";
        File outputFile = new File(outputDirectory, fileName);
        ImageIO.write(image, "png", outputFile);
    }
}