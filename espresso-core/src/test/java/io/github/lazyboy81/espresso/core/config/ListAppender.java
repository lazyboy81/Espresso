package io.github.lazyboy81.espresso.core.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.List;

public class ListAppender extends AppenderBase<ILoggingEvent> {
    private static final List<ILoggingEvent> events = new ArrayList<>();

    public List<ILoggingEvent> getEvents() {
        return events;
    }

    @Override
    public void append(ILoggingEvent e) {
        events.add(e);
    }

    public void clear() {
        events.clear();
    }
}
