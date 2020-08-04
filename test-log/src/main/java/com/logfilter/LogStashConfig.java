package com.logfilter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public class LogStashConfig implements LoggerContextListener {
    private static final String LOG_STASH_LEVEL_KEY= "LOG_STASH_LEVEL";
    private static final String LOG_STASH_LEVEL_DEFAULT = "DEBUG";
    private static final String FILE_APPENDER_NAME = "LOG_STASH";
    private static final String FILE_PATH = "C:/app/logs/debug/logstash.log";
    private static final String FILE_ROLLING_PATH = "/app/logs/debug/logstash.%d{yyyy-MM-dd}.%i.log.gz";
    private static final int FILE_MAX_HISTORY_DAYS = 1;
    private static final String SINGLE_FILE_MAX_SIZE = "100MB";
    private static final String TOTAL_FILE_MAX_SIZE = "1GB";
    private static final String ENCODER_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{100}:%L - %msg%n";

    private LoggerContext loggerContext;

    @PostConstruct
    public void init() {
        loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.addListener(this);
        addAppender();
    }

    class MyLogFilter extends Filter<ILoggingEvent> {

        @Override
        public FilterReply decide(ILoggingEvent event) {
            if(event.getLoggerName().equalsIgnoreCase("logDebug")){
                return FilterReply.ACCEPT;
            }
            return FilterReply.DENY;
        }

    }

    private void addAppender() {
        RollingFileAppender<ILoggingEvent> logStashAppender = new RollingFileAppender<>();
        logStashAppender.setContext(loggerContext);
        logStashAppender.setName(FILE_APPENDER_NAME);
        logStashAppender.setFile(FILE_PATH);
        logStashAppender.setAppend(true);
        logStashAppender.setPrudent(false);
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern(ENCODER_PATTERN);
        encoder.setContext(loggerContext);
        encoder.start();
        logStashAppender.setEncoder(encoder);
//        ThresholdFilter filter = new ThresholdFilter();
//        filter.setLevel(LOG_STASH_LEVEL_DEFAULT);
//        filter.setContext(loggerContext);
//        filter.start();
        MyLogFilter myLogFilter = new MyLogFilter();
        myLogFilter.start();
        logStashAppender.addFilter(myLogFilter);
        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
        policy.setFileNamePattern(FILE_ROLLING_PATH);
        policy.setMaxFileSize(SINGLE_FILE_MAX_SIZE);
        policy.setTotalSizeCap(FileSize.valueOf(TOTAL_FILE_MAX_SIZE));
        policy.setMaxHistory(FILE_MAX_HISTORY_DAYS);
        policy.setParent(logStashAppender);
        policy.setContext(loggerContext);
        policy.start();
        logStashAppender.setRollingPolicy(policy);
        logStashAppender.start();
        loggerContext.getLogger("root").addAppender(logStashAppender);
    }


    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext loggerContext) {
    }

    @Override
    public void onReset(LoggerContext loggerContext) {
        addAppender();
    }

    @Override
    public void onStop(LoggerContext loggerContext) {}

    @Override
    public void onLevelChange(ch.qos.logback.classic.Logger logger, Level level) {}
}
