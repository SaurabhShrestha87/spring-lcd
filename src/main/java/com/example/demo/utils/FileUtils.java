package com.example.demo.utils;

import com.example.demo.model.Panel;
import com.example.demo.model.PanelStatus;

import java.io.File;
import java.util.ArrayList;
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
}