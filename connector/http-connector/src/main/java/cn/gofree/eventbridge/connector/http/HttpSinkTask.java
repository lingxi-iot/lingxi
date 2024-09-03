package cn.gofree.eventbridge.connector.http;

import io.openmessaging.KeyValue;
import io.openmessaging.connector.api.component.task.sink.SinkTask;
import io.openmessaging.connector.api.data.ConnectRecord;
import io.openmessaging.connector.api.errors.ConnectException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class HttpSinkTask extends SinkTask {

    @Override
    public void put(List<ConnectRecord> list) throws ConnectException {
        log.info("================= public void put() =========");
    }

    @Override
    public void pause() {
        log.info("================= public void pause() =========");
    }

    @Override
    public void resume() {
        log.info("================= public void resume() =========");
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
