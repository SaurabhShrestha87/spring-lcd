package com.example.demo.utils;

import com.example.demo.model.InfoType;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static List<Panel> getPanelsList() {
        List<Panel> stringList = new ArrayList<>();
        RegexFileFilter regexFileFilter = new RegexFileFilter("ttyACM*");
        File dir = new File("/dev");
        if (OSValidator.isWindows()) {
            dir = new File("D:\\upload\\");
            regexFileFilter = new RegexFileFilter("frame*");
        }
        if (!dir.isDirectory()) throw new IllegalStateException("Unknown Directory!");
        for (File file : dir.listFiles(regexFileFilter)) {
            String panel_id = file.getName().substring(6);
            stringList.add(new Panel(Long.parseLong(panel_id), file.getName(), "30x118", PanelStatus.ACTIVE, null));
        }
        return stringList;
    }

    public static String createFileDir(String fileName) {
        return OSValidator.isWindows() ? "D:\\upload\\" + fileName : "/home/pi/Application/Uploads/" + fileName;
    }

    public static String createGifFramesFolderDir(String fileName) {
        String folderName = removeFileExtension(fileName, false);
        return OSValidator.isWindows() ? "D:\\upload\\" + folderName : "/home/pi/Application/Uploads/" + folderName;
    }

    public static String createFrameFromCount(String folderName, int count) {
        return (OSValidator.isWindows() ? folderName + "\\Frame_" + count : folderName + "/Frame_" + count) + ".png";
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

    public static String removeFileExtension(String filename, boolean removeAllExtensions) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        String extPattern = "(?<!^)[.]" + (removeAllExtensions ? ".*" : "[^.]*$");
        return filename.replaceAll(extPattern, "");
    }

    public static InputStream readImageToInputStream(String filePath) {
        InputStream is = null;
        try {
            System.out.println("filePath : " + filePath);
            File input_file = new File(filePath);
            // Reading input file
            BufferedImage image = ImageIO.read(input_file);
            System.out.println("Reading complete.");
            System.out.println("\n");
            System.out.println("image.getData : " + image.getData());
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(image, "png", os); // Passing: ​(RenderedImage im, String formatName, OutputStream output)
            is = new ByteArrayInputStream(os.toByteArray());
            System.out.println("\n");
            System.out.println("OUTPUT STREAM : " + os);
            System.out.println("\n");
            System.out.println("readAllBytes : " + Arrays.toString(is.readAllBytes()));
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }
        return is;
    }
}