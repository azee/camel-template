package ru.yandex.qatools.yard.camel;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.yard.events.BuildEvent;
import ru.yandex.qatools.yard.events.CommitEvent;
import ru.yandex.qatools.yard.events.Event;
import ru.yandex.qatools.yard.projects.Project;
import ru.yandex.qatools.yard.camel.utils.TestBeansGenerator;
import ru.yandex.qatools.yard.projects.Projects;
import ru.yandex.qatools.yard.services.EventService;
import ru.yandex.qatools.yard.services.ProjectService;

import javax.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * Created by azee
 * on 2/3/14.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/camel-context.xml", "classpath:camel-test-beans.xml", "classpath:jms-test.xml"})
@DirtiesContext
@MockEndpoints("*")
public class ITDefaultWorkflowTest {

    private static final String EVENT_SERVICE_PATH = "http://localhost:9091/event";

    @Autowired
    TestBeansGenerator testBeansGenerator;

    @Autowired
    ProjectService projectService;

    @Autowired
    EventService eventService;

    @EndpointInject(uri = "mock:activemq:queue:save.confirm")
    protected MockEndpoint eventSaveConfirm;

    @Produce(uri = "activemq:queue:event.default")
    protected ProducerTemplate defaultEventQueue;

    private static final String PROJECT_SERVICE_PATH = "http://localhost:9091/project";

    @Before
    public void before() {
        eventSaveConfirm.reset();
        eventSaveConfirm.setAssertPeriod(3000);
    }

    @DirtiesContext
    @Test
    public void testQueue() throws InterruptedException {
        eventSaveConfirm.setExpectedMessageCount(1);

        Project project = testBeansGenerator.createProject("Project1");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("projectId", project.getId());

        Event event = testBeansGenerator.createDefaultEvent("Event1");
        event.setProjectId(project.getId());

        defaultEventQueue.sendBodyAndHeaders(event, headers);
        eventSaveConfirm.assertIsSatisfied();
    }

    @DirtiesContext
    @Test
    public void defaultWorkflowTest() throws InterruptedException {
        //Create a project and tag in db
        Project project = testBeansGenerator.createProject("Project1");
        projectService.saveProject(project);

        Event event = testBeansGenerator.createDefaultEvent("Event1");
        event.setProjectId(project.getId());

        //Send an event
        Client client = Client.create();
        WebResource webResource = client.resource(EVENT_SERVICE_PATH + "/Project1");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(1);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(1));
        assertNotNull(events.get(0));
        Assert.assertThat("Wrong event object was found for project", events.get(0).getTitle(), is("Event1"));


        //Send another event
        event = testBeansGenerator.createDefaultEvent("Event2");
        event.setProjectId(project.getId());
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(2);
        eventSaveConfirm.assertIsSatisfied();

        events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(2));
        for (Event gotEvent : events) {
            assertTrue("Wrong event object was found for project",
                    gotEvent.getTitle().equals("Event1") || gotEvent.getTitle().equals("Event2"));
        }
    }
}

