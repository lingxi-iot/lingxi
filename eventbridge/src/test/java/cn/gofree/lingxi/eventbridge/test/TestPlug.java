package cn.gofree.lingxi.eventbridge.test;


import cn.gofree.lingxi.eventbridge.plugin.PluginLoader;
import cn.gofree.lingxi.eventbridge.plugin.PluginManager;
import io.openmessaging.connector.api.component.task.sink.SinkConnector;
import io.openmessaging.connector.api.component.task.sink.SinkTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.InvocationTargetException;

@RunWith(MockitoJUnitRunner.class)
public class TestPlug {


    private String path;

    @InjectMocks
    private PluginManager pluginManager;

    @Before
    public void testplugin() {
        pluginManager.initPlugin("/Volumes/T7 1/java/lingxi/connector/http-connector/target/http-connector-0.0.1-SNAPSHOT-jar-with-dependencies.jar");
        //pluginManager.initPlugin("/Volumes/T7 1/java/RocketMQCOnnect/rocketmq-connect/connectors/rocketmq-connect-http/target/rocketmq-connect-http-0.0.1-SNAPSHOT-jar-with-dependencies.jar");
        // System.out.println(path);
    }

    @Test
    public void testStart() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String s = pluginManager.getPluginPath();
        System.out.println(s);
        String taskClass = "cn.gofree.eventbridge.connector.httpconnector.HttpSinkTask";
        ClassLoader loader = pluginManager.getPluginClassLoader(taskClass);
        Class taskClazz=((PluginLoader)loader).loadClass(taskClass,false);
        SinkTask sinkTask=(SinkTask) taskClazz.getDeclaredConstructor().newInstance();

        //Class<SinkConnector> sinkConnector= (Class<SinkConnector>) pluginManager.getPluginClassLoader("cn.gofree.lingxi.eventbridge.connector.support.ModbusSinkConnector");
        //System.out.println( sinkConnector.getTypeName());
    }


}
