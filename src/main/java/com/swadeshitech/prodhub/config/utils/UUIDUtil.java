package com.swadeshitech.prodhub.config.utils;

import java.time.LocalDateTime;
import java.util.Random;

public class UUIDUtil {
    
    public static String generateUUID() {
        LocalDateTime now = LocalDateTime.now();
        Random random = new Random();
        String uniqueId = now.toString() + random.nextInt(1000);
        return uniqueId;
    }
}
