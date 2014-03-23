package ru.yandex.qatools.yard.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.yard.events.Event;
import ru.yandex.qatools.yard.services.EventService;
import ru.yandex.qatools.yard.services.ProjectService;

import javax.xml.datatype.DatatypeFactory;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by azee on 1/31/14.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class SaveEventProcessor implements Processor{

    @Autowired
    EventService eventService;

    @Autowired
    ProjectService projectService;

    @Override
    public void process(Exchange exchange) throws Exception {
        Event event = exchange.getIn().getBody(Event.class);
        eventService.save(event);
        exchange.getIn().setBody("Saved");
    }
}
