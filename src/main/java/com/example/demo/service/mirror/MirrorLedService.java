package com.example.demo.service.mirror;

import com.example.demo.model.InfoType;
import com.example.demo.model.Information;
import com.example.demo.model.PanelStatus;
import com.example.demo.repository.PanelRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Runnable to send a timestamp to the Arduino board to demonstrate the echo function.
 */
@Service
@NoArgsConstructor
@Getter
@Setter
public class MirrorLedService {
    //    private static final Logger logger = LoggerFactory.getLogger(LedService.class);
    private static final int INTERVAL_SEND_SECONDS = 33;
    @Autowired
    PanelRepository panelRepository;
    @Autowired
    MirrorDecodingService decodingService;

    @PostConstruct
    public void init() {
        // initialize your monitor here, instance of someService is already injected by this time.
    }

    public String execute(Information information) {
        decodingService = new MirrorDecodingService();
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