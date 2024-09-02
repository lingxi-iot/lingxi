package cn.gofree.lingxi.eventbridge.observer;

import cn.gofree.lingxi.eventbridge.config.TargetRunnerConfig;
import cn.gofree.lingxi.eventbridge.entity.SubscribeRunnerKeys;
import cn.gofree.lingxi.eventbridge.listener.TargetRunnerListener;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract  class AbstractTargetRunnerConfigObserver implements TargetRunnerConfigObserver {
    private Set<TargetRunnerConfig> targetRunnerConfigs = Sets.newHashSet();

    /**
     * All listeners to trigger while config change.
     */
    private Set<TargetRunnerListener> targetRunnerConfigListeners = new HashSet<>();

    public Set<TargetRunnerConfig> getTargetRunnerConfig() {
        return targetRunnerConfigs;
    }

    @Override
    public Set<SubscribeRunnerKeys> getSubscribeRunnerKeys() {
        if (CollectionUtils.isEmpty(targetRunnerConfigs)) {
            return null;
        }
        return targetRunnerConfigs.stream().map(item -> {
            SubscribeRunnerKeys subscribeRunnerKeys = new SubscribeRunnerKeys();
            subscribeRunnerKeys.setRunnerName(item.getName());
            subscribeRunnerKeys.setAccountId(item.getAccountId());
            subscribeRunnerKeys.setEventBusName(item.getEventBusName());
            return subscribeRunnerKeys;
        }).collect(Collectors.toSet());
    }

    public abstract Set<TargetRunnerConfig> getLatestTargetRunnerConfig();

    @Override
    public void registerListener(TargetRunnerListener listener) {
        this.targetRunnerConfigListeners.add(listener);
    }

    void onAddTargetRunner(TargetRunnerConfig targetRunnerConfig) {
        this.targetRunnerConfigs.add(targetRunnerConfig);
        if (CollectionUtils.isEmpty(this.targetRunnerConfigListeners)) {
            return;
        }
        for (TargetRunnerListener listener : this.targetRunnerConfigListeners) {
            listener.onAddTargetRunner(targetRunnerConfig);
        }
    }

    /**
     * Call when the old target runner updated.
     *
     * @param targetRunnerConfig
     */
    void onUpdateTargetRunner(TargetRunnerConfig targetRunnerConfig) {
        this.targetRunnerConfigs.add(targetRunnerConfig);
        if (CollectionUtils.isEmpty(this.targetRunnerConfigListeners)) {
            return;
        }
        for (TargetRunnerListener listener : this.targetRunnerConfigListeners) {
            listener.onUpdateTargetRunner(targetRunnerConfig);
        }
    }

    /**
     * Call when the old target runner deleted from runtimer.
     *
     * @param targetRunnerConfig
     */
    void onDeleteTargetRunner(TargetRunnerConfig targetRunnerConfig) {
        this.targetRunnerConfigs.remove(targetRunnerConfig);
        if (CollectionUtils.isEmpty(this.targetRunnerConfigListeners)) {
            return;
        }
        for (TargetRunnerListener listener : this.targetRunnerConfigListeners) {
            listener.onDeleteTargetRunner(targetRunnerConfig);
        }
    }

    protected void diff() {
        Map<String, TargetRunnerConfig> lastMap = toMap(this.getTargetRunnerConfig());
        Map<String, TargetRunnerConfig> latestMap = toMap(this.getLatestTargetRunnerConfig());
        lastMap.entrySet().forEach(entry -> {
            TargetRunnerConfig latest = latestMap.get(entry.getKey());
            if (latest == null) {
                this.onDeleteTargetRunner(entry.getValue());
            } else if (!latest.equals(entry.getValue())) {
                this.onUpdateTargetRunner(entry.getValue());
            }
        });

        latestMap.entrySet().forEach(entry -> {
            TargetRunnerConfig latest = lastMap.get(entry.getKey());
            if (latest == null) {
                this.onAddTargetRunner(entry.getValue());
            }
        });
    }

    protected Map<String, TargetRunnerConfig> toMap(Set<TargetRunnerConfig> targetRunnerConfigs) {
        if (targetRunnerConfigs == null || targetRunnerConfigs.isEmpty()) {
            return Maps.newHashMapWithExpectedSize(0);
        }
        Map<String, TargetRunnerConfig> map = Maps.newHashMap();
        targetRunnerConfigs.forEach(entry -> map.put(entry.getName(), entry));
        return map;
    }
}
