package cn.gofree.lingxi.eventbridge.listener;

import cn.gofree.lingxi.eventbridge.config.TargetRunnerConfig;

public interface TargetRunnerListener {

    /**
     * Call when add new target runner to runtimer.
     *
     * @param targetRunnerConfig
     */
    void onAddTargetRunner(TargetRunnerConfig targetRunnerConfig);

    void onUpdateTargetRunner(TargetRunnerConfig targetRunnerConfig);

    void onDeleteTargetRunner(TargetRunnerConfig targetRunnerConfig);

}
