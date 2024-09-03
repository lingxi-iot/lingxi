package cn.gofree.lingxi.eventbridge.listener;

import cn.gofree.lingxi.eventbridge.config.TargetRunnerConfig;
import cn.gofree.lingxi.eventbridge.entity.SubscribeRunnerKeys;
import cn.gofree.lingxi.eventbridge.enums.RefreshTypeEnum;
import io.openmessaging.connector.api.data.ConnectRecord;

import java.util.List;

public abstract class EventSubscriber implements TargetRunnerListener{

    /**
     * Refresh subscriber inner data when runner keys changed
     *
     * @param subscribeRunnerKeys
     * @param refreshTypeEnum
     */
    public abstract void refresh(SubscribeRunnerKeys subscribeRunnerKeys, RefreshTypeEnum refreshTypeEnum);

    /**
     * Pull connect records from store, Blocking method when is empty.
     *
     * @return
     */
    public abstract List<ConnectRecord> pull();

    /**
     * Commit connect records.
     *
     * @param connectRecordList
     */
    public abstract void commit(List<ConnectRecord> connectRecordList);

    /**
     * close resource such as consumer
     */
    public abstract void close();

    /**
     * Put connect record to the eventbus.
     *
     * @param eventBusName
     * @param connectRecord
     * @param delaySec
     */
    public boolean put(String eventBusName, ConnectRecord connectRecord, int delaySec) {
        // convert the eventBusName to Topic ?
        return true;
    }

    /**
     * Call when add new target runner to runtimer.
     *
     * @param targetRunnerConfig
     */
    @Override
    public void onAddTargetRunner(TargetRunnerConfig targetRunnerConfig) {
        this.refresh(targetRunnerConfig.getSubscribeRunnerKeys(), RefreshTypeEnum.ADD);
    }

    /**
     * Call when the old target runner updated.
     *
     * @param targetRunnerConfig
     */
    @Override
    public void onUpdateTargetRunner(TargetRunnerConfig targetRunnerConfig) {
        this.refresh(targetRunnerConfig.getSubscribeRunnerKeys(), RefreshTypeEnum.UPDATE);
    }

    /**
     * Call when the old target runner deleted from runtimer.
     *
     * @param targetRunnerConfig
     */
    @Override
    public void onDeleteTargetRunner(TargetRunnerConfig targetRunnerConfig) {
        this.refresh(targetRunnerConfig.getSubscribeRunnerKeys(), RefreshTypeEnum.DELETE);
    }
}
