package com.example.demo.utils;

import com.example.demo.model.InfoType;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;
import lombok.SneakyThrows;
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
        List<Panel> stringList = new ArrayList<>();
        RegexFileFilter regexFileFilter = new RegexFileFilter("ttyACM*");
        File dir = new File("/dev");
        if (OSValidator.isWindows()) {
            dir = new File("D:\\dev\\");
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

    public static String readFile(String filePath) {
        String str;
        StringBuilder str2 = new StringBuilder();
        // the following line means the try block takes care of closing the resource
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((str = br.readLine()) != null) {
                str2.append(str2).append(str).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return str2.toString();
    }

    public static String readBufferedData(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", os);
        logger.info("SERIAL DATA : " + os);
        return String.valueOf(os);
    }

    public static InputStream asInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static void copyFileUsingStream(InputStream is, File dest) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}