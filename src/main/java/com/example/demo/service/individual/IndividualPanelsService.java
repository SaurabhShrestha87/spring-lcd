package com.example.demo.service.individual;

import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@NoArgsConstructor
public class IndividualPanelsService {
    private static final Logger logger = LoggerFactory.getLogger(IndividualPanelsService.class);
    private final Map<String, Thread> threadMap = new ConcurrentHashMap<>();
    private final Map<String, Boolean> threadStatusMap = new ConcurrentHashMap<>();
    public ThreadState threadState = ThreadState.READY;
    @Autowired
    LendRepository lendRepository;
    @Autowired
    PanelRepository panelRepository;
    @Autowired
    IndividualLedService individualLedService;
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
        return threadMap.toString();
    }

    @PostConstruct
    public void createThreads() {
        List<Panel> activePanels = panelRepository.findAllByStatus(PanelStatus.ACTIVE);
        for (Panel activePanel : activePanels) {
            String threadName = activePanel.getDevice();
            Runnable runnable = () -> {
                List<Lend> runningLends = lendRepository.findAllByPanelIdAndStatus(activePanel.getId(), LendStatus.RUNNING);
                for (Lend runningLend : runningLends) {
                    doAction(runningLend.getPanel(), runningLend.getProfile());
                }
            };
            Thread thread = new Thread(runnable);
            threadMap.put(threadName, thread);
            threadStatusMap.put(threadName, false);
        }
    }

    private void doAction(Panel panel, Profile profile) {
        logger.info("Looping profile : " + profile.getName() + " at panel " + panel.getDevice());
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
            String data = individualLedService.executeSync(information, panel);
            logger.info("Finish sending information : " + information.getName() + " at panel : " + panel.getDevice());
            logger.info("data : " + data);
        }
    }

    public void startRunningThread(String threadName) {
        Thread thread = threadMap.get(threadName);
        if (thread != null && !threadStatusMap.get(threadName)) {
            thread.start();
            threadStatusMap.put(threadName, true);
        }
    }

    public void pauseThread(String threadName) {
        Thread thread = threadMap.get(threadName);
        if (thread != null) {
            threadStatusMap.put(threadName, true);
        }
    }

    public void resumeThread(String threadName) {
        Thread thread = threadMap.get(threadName);
        if (thread != null) {
            threadStatusMap.put(threadName, false);
        }
    }

    public void stopThread(String threadName) {
        Thread thread = threadMap.get(threadName);
        if (thread != null) {
            thread.interrupt();
            threadMap.remove(threadName);
            threadStatusMap.remove(threadName);
        }
    }

    public void startAllThreads() {
        if (threadState == ThreadState.STOPPED) {
            createThreads();
        }
        for (String threadName : threadMap.keySet()) {
            startRunningThread(threadName);
        }
        threadState = ThreadState.RUNNING;
    }

    public void pauseAllThreads() {
        for (Thread thread : threadMap.values()) {
            if (thread != null) {
                threadStatusMap.put(thread.getName(), true);
            }
        }
        threadState = ThreadState.PAUSED;
    }

    public void resumeAllThreads() {
        for (Thread thread : threadMap.values()) {
            if (thread != null) {
                threadStatusMap.put(thread.getName(), false);
            }
        }
        threadState = ThreadState.RUNNING;
    }

    public void stopAllThreads() {
        for (Thread thread : threadMap.values()) {
            thread.interrupt();
        }
        threadMap.clear();
        threadStatusMap.clear();
        threadState = ThreadState.STOPPED;
    }

}
