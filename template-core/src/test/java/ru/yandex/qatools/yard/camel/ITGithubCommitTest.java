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
import ru.yandex.qatools.yard.camel.utils.TestBeansGenerator;
import ru.yandex.qatools.yard.events.Event;
import ru.yandex.qatools.yard.events.GithubEvent;
import ru.yandex.qatools.yard.events.GithubRepository;
import ru.yandex.qatools.yard.projects.Project;
import ru.yandex.qatools.yard.projects.Services;
import ru.yandex.qatools.yard.services.*;

import javax.ws.rs.core.MediaType;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by azee on 3/11/14.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/camel-context.xml", "classpath:camel-test-beans.xml", "classpath:jms-test.xml"})
@DirtiesContext
@MockEndpoints("*")
public class ITGithubCommitTest {
    private static final String PROJECT_SERVICE_PATH = "http://localhost:9091/project";
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
    public void acceptedGithubCommitWorkflowTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectGithubTest");

        Services services = new Services();

        GithubServiceImpl githubService = new GithubServiceImpl();
        githubService.setHost("githost");
        githubService.getRepositories().add("/org/repo");
        services.getGithubServices().add(githubService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        GithubEvent event = testBeansGenerator.createGithubEvent();
        GithubRepository githubRepository = new GithubRepository();
        githubRepository.setUrl("githost/org/repo");
        event.setRepository(githubRepository);

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/github/object");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(2);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(2));
        assertNotNull(events.get(0));
        assertNotNull(events.get(1));

        assertThat("Wrong event type", events.get(0).getType(), is("commit"));
        assertThat("Wrong event projectId", events.get(0).getProjectId(), is("ProjectGithubTest"));

        assertThat("Wrong event type", events.get(1).getType(), is("commit"));
        assertThat("Wrong event projectId", events.get(1).getProjectId(), is("ProjectGithubTest"));
    }

    @DirtiesContext
    @Test
    public void acceptedGithubCommitMultipleServicesTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectGithubTest");

        Services services = new Services();

        GithubServiceImpl githubService = new GithubServiceImpl();
        githubService.setHost("githost");
        githubService.getRepositories().add("/org/repo");
        githubService.getRepositories().add("/org1/repo1");
        services.getGithubServices().add(githubService);

        githubService = new GithubServiceImpl();
        githubService.setHost("githost2");
        githubService.getRepositories().add("/org2/repo2");
        githubService.getRepositories().add("/org22/repo22");
        services.getGithubServices().add(githubService);

        JenkinsService jenkinsService = new JenkinsService();
        jenkinsService.setHost("jenkinshost");
        jenkinsService.getJobs().add("Jenkins Job 1");
        jenkinsService.getJobs().add("Jenkins Job 2");
        services.getJenkinsServices().add(jenkinsService);

        jenkinsService = new JenkinsService();
        jenkinsService.setHost("jenkinshost2");
        jenkinsService.getJobs().add("Jenkins Job 3");
        jenkinsService.getJobs().add("Jenkins Job 4");
        services.getJenkinsServices().add(jenkinsService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        GithubEvent event = testBeansGenerator.createGithubEvent();
        GithubRepository githubRepository = new GithubRepository();
        githubRepository.setUrl("githost/org/repo");
        event.setRepository(githubRepository);

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/github/object");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(2);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(2));
        assertNotNull(events.get(0));
        assertNotNull(events.get(1));

        assertThat("Wrong event type", events.get(0).getType(), is("commit"));
        assertThat("Wrong event projectId", events.get(0).getProjectId(), is("ProjectGithubTest"));

        assertThat("Wrong event type", events.get(1).getType(), is("commit"));
        assertThat("Wrong event projectId", events.get(1).getProjectId(), is("ProjectGithubTest"));
    }

    @DirtiesContext
    @Test
    public void notAcceptedGithubCommitWorkflowTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectGithubTestNotAccepted");

        Services services = new Services();

        GithubServiceImpl githubService = new GithubServiceImpl();
        githubService.setHost("githost");
        githubService.getRepositories().add("/org/repo");
        services.getGithubServices().add(githubService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        GithubEvent event = testBeansGenerator.createGithubEvent();
        GithubRepository githubRepository = new GithubRepository();
        githubRepository.setUrl("WRONGgithost/org/repo");
        event.setRepository(githubRepository);

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/github/object");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(0);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);
        Assert.assertThat("Unaccepted events were found for project", events.size(), is(0));
    }

    @DirtiesContext
    @Test
    public void notAcceptedGithubCommitTestMultipleServices() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectGithubTestNotAccepted");

        Services services = new Services();

        GithubServiceImpl githubService = new GithubServiceImpl();
        githubService.setHost("githost");
        githubService.getRepositories().add("/org/repo");
        githubService.getRepositories().add("/org1/repo1");
        services.getGithubServices().add(githubService);

        githubService = new GithubServiceImpl();
        githubService.setHost("githost2");
        githubService.getRepositories().add("/org2/repo2");
        githubService.getRepositories().add("/org22/repo22");
        services.getGithubServices().add(githubService);

        JenkinsService jenkinsService = new JenkinsService();
        jenkinsService.setHost("jenkinshost");
        jenkinsService.getJobs().add("Jenkins Job 1");
        jenkinsService.getJobs().add("Jenkins Job 2");
        services.getJenkinsServices().add(jenkinsService);

        jenkinsService = new JenkinsService();
        jenkinsService.setHost("jenkinshost2");
        jenkinsService.getJobs().add("Jenkins Job 3");
        jenkinsService.getJobs().add("Jenkins Job 4");
        services.getJenkinsServices().add(jenkinsService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        GithubEvent event = testBeansGenerator.createGithubEvent();
        GithubRepository githubRepository = new GithubRepository();
        githubRepository.setUrl("WRONGgithost/org/repo");
        event.setRepository(githubRepository);

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/github/object");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(0);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);
        Assert.assertThat("Unaccepted events were found for project", events.size(), is(0));
    }
}
