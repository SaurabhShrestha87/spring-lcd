package com.example.demo.service.settings;

import com.example.demo.repository.PanelRepository;
import com.example.demo.service.SerialCommunication;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class IdentifyService {
    @Autowired
    SerialCommunication serialCommunication;
    @Autowired
    PanelRepository panelRepository;
    public void startIdentify() throws IOException, InterruptedException {
        int size = serialCommunication.getSize();
        label:
        for (int i = 0; i < size; i++) {
            switch (i) {
                case 0, 3:
                    serialCommunication.runSerial(new FileInputStream(new File("/home/pi/Application/Identify/small_1_.png")), i);
                    break;
                case 1, 4:
                    serialCommunication.runSerial(new FileInputStream(new File("/home/pi/Application/Identify/small_2_.png")), i);
                    break;
                case 2:
                    serialCommunication.runSerial(new FileInputStream(new File("/home/pi/Application/Identify/small_3_.png")), i);
                    break;
                default:
                    break label;
            }
        }
        Thread.sleep(5000);
        serialCommunication.clearAll();
    }
}
