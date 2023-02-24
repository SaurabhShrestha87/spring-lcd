package com.example.demo.service;

import com.example.demo.controller.HomeController;
import com.example.demo.model.*;
import com.example.demo.repository.LendRepository;
import com.example.demo.repository.PanelRepository;
import com.example.demo.utils.RunShellCommandFromJava;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@NoArgsConstructor
public class MainService {
    private static final Logger logger = LoggerFactory.getLogger(MainService.class);
    @Autowired
    LendRepository lendRepository;
    @Autowired
    PanelRepository panelRepository;
    @Autowired
    LedService ledService;
    @Autowired
    ThreadService threadService;
    MainServiceCallback callback;
    String logs = "";
    int logCOUNT = 0;
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    private Map<String, Thread> threads = new ConcurrentHashMap<>();

    public void init() {
        List<Panel> activePanels = panelRepository.findAllByStatus(PanelStatus.ACTIVE);
        activePanels.stream().map(activePanel -> lendRepository.findAllByPanelIdAndStatus(activePanel.getId(), LendStatus.RUNNING)).forEach(runningLends -> runningLends.forEach(runningLend -> doAction(runningLend.getPanel(), runningLend.getProfile())));
    }

    public void setCallback(MainServiceCallback callback) {
        this.callback = callback;
    }

    @Async
    private void doAction(Panel panel, Profile profile) {
        for (Information information : profile.getInformation()) {
            callback.currentInformationOnPanel(information.getName(), panel.getName());
            RunShellCommandFromJava.ThreadCompleteListener threadCompleteListener = (interrupted, log) -> {
                System.out.println("ThreadCompleteListener .. interrupted : " + interrupted + " log : " + log);
                makeLogs(log);
                executorService.shutdown();
            };
            executorService = Executors.newFixedThreadPool(3);
            executorService.submit(() -> ledService.execute(information, panel, threadCompleteListener));
        }
    }


    public void pauseLoop() {
        executorService.shutdown();
    }


    public void startLoop() {
        init();
    }

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

    public void createThread(String name, Runnable runnable) {
        Thread thread = new Thread(runnable, name);
        threads.put(name, thread);
        thread.start();
    }

    public void pauseThread(String name) {
        Thread thread = threads.get(name);
        if (thread != null) {
            thread.suspend();
        }
    }

    public void resumeThread(String name) {
        Thread thread = threads.get(name);
        if (thread != null) {
            thread.resume();
        }
    }

    public void stopThread(String name) {
        Thread thread = threads.get(name);
        if (thread != null) {
            thread.interrupt();
            threads.remove(name);
        }
    }

    public void restartThread(String name) {
        Thread thread = threads.get(name);
        if (thread != null) {
            thread.interrupt();
            threads.remove(name);
            Thread newThread = new Thread(thread);
            threads.put(name, newThread);
            newThread.start();
        }
    }

    public interface MainServiceCallback {
        void currentInformationOnPanel(String infoString, String panelString);
    }
}
