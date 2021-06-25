package zk.logcollector.plugins.input;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import zk.logcollector.plugin.api.Input;
import zk.logcollector.plugin.api.LogRecord;
import zk.logcollector.plugin.api.PluginParameter;
import zk.logcollector.plugins.LogRecordQueue;
import zk.logcollector.utils.FileUtils;

@NoArgsConstructor
@Slf4j
@SuppressWarnings("FieldMayBeFinal,FieldCanBeLocal")
public class FileInput implements Input {

    private transient LogRecordQueue logRecordQueue;

    // some getters is just for unit tests

    @Getter
    private transient Set<Path> detectedFiles;

    private transient ScheduledExecutorService scheduledExecutorService;
    private transient ExecutorService executorService;

    @PluginParameter(value = "paths")
    @Setter
    private List<String> filePatterns;

    @Setter
    @PluginParameter(required = false)
    private boolean fromBeginning = false;

    @Setter
    @Getter
    @PluginParameter(required = false)
    private long newFileDetectInterval = 10000L;

    @Setter
    @Getter
    @PluginParameter(required = false)
    private long newLineDetectInterval = 10000L;

    @Setter
    @PluginParameter(required = false)
    private long delay = 1000L;

    private transient boolean running = false;

    @Override
    public LogRecord emit() {
        return logRecordQueue.poll();
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        detectedFiles = new HashSet<>();
        logRecordQueue = new LogRecordQueue();
        executorService = Executors.newCachedThreadPool();
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this::watch, 0, newFileDetectInterval, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        scheduledExecutorService.shutdown();
        scheduledExecutorService = null;
        executorService.shutdown();
        executorService = null;
        logRecordQueue = null;
    }

    private void watch() {
        Set<Path> newDetectedFiles = detectNewFiles();
        if (newDetectedFiles.isEmpty()) {
            return;
        }
        for (Path path : newDetectedFiles) {
            Tailer tailer = new Tailer(path.toFile(), new MyTailerListener(logRecordQueue), newLineDetectInterval);
            executorService.execute(tailer);
        }
    }

    private Set<Path> detectNewFiles() {
        Set<Path> newDetectedFiles = new HashSet<>();
        for (String globPattern : filePatterns) {
            try {
                List<Path> paths = FileUtils.glob(globPattern);
                for (Path path : paths) {
                    if (detectedFiles.contains(path)) {
                        continue;
                    }
                    newDetectedFiles.add(path);
                    detectedFiles.add(path);
                }
            } catch (IOException ioE) {
                log.error("Failed to load glob pattern {}", filePatterns, ioE);
            }
        }
        return newDetectedFiles;
    }

    static class MyTailerListener extends TailerListenerAdapter {

        private Tailer tailer;

        private final LogRecordQueue logRecordQueue;

        public MyTailerListener(LogRecordQueue logRecordQueue) {
            super();
            this.logRecordQueue = logRecordQueue;
        }

        @Override
        public void init(Tailer tailer) {
            this.tailer = tailer;
        }

        @Override
        public void handle(String line) {
            logRecordQueue.offer(new LogRecord(line));
        }

        @Override
        public void fileNotFound() {
            log.debug("File {} not found", tailer.getClass().getName());
        }
    }

}
