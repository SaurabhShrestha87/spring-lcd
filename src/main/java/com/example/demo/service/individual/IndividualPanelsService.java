package com.example.demo.service.individual;

import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
import com.example.demo.service.SerialCommunication;
import com.example.demo.utils.FileUtils;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IndividualPanelsService {
    private static final Logger logger = LoggerFactory.getLogger(IndividualPanelsService.class);
    private final Map<Integer, Boolean> threadStatusMap = new ConcurrentHashMap<>();
    public ThreadState threadState = ThreadState.READY;
    @Autowired
    LendRepository lendRepository;
    @Autowired
    PanelRepository panelRepository;
    @Autowired
    IndividualLedService individualLedService;
    @Autowired
    SerialCommunication serialCommunication;
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

    public String getData() {
        return threadState.toString();
    }

    public void createThreads() {
        int panelCount = serialCommunication.getSize();
        try {
            Thread[] threads = new Thread[panelCount];
            for (int i = 0; i < panelCount; i++) {
                int finalIndex = i;
                final int index = i;
                Runnable runnable = () -> {
                    List<Lend> runningLends = lendRepository.findAllByPanelIdAndStatus(serialCommunication.panelIdFromIndex(finalIndex), LendStatus.RUNNING);
                    for (Lend runningLend : runningLends) {
                        doAction(runningLend.getPanel(), runningLend.getProfile());
                    }
                };
                threads[index] = new Thread(runnable);
                threadStatusMap.put(i, false);
                threads[i].start();
            }
            // Wait for all threads to complete before returning
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doAction(Panel panel, Profile profile) {
        List<Information> profileInformation = profile.getInformation();
        for (Information information : profileInformation) {
            if (threadStatusMap.get(panel.getDevice())) { // if thread status is paused... Halt the loop
                try {
                    Thread.sleep(1000); // sleep for 1 second if thread is paused
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            individualLedService.executeSync(information, panel);
        }
    }

//    public void startRunningThread(String threadName) {
//        Thread thread = threadMap.get(threadName);
//        if (thread != null && !threadStatusMap.get(threadName)) {
//            thread.start();
//            threadStatusMap.put(threadName, true);
//        }
//    }
//
//    public void pauseThread(String threadName) {
//        Thread thread = threadMap.get(threadName);
//        if (thread != null) {
//            threadStatusMap.put(threadName, true);
//        }
//    }
//
//    public void resumeThread(String threadName) {
//        Thread thread = threadMap.get(threadName);
//        if (thread != null) {
//            threadStatusMap.put(threadName, false);
//        }
//    }
//
//    public void stopThread(String threadName) {
//        Thread thread = threadMap.get(threadName);
//        if (thread != null) {
//            thread.interrupt();
//            threadMap.remove(threadName);
//            threadStatusMap.remove(threadName);
//        }
//    }

    public void startAllThreads() {
        createThreads();
    }

}
