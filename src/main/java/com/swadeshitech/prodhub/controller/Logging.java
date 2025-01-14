package com.swadeshitech.prodhub.controller;

import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/logging")
@Slf4j
public class Logging {

    @PutMapping("/level")
    public ResponseEntity<String> setLogLevel(@RequestParam("level") String level) {
        try {
            // Validate and set the log level
            if (isValidLogLevel(level)) {
                Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                rootLogger.setLevel(Level.valueOf(level));
                return ResponseEntity.ok("Log level set to " + level);
            } else {
                return ResponseEntity.badRequest().body("Invalid log level. Valid levels: DEBUG, INFO, WARN, ERROR");
            }
        } catch (Exception e) {
            log.error("Failed to set log level: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to set log level.");
        }
    }

    private boolean isValidLogLevel(String level) {
        return Level.INFO.levelStr.equalsIgnoreCase(level) ||
        Level.DEBUG.levelStr.equalsIgnoreCase(level) ||
        Level.ERROR.levelStr.equalsIgnoreCase(level) ||
        Level.WARN.levelStr.equalsIgnoreCase(level);
    }
}
