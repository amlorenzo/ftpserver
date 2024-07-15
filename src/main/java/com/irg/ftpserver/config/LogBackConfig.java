package com.irg.ftpserver.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.irg.ftpserver.service.SFTPConfigurationService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.ILoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Iterator;

@Configuration
@DependsOn({"SFTPInitialConfigService"})
@AllArgsConstructor
public class LogBackConfig {

    private final SFTPConfigurationService sftpConfigurationService;

    @PostConstruct
    public void setupLogging() {
        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();

        if (!(iLoggerFactory instanceof LoggerContext context)) {
            return; // Not a Logback logger context
        }

        context.getLoggerList().forEach(logger -> {
            Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders();
            while (it.hasNext()) {
                Appender<ILoggingEvent> appender = it.next();
                if (appender instanceof ConsoleAppender || appender instanceof RollingFileAppender) {
                    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
                    encoder.setContext(context);
                    encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} " + sftpConfigurationService
                            .getLatestConfiguration().getTimeZone() + " [%thread] %-5level %logger{0} - %msg%n");
                    encoder.start();

                    ((OutputStreamAppender<ILoggingEvent>) appender).setEncoder(encoder);
                }
            }
        });
        context.start();
    }
}


