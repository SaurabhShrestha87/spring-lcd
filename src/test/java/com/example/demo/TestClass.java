package com.example.demo;

import com.example.demo.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);

    @Test
    void extractSingleInputImageToMultipleTest() {
        File file = new File("D:\\upload\\frame09.png");
        try {
            InputStream[] list = FileUtils.splitInputStreamHorizontally(new FileInputStream(file), 3);
            FileUtils.saveInputStreamsAsImages(list, "D:\\upload\\split", "ArrowSplit_new");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
