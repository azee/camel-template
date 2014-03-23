package ru.yandex.qatools.yard.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Processor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.yard.camel.tools.EventsFactory;
import ru.yandex.qatools.yard.events.*;
import ru.yandex.qatools.yard.projects.Project;
import ru.yandex.qatools.yard.services.ProjectService;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by azee on 1/31/14.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class TeamcityMessageProcessor implements Processor {

    @Autowired
    ProjectService projectService;

    @Autowired
    EventsFactory eventsFactory;

    private static final Logger log = LogManager.getLogger(TeamcityMessageProcessor.class);

    @Override
    public void process(Exchange exchange){
        TeamcityEvent teamcityEvent = exchange.getIn().getBody(TeamcityEvent.class);
        String state = exchange.getIn().getHeader("state").toString();
        exchange.getIn().setBody(transformBuildEvent(teamcityEvent, state));
    }

    private StartFinishEvent transformBuildEvent(TeamcityEvent teamcityEvent, String state){
        BuildEvent buildEvent = new BuildEvent();
        buildEvent.setProjectId(teamcityEvent.getProjectId());
        buildEvent.setUrl(teamcityEvent.getUrl());
        buildEvent.setTests(teamcityEvent.getTests());
        buildEvent.setFailed(teamcityEvent.getFailed());
        buildEvent.setPassed(teamcityEvent.getPassed());
        buildEvent.setStatus(teamcityEvent.getStatus());
        buildEvent.setTitle(teamcityEvent.getTitle());

        log.info("Setting a title " + buildEvent.getTitle());

        if (teamcityEvent.getDescription() != null && !"".equals(teamcityEvent.getDescription())){
            buildEvent.setDescription(teamcityEvent.getDescription());
        } else {
            buildEvent.setDescription(teamcityEvent.getJob());
        }

        buildEvent.setStartTime(teamcityEvent.getStartTime());
        buildEvent.setStopTime(teamcityEvent.getStopTime());
        buildEvent.setType(EventType.BUILD.value());

        StartFinishEvent startFinishEvent = eventsFactory.getStartFinishEvent(state);
        startFinishEvent.setEvent(buildEvent);
        startFinishEvent.setKey("teamcity-" + teamcityEvent.getUrl());
        return startFinishEvent;
    }
}
