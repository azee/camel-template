package ru.yandex.qatools.yard.camel;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.yard.events.*;
import ru.yandex.qatools.yard.camel.utils.TestBeansGenerator;
import ru.yandex.qatools.yard.services.EventService;
import ru.yandex.qatools.yard.services.ProjectService;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;

import ru.yandex.qatools.yard.projects.Project;

/**
 * Created by azee on 3/3/14.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/camel-context.xml", "classpath:camel-test-beans.xml", "classpath:jms-test.xml"})
@DirtiesContext
@MockEndpoints("*")
public class ITCommitWorkflowTest {

    private static final String EVENT_SERVICE_PATH = "http://localhost:9091/event";

    @Autowired
    TestBeansGenerator testBeansGenerator;

    @Autowired
    ProjectService projectService;

    @Autowired
    EventService eventService;

    @EndpointInject(uri = "mock:activemq:queue:save.confirm")
    protected MockEndpoint eventSaveConfirm;

    @Before
    public void before() {
        eventSaveConfirm.reset();
        eventSaveConfirm.setAssertPeriod(3000);
    }

    @DirtiesContext
    @Test
    public void commitWorkflowTest() throws InterruptedException {
        //Create a project
        Project project = testBeansGenerator.createProject("Project1");
        projectService.saveProject(project);

        CommitEvent event = testBeansGenerator.createCommitEvent("CommitEvent");
        event.setProjectId(project.getId());

        //Send an event
        Client client = Client.create();
        WebResource webResource = client.resource(EVENT_SERVICE_PATH + "/commit/Project1");
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

        CommitEvent commitEvent = (CommitEvent) events.get(0);
        Assert.assertThat(commitEvent.getTitle(), is("CommitEvent"));
        Assert.assertThat(commitEvent.getRepository(), is("MyRepo"));
        Assert.assertThat(commitEvent.getSha(), is("MySha"));
        Assert.assertThat(commitEvent.getUser(), is("MyUser"));
        Assert.assertThat(commitEvent.getDescription(), is("CommitEvent description"));
        Assert.assertThat(commitEvent.getProjectId(), is(project.getId()));
        assertNotNull(commitEvent.getStartTime());
        assertNotNull(commitEvent.getStopTime());
        Assert.assertThat(commitEvent.getType(), is(EventType.COMMIT.value()));
    }
}
