package cn.gofree.lingxi.eventcenter.observer;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuntimeConfiguration {

    @Bean(name = "runnerConfigObserver")
    public TargetRunnerConfigObserver targetRunnerConfigObserver(@Value("${runtime.config.mode}") String configMode) {
                return new TargetRunnerConfigOnFileObserver();
    }
}