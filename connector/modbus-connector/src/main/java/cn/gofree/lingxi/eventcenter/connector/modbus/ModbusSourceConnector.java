package cn.gofree.lingxi.eventcenter.connector.modbus;

import io.openmessaging.KeyValue;
import io.openmessaging.connector.api.component.task.Task;
import io.openmessaging.connector.api.component.task.source.SourceConnector;

import java.util.List;

public class ModbusSourceConnector extends SourceConnector {
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
