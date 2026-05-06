package com.synaptiq.workflow.infrastructure.config;

import com.synaptiq.agentflow.executor.FlowExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowConfig {

    @Bean
    public FlowExecutor flowExecutor() {
        return new FlowExecutor();
    }
}
