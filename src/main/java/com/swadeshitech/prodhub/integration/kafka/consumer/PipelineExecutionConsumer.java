package com.swadeshitech.prodhub.integration.kafka.consumer;

import com.swadeshitech.prodhub.entity.PipelineExecution;
import com.swadeshitech.prodhub.services.PipelineService;
import com.swadeshitech.prodhub.transaction.read.ReadTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PipelineExecutionConsumer {

    @Autowired
    ReadTransactionService readTransactionService;

    @Autowired
    PipelineService pipelineService;

    @KafkaListener(topics = "${spring.kafka.topic.pipeline-execution}", groupId = "default_group", containerFactory = "kafkaListenerContainerFactory")
    public void listen(String pipelineExecutionId) {
        log.info("Message received for pipeline-execution for pipeline {}", pipelineExecutionId);

        List<PipelineExecution> pipelineExecutions = readTransactionService.findByDynamicOrFilters(Map.of("_id", new ObjectId(pipelineExecutionId))
                ,PipelineExecution.class);
        if(CollectionUtils.isEmpty(pipelineExecutions)) {
            log.error("Pipeline executions could not found {}", pipelineExecutionId);
            return;
        }
        PipelineExecution pipelineExecution = pipelineExecutions.getFirst();

        pipelineService.startPipelineExecution(pipelineExecution);
    }
}
