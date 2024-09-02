package cn.gofree.lingxi.eventbridge.observer;

import cn.gofree.lingxi.eventbridge.config.TargetRunnerConfig;
import cn.gofree.lingxi.eventbridge.exception.EventBridgeException;
import cn.gofree.lingxi.eventbridge.util.ThreadUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
//@Component
public class TargetRunnerConfigOnFileObserver extends AbstractTargetRunnerConfigObserver {

    private String pathName;

    public static final String DEFAULT_TARGET_RUNNER_CONFIG_FILE_NAME = "target-runner.json";

    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
            ThreadUtils.newThreadFactory("TargetRunnerConfigOnFileObserver", false));

    public TargetRunnerConfigOnFileObserver(String pathName) {
        this.pathName = pathName;
        super.getTargetRunnerConfig().addAll(getLatestTargetRunnerConfig());
        this.addListen(pathName, this);
    }

    public TargetRunnerConfigOnFileObserver() {
        this.pathName = getConfigFilePath();
        this.pathName = StringUtils.replace(this.pathName,"%20"," ");
        super.getTargetRunnerConfig().addAll(getLatestTargetRunnerConfig());
        this.addListen(pathName, this);
    }

    @Override
    public Set<TargetRunnerConfig> getLatestTargetRunnerConfig() {
        String config = null;
        try {
            File file = new File(pathName);
            config = FileUtils.readFileToString(file, "UTF-8");
            Type workerConfigType = new TypeToken<HashSet<TargetRunnerConfig>>() {}.getType();
            Set<TargetRunnerConfig> taskConfigList = new Gson().fromJson(config, workerConfigType);
            return taskConfigList;
        } catch (IOException e) {
            throw new EventBridgeException("Load component properties failed.", e);
        } catch (Throwable e) {
            log.error("fail to parse config={} from file={}", config, pathName);
            throw e;
        }
    }

    public void addListen(String pathName, TargetRunnerConfigOnFileObserver pusherConfigOnFileService) {
        log.info("Watching task file changing:{}", pathName);
        int index = pathName.lastIndexOf("/");
        String filePath = pathName.substring(0, index);
        String fileName = pathName.substring(index + 1);
        service.scheduleAtFixedRate(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get(filePath);
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                WatchKey watchKey;
                while (true) {
                    watchKey = watchService.take();
                    if (watchKey != null && !watchKey.pollEvents()
                            .isEmpty()) {
                        log.info("Watched the file changed events.");
                        pusherConfigOnFileService.diff();
                    }
                    watchKey.reset();
                }
            } catch (Throwable e) {
                log.error("Watch file failed.", e);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }


    private String getConfigFilePath() {
        return this.getClass().getClassLoader().getResource(DEFAULT_TARGET_RUNNER_CONFIG_FILE_NAME).getPath();
    }
}
