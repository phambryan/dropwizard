package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.FilterAttachable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.TimeZone;

@JsonTypeName("syslog")
public class SyslogAppenderFactory implements AppenderFactory {
    public enum Facility {
        AUTH, AUTHPRIV, DAEMON, CRON, FTP, LPR, KERN, MAIL, NEWS, SYSLOG, USER, UUCP,
        LOCAL0, LOCAL1, LOCAL2, LOCAL3, LOCAL4, LOCAL5, LOCAL6, LOCAL7;

        @Override
        @JsonValue
        public String toString() {
            return super.toString().replace("_", "+").toLowerCase(Locale.ENGLISH);
        }

        @JsonCreator
        public static Facility parse(String facility) {
            return valueOf(facility.toUpperCase(Locale.ENGLISH).replace('+', '_'));
        }
    }

    @NotNull
    private Level threshold = Level.ALL;

    @NotNull
    private String host = "localhost";

    @NotNull
    private Facility facility = Facility.LOCAL0;

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    private String logFormat;

    @JsonProperty
    public Level getThreshold() {
        return threshold;
    }

    @JsonProperty
    public void setThreshold(Level threshold) {
        this.threshold = threshold;
    }

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public Facility getFacility() {
        return facility;
    }

    @JsonProperty
    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty
    public String getLogFormat() {
        return logFormat;
    }

    @JsonProperty
    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
        final SyslogAppender appender = new SyslogAppender();
        appender.setContext(context);
        appender.setSuffixPattern(logFormat);
        appender.setSyslogHost(host);
        appender.setFacility(facility.toString());
        addThresholdFilter(appender, threshold);
        appender.start();

        return appender;
    }

    private void addThresholdFilter(FilterAttachable<ILoggingEvent> appender, Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }
}
