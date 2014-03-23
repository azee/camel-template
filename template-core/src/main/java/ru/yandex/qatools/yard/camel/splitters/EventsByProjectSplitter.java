package ru.yandex.qatools.yard.camel.splitters;

import org.apache.commons.lang.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.yard.annotations.Identified;
import ru.yandex.qatools.yard.events.*;
import ru.yandex.qatools.yard.projects.Project;
import ru.yandex.qatools.yard.services.ProjectService;
import ru.yandex.qatools.yard.services.processors.DefaultSourceProcessor;
import ru.yandex.qatools.yard.services.processors.SourceProcessor;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by azee on 3/21/14.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class EventsByProjectSplitter {
    @Autowired
    ProjectService projectService;

    public List<Event> split(Event event) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        List<Event> events = new LinkedList<>();

        String credentials = getCredentials(event);

        //Getting projects for current credentials
        List<Project> projects = projectService.getProjectsByCredential(credentials);
        for (Project project : projects){
            events.add(copyObject(event, project.getId()));
        }
        return events;
    }

    private String getCredentials(Event event){
        SourceProcessor sourceProcessor;
        Identified annotation = event.getClass().getAnnotation(Identified.class);
        Class sourceProcessorClass = annotation.identity();
        try {
            sourceProcessor = (SourceProcessor) sourceProcessorClass.newInstance();
        } catch (Exception e){
            sourceProcessor = new DefaultSourceProcessor();
        }

        return sourceProcessor.getSourceIdentity(event);
    }

    private Event copyObject(Event event, String projectId) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        Event newEvent = (Event) SerializationUtils.clone(event);
        newEvent.setProjectId(projectId);
        return newEvent;
    }
}
