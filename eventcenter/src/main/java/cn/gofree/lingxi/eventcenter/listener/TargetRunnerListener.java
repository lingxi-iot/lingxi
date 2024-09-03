package cn.gofree.lingxi.eventcenter.listener;

import cn.gofree.lingxi.eventcenter.config.TargetRunnerConfig;

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
