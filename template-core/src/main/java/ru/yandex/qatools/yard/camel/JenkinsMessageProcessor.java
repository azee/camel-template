package ru.yandex.qatools.yard.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.yard.camel.tools.EventsFactory;
import ru.yandex.qatools.yard.events.*;
import ru.yandex.qatools.yard.services.ProjectService;

/**
 * Created by azee on 1/31/14.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class JenkinsMessageProcessor implements Processor {

    @Autowired
    ProjectService projectService;

    @Autowired
    EventsFactory eventsFactory;

    @Override
    public void process(Exchange exchange){
        JenkinsEvent jenkinsEvent = exchange.getIn().getBody(JenkinsEvent.class);
        String state = exchange.getIn().getHeader("state").toString();

        exchange.getIn().setBody(transformBuildEvent(jenkinsEvent, state));
    }

    private StartFinishEvent transformBuildEvent(JenkinsEvent jenkinsEvent, String state){
        BuildEvent buildEvent = new BuildEvent();
        buildEvent.setProjectId(jenkinsEvent.getProjectId());
        buildEvent.setUrl(jenkinsEvent.getUrl());
        buildEvent.setTests(jenkinsEvent.getTests());
        buildEvent.setFailed(jenkinsEvent.getFailed());
        buildEvent.setPassed(jenkinsEvent.getPassed());
        buildEvent.setStatus(jenkinsEvent.getStatus());
        buildEvent.setTitle(jenkinsEvent.getTitle());

        if (jenkinsEvent.getDescription() != null && !"".equals(jenkinsEvent.getDescription())){
            buildEvent.setDescription(jenkinsEvent.getDescription());
        } else {
            buildEvent.setDescription(jenkinsEvent.getJob() + " ["
                    + jenkinsEvent.getStatus() + "]");
        }

        buildEvent.setStartTime(jenkinsEvent.getStartTime());
        buildEvent.setStopTime(jenkinsEvent.getStopTime());
        buildEvent.setType(EventType.BUILD.value());

        StartFinishEvent startFinishEvent = eventsFactory.getStartFinishEvent(state);
        startFinishEvent.setEvent(buildEvent);
        startFinishEvent.setKey("jenkins-" + jenkinsEvent.getUrl());
        return startFinishEvent;
    }
}
