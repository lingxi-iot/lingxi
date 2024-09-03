package cn.gofree.eventbridge.connector.http;

import io.openmessaging.KeyValue;
import io.openmessaging.connector.api.component.task.Task;
import io.openmessaging.connector.api.component.task.sink.SinkConnector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class HttpSinkConnector extends SinkConnector {
    @Override
    public void pause() {
        log.info("================= public void pause() =========");
    }

    @Override
    public void resume() {
        log.info("================= public void resume() =========");
    }

    @Override
    public List<KeyValue> taskConfigs(int i) {
        log.info("================= public void taskConfigs() =========");
        return null;
    }

    @Override
    public Class<? extends Task> taskClass() {
        log.info("================= public void taskClass() =========");
        return null;
    }

    @Override
    public void validate(KeyValue keyValue) {
        log.info("================= public void validate() =========");
    }

    @Override
    public void init(KeyValue keyValue) {
        log.info("================= public void init() =========");
    }

    @Override
    public void stop() {
        log.info("================= public void stop() =========");
    }
}
