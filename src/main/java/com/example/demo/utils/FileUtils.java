package com.example.demo.utils;

import com.example.demo.model.InfoType;
import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<Panel> getPanelsList() {
        List<Panel> panelList = new ArrayList<>();
        RegexFileFilter regexFileFilter = new RegexFileFilter("ttyACM*");
        File dir = new File("/dev");
        if (OSValidator.isWindows()) {
            dir = new File("D:\\dev\\");
        }
        if (!dir.isDirectory()) throw new IllegalStateException("Unknown Directory!");
        for (File file : dir.listFiles(regexFileFilter)) {
            String panel_id = file.getName().substring(6);
            panelList.add(new Panel(Long.parseLong(panel_id), file.getName(), "30x118", PanelStatus.ACTIVE, null));
        }
        return panelList;
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

    public static InputStream asInputStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static List<InputStream> splitImageVertically(InputStream inputStream, int n) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputStream);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int partWidth = width / n;

        List<InputStream> inputStreamList = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            int x = i * partWidth;
            BufferedImage partImage = originalImage.getSubimage(x, 0, partWidth, height);
            BufferedImage scaledImage = new BufferedImage(partWidth, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = scaledImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(partImage, 0, 0, partWidth, height, null);
            graphics.dispose();
            InputStream partStream = asInputStream(scaledImage);
            inputStreamList.add(partStream);
        }
        return inputStreamList;
    }

    public static void saveInputStreamsAsImages(List<InputStream> inputStreamList, String outputDirectory, String baseFileName) throws IOException {
        for (int i = 0; i < inputStreamList.size(); i++) {
            InputStream inputStream = inputStreamList.get(i);
            BufferedImage image = ImageIO.read(inputStream);
            String fileName = baseFileName + "_" + i + ".png";
            File outputFile = new File(outputDirectory, fileName);
            ImageIO.write(image, "png", outputFile);
        }
    }
}