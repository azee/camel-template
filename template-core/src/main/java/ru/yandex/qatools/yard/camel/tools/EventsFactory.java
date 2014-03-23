package ru.yandex.qatools.yard.camel.tools;

import org.springframework.stereotype.Service;
import ru.yandex.qatools.yard.events.*;

/**
 * Created by azee on 3/17/14.
 */
@Service
public class EventsFactory {

    public StartFinishEvent getStartFinishEvent(String state){
        if ("finished".equals(state)){
            return new FinishedEvent();
        }else {
            return new StartedEvent();
        }
    }
}
