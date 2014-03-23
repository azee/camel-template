package ru.yandex.qatools.yard.camel.fsm;


import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.yard.events.*;
import ru.yandex.qatools.yard.services.EventService;
import ru.yandex.qatools.yard.states.*;

import javax.xml.datatype.DatatypeConfigurationException;

/**
 * Created by azee on 3/14/14.
 */
@Component
@FSM(start = UndefinedState.class)
@Transitions({
        @Transit(from = UndefinedState.class, to = StartedState.class, on = StartedEvent.class),
        @Transit(from = UndefinedState.class, to = FinishedState.class, on = FinishedEvent.class),
        @Transit(from = StartedState.class, on = FinishedEvent.class, stop = true),
        @Transit(from = FinishedState.class, on = StartedEvent.class, stop = true)
})
public class StartFinishEventAggregator {
    private static final Logger logger = Logger.getLogger(StartFinishEventAggregator.class);

    @Produce(uri = "activemq:queue:save.confirm")
    private ProducerTemplate saveConfirm;

    @Autowired
    EventService eventService;

    //Start even arrived first
    @OnTransit
    public void onStarted(UndefinedState oldState, StartedState newState, StartedEvent carrierEvent) throws DatatypeConfigurationException {
        logger.info("Start-Stop FSM recieved Undefined -> Started Event");
        Event newEvent = carrierEvent.getEvent();
        newState.setEvent(newEvent);
        saveEvent(newEvent);
    }

    //Finish even arrived first. Await for a start event to set correct start time
    @OnTransit
    public void onFinished(UndefinedState oldState, FinishedState newState, FinishedEvent carrierEvent){
        logger.info("Start-Stop FSM recieved Undefined -> Finished Event");
        Event newEvent = carrierEvent.getEvent();
        newState.setEvent(newEvent);
    }

    //Aqua finished event arrived after build started event
    @OnTransit
    public void onFinished(StartedState oldState, FinishedEvent carrierEvent) throws DatatypeConfigurationException {
        logger.info("Start-Stop FSM recieved Started -> Finished Event");
        Event newEvent = carrierEvent.getEvent();
        Event oldEvent = oldState.getEvent();
        newEvent.setStartTime(oldEvent.getStartTime());
        newEvent.setId(oldEvent.getId());
        saveEvent(newEvent);
    }

    //Aqua started event arrived after build finished event
    @OnTransit
    public void onAquaFinished(FinishedState oldState, StartedEvent carrierEvent) throws DatatypeConfigurationException {
        logger.info("Start-Stop FSM recieved Finished -> Started Event");
        Event newEvent = carrierEvent.getEvent();
        Event oldEvent = oldState.getEvent();
        oldEvent.setStartTime(newEvent.getStartTime());
        saveEvent(oldEvent);
    }

    private void saveEvent (Event event) throws DatatypeConfigurationException {
        eventService.save(event);
        saveConfirm.sendBody("Saved");
        logger.info("Event saved with id " + event.getId());
        logger.info("Event saved with title " + event.getTitle());
    }

}
