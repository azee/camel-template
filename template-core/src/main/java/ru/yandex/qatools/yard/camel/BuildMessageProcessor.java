package ru.yandex.qatools.yard.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import ru.yandex.qatools.yard.events.BuildEvent;
import ru.yandex.qatools.yard.events.EventType;

/**
 * Created by azee on 1/31/14.
 */
public class BuildMessageProcessor implements Processor{

    @Override
    public void process(Exchange exchange) throws Exception {
        BuildEvent event = exchange.getIn().getBody(BuildEvent.class);
        String projectId = exchange.getIn().getHeader("projectId", String.class);

        event.setProjectId(projectId);
        event.setType(EventType.BUILD.value());
        exchange.getIn().setBody(event);
    }
}
