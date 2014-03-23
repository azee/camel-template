package ru.yandex.qatools.yard.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import ru.yandex.qatools.yard.events.CommitEvent;
import ru.yandex.qatools.yard.events.EventType;

/**
 * Created by azee on 1/31/14.
 */
public class CommitMessageProcessor implements Processor{

    @Override
    public void process(Exchange exchange) throws Exception {
        CommitEvent event = exchange.getIn().getBody(CommitEvent.class);
        String projectId = exchange.getIn().getHeader("projectId", String.class);

        event.setProjectId(projectId);
        event.setType(EventType.COMMIT.value());
        exchange.getIn().setBody(event);
    }
}
