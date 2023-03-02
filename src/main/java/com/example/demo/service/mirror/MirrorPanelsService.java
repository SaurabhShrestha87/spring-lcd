package com.example.demo.service.mirror;

import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NoArgsConstructor
public class MirrorPanelsService {
    @Autowired
    LendRepository lendRepository;
    @Autowired
    MirrorLedService mirrorLedService;

    public String stop() {
        return "Todo : stop";
    }

    public String start() {
        StringBuilder log = new StringBuilder();
        List<Lend> runningLends = lendRepository.findAllByTypeAndStatus(DisplayType.MIRROR, LendStatus.RUNNING);
        for (Lend runningLend : runningLends) {
            log.append(doAction(runningLend.getProfile()));
        }
        return log.toString();
    }

    private String doAction(Profile profile) {
        StringBuilder log = new StringBuilder();
        List<Information> profileInformation = profile.getInformation();
        for (Information information : profileInformation) {
            log.append(mirrorLedService.execute(information));
        }
        return log.toString();
    }
}
