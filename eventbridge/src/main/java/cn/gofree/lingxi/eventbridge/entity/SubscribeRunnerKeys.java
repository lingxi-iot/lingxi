package cn.gofree.lingxi.eventbridge.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class SubscribeRunnerKeys implements Serializable {
    private String accountId;
    private String runnerName;
    private String eventBusName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscribeRunnerKeys that = (SubscribeRunnerKeys) o;
        return Objects.equals(accountId, that.accountId) && Objects.equals(runnerName, that.runnerName) && Objects.equals(eventBusName, that.eventBusName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, runnerName, eventBusName);
    }

    @Override
    public String toString() {
        return "SubscribeRunnerKeys{" +
                "accountId='" + accountId + '\'' +
                ", runnerName='" + runnerName + '\'' +
                ", eventBusName='" + eventBusName + '\'' +
                '}';
    }
}
