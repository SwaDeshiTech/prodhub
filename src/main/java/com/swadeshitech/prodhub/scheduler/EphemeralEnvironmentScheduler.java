package com.swadeshitech.prodhub.scheduler;

import com.swadeshitech.prodhub.entity.EphemeralEnvironment;
import com.swadeshitech.prodhub.enums.EphemeralEnvrionmentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class EphemeralEnvironmentScheduler {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Scheduled(cron = "${cron.ephemeralCleanup:0 */5 * * * *}") // Default: every 5 mins
    public void cleanupExpiredEnvironments() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Fetch expired records that aren't already deleted
        Query query = new Query();
        query.addCriteria(Criteria.where("expiryOn").lt(now));
        query.addCriteria(Criteria.where("status").ne(EphemeralEnvrionmentStatus.DELETED.name()));

        List<EphemeralEnvironment> expiredEnvs = mongoTemplate.find(query, EphemeralEnvironment.class);

        if (expiredEnvs.isEmpty()) {
            return;
        }

        log.info("Found {} expired ephemeral environments to clean up", expiredEnvs.size());

        for (EphemeralEnvironment env : expiredEnvs) {
            try {
                // 2. Mark as DELETING in DB first to prevent other threads from picking it up
                updateStatus(env.getId(), EphemeralEnvrionmentStatus.DELETING.name());

                // 3. Call your logic to remove resources (Cloud/K8s/etc)

                // 4. Mark as DELETED upon success
                updateStatus(env.getId(), EphemeralEnvrionmentStatus.DELETED.name());
                log.info("Successfully cleaned up environment: {}", env.getName());

            } catch (Exception e) {
                log.error("Failed to clean up environment {}: {}", env.getName(), e.getMessage());
                updateStatus(env.getId(), EphemeralEnvrionmentStatus.FAILED.name());
            }
        }
    }

    private void updateStatus(String id, String status) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update()
                .set("status", status)
                .set("lastModifiedTime", LocalDateTime.now());
        mongoTemplate.updateFirst(query, update, EphemeralEnvironment.class);
    }
}
