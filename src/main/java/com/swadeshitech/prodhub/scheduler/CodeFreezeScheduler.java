package com.swadeshitech.prodhub.scheduler;

import com.swadeshitech.prodhub.entity.CodeFreeze;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@Slf4j
public class CodeFreezeScheduler {

    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * Runs every hour to check for expired Code Freeze records.
     * Cron expression: "0 0 * * * *"
     */
    @Scheduled(cron = "${cron.codeFreezeScheduler:0 * * * * *}")
    public void deactivateExpiredCodeFreezes() {

        log.info("Starting code freeze scheduler");

        LocalDateTime now = LocalDateTime.now();

        // 1. Define the Query: endTime < now AND isActive == true
        Query query = new Query();
        query.addCriteria(Criteria.where("endTime").lt(now));
        query.addCriteria(Criteria.where("isActive").is(true));

        // 2. Define the Update: Set isActive to false
        Update update = new Update();
        update.set("isActive", false);
        update.set("lastModifiedTime", LocalDateTime.now());
        update.set("lastModifiedBy", "SYSTEM_SCHEDULER");

        // 3. Execute Update
        var result = mongoTemplate.updateMulti(query, update, CodeFreeze.class);

        log.info("Scheduled Task: Deactivated {} records", result.getModifiedCount());
    }
}
