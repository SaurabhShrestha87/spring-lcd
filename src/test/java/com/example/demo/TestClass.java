package com.example.demo;

import com.example.demo.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);

    @Test
    void extractSingleInputImageToMultipleTest() {
        File file = new File("D:\\upload\\big_2_.PNG");
        try {
            InputStream[] list = FileUtils.splitInputStreamHorizontally(new FileInputStream(file), 3);
            FileUtils.saveInputStreamsAsImages(list, "D:\\upload\\split", "ArrowSplit_new");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    @Test
    void extractSingleInputImageToMultipleTest2() {
        File file = new File("D:\\upload\\big_2_.PNG");
        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter writer = new PrintWriter(System.out);
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }
            writer.flush();
        } catch (IOException e) {
            logger.error("Error : " + e);
        }
    }
}
