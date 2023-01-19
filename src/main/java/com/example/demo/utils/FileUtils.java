package com.example.demo.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static List<String> getPanelsList() {
        List<String> stringList = new ArrayList<>();
        File dir = new File("/dev");
        if(!dir.isDirectory()) throw new IllegalStateException("Unknown Directory!");
        for(File file : dir.listFiles(new RegexFileFilter("ttyACM*"))) {
            stringList.add(file.getName());
        }
        return stringList;
    }
}

