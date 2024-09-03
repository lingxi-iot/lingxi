package cn.gofree.lingxi.eventcenter.connector;

import io.openmessaging.KeyValue;
import io.openmessaging.connector.api.component.task.Task;
import io.openmessaging.connector.api.component.task.sink.SinkConnector;

import java.util.List;

public class AbstractProductSinkConnector extends SinkConnector {

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public List<KeyValue> taskConfigs(int i) {
        return null;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return null;
    }

    @Override
    public void validate(KeyValue keyValue) {

    }

    @Override
    public void init(KeyValue keyValue) {

    }

    @Override
    public void stop() {

    }
}
