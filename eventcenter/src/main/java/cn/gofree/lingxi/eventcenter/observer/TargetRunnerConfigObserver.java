package cn.gofree.lingxi.eventcenter.observer;

import cn.gofree.lingxi.eventcenter.config.TargetRunnerConfig;
import cn.gofree.lingxi.eventcenter.entity.SubscribeRunnerKeys;
import cn.gofree.lingxi.eventcenter.listener.TargetRunnerListener;

import java.util.Set;

/**
 * 目标运行时配置观察者
 */
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
