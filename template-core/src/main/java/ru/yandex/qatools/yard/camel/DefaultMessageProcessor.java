package ru.yandex.qatools.yard.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import ru.yandex.qatools.yard.events.Event;
import ru.yandex.qatools.yard.events.EventType;

/**
 * Created by azee on 1/31/14.
 */
public class DefaultMessageProcessor implements Processor{

    @Override
    public void process(Exchange exchange) throws Exception {
        Event event = exchange.getIn().getBody(Event.class);
        String projectId = exchange.getIn().getHeader("projectId", String.class);
        event.setProjectId(projectId);
        event.setType(EventType.DEFAULT.value());
        exchange.getIn().setBody(event);
    }
}
