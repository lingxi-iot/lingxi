package cn.gofree.lingxi.eventbridge.connector.support;


import cn.gofree.lingxi.eventbridge.connector.AbstractProductSinkConnector;
import io.openmessaging.KeyValue;
import io.openmessaging.connector.api.component.task.Task;
import io.openmessaging.connector.api.component.task.sink.SinkConnector;

import java.util.List;

public class ModbusSinkConnector extends SinkConnector {
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
        return ModbusSinkTask.class;
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
