package com.example.demo.service.contigous;

import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
import lombok.NoArgsConstructor;
import org.bytedeco.opencv.presets.opencv_core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@NoArgsConstructor
public class ContigousPanelsService {
    private static final Logger logger = LoggerFactory.getLogger(ContigousPanelsService.class);
    public ThreadState threadState = ThreadState.READY;
    @Autowired
    LendRepository lendRepository;
    @Autowired
    PanelRepository panelRepository;
    @Autowired
    ContigousLedService contigousLedService;
    String logs = "";
    int logCOUNT = 0;

    private void makeLogs(String log) {
        logs = logs + log + "\n";
        logCOUNT++;
        if (logCOUNT > 100) {
            logs = "";
        }
    }

    public String getLogs() {
        return logs;
    }

    public String stop() {
        return "Todo : stop";
    }
    public String start() {
        StringBuilder log = new StringBuilder();
        List<Lend> runningLends = lendRepository.findAllByTypeAndStatus(DisplayType.CONTIGUOUS, LendStatus.RUNNING);
        for (Lend runningLend : runningLends) {
            log.append(doAction(runningLend.getProfile()));
        }
        return log.toString();
    }

    private String doAction(Profile profile) {
        StringBuilder log = new StringBuilder();
        List<Information> profileInformation = profile.getInformation();
        for (Information information : profileInformation) {
            log.append(contigousLedService.execute(information));
        }
        return log.toString();
    }
}
