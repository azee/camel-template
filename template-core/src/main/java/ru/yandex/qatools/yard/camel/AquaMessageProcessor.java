package ru.yandex.qatools.yard.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.yard.events.*;
import ru.yandex.qatools.yard.services.ProjectService;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by azee on 1/31/14.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class AquaMessageProcessor implements Processor {

    private static final Logger log = LogManager.getLogger(AquaMessageProcessor.class);

    @Autowired
    ProjectService projectService;

    @Override
    public void process(Exchange exchange) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        AquaEvent aquaEvent = exchange.getIn().getBody(AquaEvent.class);
        StartFinishEvent newEvent = new StartFinishEvent();
        aquaEvent.setType(EventType.AQUA.value());

        if (aquaEvent instanceof AquaStartedEvent){
            newEvent = new StartedEvent();
            aquaEvent.setStatus("STARTED");
        }
        if (aquaEvent instanceof AquaFinishedEvent){
            newEvent = new FinishedEvent();
            aquaEvent.setStatus("FINISHED");
        }

        newEvent.setEvent(aquaEvent);
        newEvent.setKey("aqua-" + aquaEvent.getLaunch());
        exchange.getIn().setBody(newEvent);
    }
}
