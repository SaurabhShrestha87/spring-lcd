package com.example.demo.service.contigous;

import com.example.demo.model.*;
import com.example.demo.model.draw.Shape;
import com.example.demo.repository.PanelRepository;
import com.example.demo.utils.RunShellCommandFromJava;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
@Service
@NoArgsConstructor
@Getter
@Setter
public class ContigousLedService {
    //    private static final Logger logger = LoggerFactory.getLogger(LedService.class);
    private static final int INTERVAL_SEND_SECONDS = 33;
    @Autowired
    PanelRepository panelRepository;
    @Autowired
    ContigousDecodingService decodingService;

    @PostConstruct
    public void init() {
        // initialize your monitor here, instance of someService is already injected by this time.
    }
    public String execute(Information information) {
        decodingService = new ContigousDecodingService();
        if (information.getType() == InfoType.VIDEO) {
            return decodingService.decodeVideo(information.getUrl(), Long.valueOf(information.getDuration()), panelRepository.findAllByStatus(PanelStatus.ACTIVE));
        } else if (information.getType() == InfoType.GIF) {
            return decodingService.decodeGif(information.getUrl(), Long.valueOf(information.getDuration()), panelRepository.findAllByStatus(PanelStatus.ACTIVE));
        } else if (information.getType() == InfoType.IMAGE) {
            return decodingService.decodeImage(information.getUrl(), Long.valueOf(information.getDuration()), panelRepository.findAllByStatus(PanelStatus.ACTIVE));
        } else {
            return "Some Error Occurred during executeSync";
        }
    }

    public void clearAllScreens() {
        decodingService.clearScreen();
    }
}