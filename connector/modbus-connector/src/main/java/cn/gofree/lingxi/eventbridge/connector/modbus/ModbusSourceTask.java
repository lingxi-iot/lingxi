package cn.gofree.lingxi.eventbridge.connector.modbus;

import io.openmessaging.KeyValue;
import io.openmessaging.connector.api.component.task.source.SourceTask;
import io.openmessaging.connector.api.data.ConnectRecord;

import java.util.List;

public class ModbusSourceTask extends SourceTask {
    @Override
    public List<ConnectRecord> poll() throws InterruptedException {
        return null;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

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
