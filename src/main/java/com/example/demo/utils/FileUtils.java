package com.example.demo.utils;

import com.example.demo.model.InfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Retrieves a list of panel names from the system.
     *
     * @return an array of panel names
     * @throws IllegalStateException if the directory is unknown or invalid
     */
    public static String[] getPanelsList() {
        RegexFileFilter regexFileFilter = new RegexFileFilter("ttyACM*");
        File dir = new File("/dev");
        if (OSValidator.isWindows()) {
            dir = new File("D:\\dev\\");
        }
        if (!dir.isDirectory()) throw new IllegalStateException("Unknown Directory!");
        File[] listFiles = dir.listFiles(regexFileFilter);
        String[] panelList = listFiles != null ? new String[listFiles.length] : new String[0];
        for (int i = 0; i < Objects.requireNonNull(listFiles).length; i++) {
            File file = listFiles[i];
            panelList[i] = file.getName();
        }
        Arrays.sort(panelList, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return extractInt(s1) - extractInt(s2);
            }

            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                // return 0 if no digits found
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });
        return panelList;
    }

    /**
     * Creates a file directory based on the operating system.
     *
     * @param fileName the name of the file
     * @return the file directory
     */
    public static String createFileDir(String fileName) {
        return OSValidator.isWindows() ? "D:\\upload\\" + fileName : "/home/mte/Application/Uploads/" + fileName;
    }

    /**
     * Determines the type of the file based on its extension.
     *
     * @param fileName the name of the file
     * @return the file type
     */
    public static InfoType getFileType(String fileName) {
        if (fileName.endsWith("gif") || fileName.endsWith("GIF")) {
            return InfoType.GIF;
        }
        if (fileName.endsWith("png")) {
            return InfoType.IMAGE;
        }
        return InfoType.VIDEO;
    }

    /**
     * Converts an input stream to a file.
     *
     * @param initialStream the initial input stream to be converted
     * @param COUNT         the count value used for generating the file name
     * @throws IOException if an I/O error occurs
     */
    public static void inputStreamToFIle(InputStream initialStream, long COUNT) throws IOException {
        File targetFile = new File("D:\\upload\\output\\VideoFRAME_" + COUNT + ".png");
        OutputStream outStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = initialStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        initialStream.close();
        outStream.close();
    }

    /**
     * Converts a BufferedImage to an InputStream.
     *
     * @param image the BufferedImage to be converted
     * @return the converted InputStream
     * @throws IOException if an I/O error occurs
     */
    public static InputStream asInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Splits an InputStream horizontally into multiple InputStreams.
     *
     * @param inputStream the input stream to be split
     * @param n           the number of splits
     * @return an array of split InputStreams
     */
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

    /**
     * Splits a BufferedImage horizontally into multiple InputStreams.
     *
     * @param originalImage the original BufferedImage to be split
     * @param n             the number of splits
     * @return an array of split InputStreams
     * @throws Exception if an error occurs during the splitting process
     */
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

    /**
     * Saves an array of InputStreams as images to the specified output directory.
     *
     * @param inputStreamList the array of InputStreams to be saved as images
     * @param outputDirectory the directory where the images will be saved
     * @param baseFileName    the base name for the images
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Saves an InputStream as an image to the specified output directory.
     *
     * @param inputStream     the InputStream to be saved as an image
     * @param outputDirectory the directory where the image will be saved
     * @param baseFileName    the base name for the image
     * @throws IOException if an I/O error occurs
     */
    public static void saveInputStreamAsImages(InputStream inputStream, String outputDirectory, String baseFileName) throws IOException {
        BufferedImage image = ImageIO.read(inputStream);
        String fileName = baseFileName + ".png";
        File outputFile = new File(outputDirectory, fileName);
        ImageIO.write(image, "png", outputFile);
    }

    /**
     * Saves data as an image file.
     *
     * @param data      the data to be saved as an image file
     * @param imagePath the path where the image file will be saved
     * @return true if the saving process is successful, false otherwise
     */
    public static Boolean saveToImageFile(String data, String imagePath) {
        // Strip off the data: prefix and Base64-encoded data
        String strippedData = data.replaceFirst("^data:image/\\w+;base64,", "");
        // Decode the Base64-encoded data
        byte[] imageBytes = Base64.getDecoder().decode(strippedData);
        // Save the image to disk
        try (FileOutputStream stream = new FileOutputStream(imagePath)) {
            stream.write(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Extracts the info name from a file name.
     *
     * @param file the file name
     * @return the extracted info name
     */
    public static String getInfoNameFromFileName(String file) {
        String formatedDate;
        // extract the timestamp from the file name
        String timestamp = file.substring(0, file.indexOf('.'));
        // convert the timestamp to a Date object
        Date date = new Date(Long.parseLong(timestamp));
        // format the Date object as "HH_MINUTE_DD_MM_YY"
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm(dd/MMM/yyyy)");
        String formattedDateString = dateFormat.format(date);
        // concatenate "canvas" and the formatted date string
        formatedDate = "canvas_" + formattedDateString;
        return formatedDate;
    }
}