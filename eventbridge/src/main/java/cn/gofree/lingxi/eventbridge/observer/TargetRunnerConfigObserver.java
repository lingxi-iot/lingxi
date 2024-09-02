package cn.gofree.lingxi.eventbridge.observer;

import cn.gofree.lingxi.eventbridge.config.TargetRunnerConfig;
import cn.gofree.lingxi.eventbridge.entity.SubscribeRunnerKeys;
import cn.gofree.lingxi.eventbridge.listener.TargetRunnerListener;

import java.util.Set;

public interface TargetRunnerConfigObserver {

    /**
     * Get the target runner config of runtimer.
     * @return
     */
    Set<TargetRunnerConfig> getTargetRunnerConfig();

    /**
     * 根据事件总线名称获取运行
     * Get the target runner key which relevant as event bus name
     * @return
     */
    Set<SubscribeRunnerKeys> getSubscribeRunnerKeys();

    /**
     * Register a listener to listen all config update operations.
     *
     * @param listener
     */
    void registerListener(TargetRunnerListener listener);
}
