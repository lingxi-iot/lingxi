package cn.gofree.lingxi.eventbridge.runtime;

import cn.gofree.lingxi.eventbridge.common.OffsetManager;
import cn.gofree.lingxi.eventbridge.context.EventContext;
import cn.gofree.lingxi.eventbridge.enums.RuntimeState;
import cn.gofree.lingxi.eventbridge.exception.ErrorHandler;
import cn.gofree.lingxi.eventbridge.listener.*;
import cn.gofree.lingxi.eventbridge.observer.TargetRunnerConfigObserver;
import cn.gofree.lingxi.eventbridge.observer.TargetRunnerConfigOnFileObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicReference;


@Component
public class Runtime {
    private static final Logger log = LoggerFactory.getLogger(Runtime.class);
    //原子性的运行状态，应该是整体的
    private AtomicReference<RuntimeState> runtimerState;

    /**
     * 事件上下文
     */
    @Autowired
    private EventContext eventContext;
    /**
     * 运行监听服务
     */
    @Autowired
    private TargetRunnerConfigObserver runnerConfigObserver;
    /**
     * 事件订阅者
     */
    @Autowired
    private EventSubscriber eventSubscriber;
    /**
     * 异常句柄
     */
    @Autowired
    private ErrorHandler errorHandler;
    /**
     * 偏移管理器
     */
    @Autowired
    private OffsetManager offsetManager;
    /**
     * 启停服务
     */
    private static final RuntimeStartAndShutdown RUNTIME_START_AND_SHUTDOWN = new RuntimeStartAndShutdown();



   /* public void init(){
        runnerConfigObserver=new TargetRunnerConfigOnFileObserver();
        eventContext=new EventContext();
        eventSubscriber=new RocketMQEventSubscriber();

    }*/
    @PostConstruct
    public void initAndStart() throws Exception {
        log.info("The runtime is Starting!");

        //获取最新的运行配置信息
        eventContext.initEventContext(runnerConfigObserver.getTargetRunnerConfig());

         //注册监听-上下文
        runnerConfigObserver.registerListener(eventContext);
        //注册监听-事件订阅者
        runnerConfigObserver.registerListener(eventSubscriber);


        EventBusListener eventBusListener = new EventBusListener(eventContext, eventSubscriber, errorHandler);
        EventRuleTransfer eventRuleTransfer = new EventRuleTransfer(eventContext, offsetManager, errorHandler);
        EventTargetTrigger eventTargetPusher = new EventTargetTrigger(eventContext, offsetManager, errorHandler);

        RUNTIME_START_AND_SHUTDOWN.appendStartAndShutdown(eventBusListener);
        RUNTIME_START_AND_SHUTDOWN.appendStartAndShutdown(eventRuleTransfer);
        RUNTIME_START_AND_SHUTDOWN.appendStartAndShutdown(eventTargetPusher);

        // start servers one by one.
        RUNTIME_START_AND_SHUTDOWN.start();

        java.lang.Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("try to shutdown server");
            try {
                RUNTIME_START_AND_SHUTDOWN.shutdown();
            } catch (Exception e) {
                log.error("err when shutdown runtime ", e);
            }
        }));

        startRuntimer();

    }
    private static class RuntimeStartAndShutdown extends AbstractStartAndShutdown {
        @Override
        protected void appendStartAndShutdown(StartAndShutdown startAndShutdown) {
            super.appendStartAndShutdown(startAndShutdown);
        }
    }

    public void startRuntimer() {
        runtimerState = new AtomicReference<>(RuntimeState.START);
    }
}
