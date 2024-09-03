package cn.gofree.lingxi.eventcenter.config;

import cn.gofree.lingxi.eventcenter.constant.RuntimeConfigDefine;
import cn.gofree.lingxi.eventcenter.entity.SubscribeRunnerKeys;
import cn.gofree.lingxi.eventcenter.enums.ErrorToleranceEnum;
import cn.gofree.lingxi.eventcenter.enums.PushRetryStrategyEnum;
import cn.gofree.lingxi.eventcenter.runtime.RetryStrategy;
import cn.gofree.lingxi.eventcenter.runtime.RunOptions;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
public class TargetRunnerConfig implements Serializable {
    private String name;
    private List<Map<String, String>> components;

    private RunOptions runOptions = RunOptions.builder().errorTolerance(ErrorToleranceEnum.ALL).retryStrategy(RetryStrategy.builder().pushRetryStrategyEnum(PushRetryStrategyEnum.EXPONENTIAL_DECAY_RETRY).build()).build();

    @Override
    public boolean equals(Object o){
        if (this == o)
            return true;
        if (o == null|| getClass() != o.getClass())
        {
            return false;
        }
        TargetRunnerConfig config = (TargetRunnerConfig) o;
        return Objects.equals(name,config.name) && isEqualsComponents(components, config.getComponents());
    }
    @Override
    public int hashCode() {
        return Objects.hash(name, components);
    }

    @Override
    public String toString() {
        return "TargetRunnerConfig{" +
                "name='" + name + '\'' +
                ", components=" + components +
                ", runOptions=" + runOptions +
                '}';
    }

    private boolean isEqualsComponents(List<Map<String, String>> source, List<Map<String, String>> target) {
        if (source == null || target == null) {
            return source == target;
        }

        if (source.isEmpty() || target.isEmpty()) {
            return source.isEmpty() && target.isEmpty();
        }

        if (source.size() != target.size()) {
            return false;
        }
        for (int index = 0; index < source.size(); index++) {
            Map<String, String> sourceComponent = source.get(index);
            Map<String, String> targetComponent = target.get(index);
            if (sourceComponent.size() != targetComponent.size()) {
                return false;
            }
            for (Map.Entry<String, String> entry : sourceComponent.entrySet()) {
                String element = targetComponent.get(entry.getKey());
                if (element == null && entry.getValue() == null) {
                    return true;
                } else {
                    return element.equals(entry.getValue());
                }
            }
        }
        return true;
    }

    public String getEventBusName() {
        return components.get(0).get(RuntimeConfigDefine.TARGET_RUNNER_KEY);
    }

    public String getAccountId() {
        return components.get(0).get(RuntimeConfigDefine.ACCOUNT_ID);
    }

    public SubscribeRunnerKeys getSubscribeRunnerKeys() {
        SubscribeRunnerKeys subscribeRunnerKeys = new SubscribeRunnerKeys();
        subscribeRunnerKeys.setRunnerName(this.getName());
        subscribeRunnerKeys.setAccountId(this.getAccountId());
        subscribeRunnerKeys.setEventBusName(this.getEventBusName());
        return subscribeRunnerKeys;
    }
}
