package com.example.demo;

import com.example.demo.model.DeviceType;
import com.example.demo.service.SerialCommunication;
import com.example.demo.utils.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class TestClass {
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);

    public static void main(String[] args) throws IOException {

    }

    @Test
    public void readFileTest() {
        SerialCommunication serialCommunication = new SerialCommunication(DeviceType.DEVICE0);
        File file =  new File("D:\\upload\\frame09.png");
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            serialCommunication.runSerial(is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
