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
import ru.yandex.qatools.yard.events.*;
import ru.yandex.qatools.yard.projects.Project;
import ru.yandex.qatools.yard.projects.Services;
import ru.yandex.qatools.yard.services.*;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by azee on 3/11/14.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/camel-context.xml", "classpath:camel-test-beans.xml", "classpath:jms-test.xml"})
@DirtiesContext
@MockEndpoints("*")
public class ITJenkinsBuildTest {
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
    public void acceptedJenkinsTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectJenkinsTest");

        Services services = new Services();

        JenkinsServiceImpl jenkinsService = new JenkinsServiceImpl();
        jenkinsService.setHost("jenkinshost");
        jenkinsService.getJobs().add("job");
        services.getJenkinsServices().add(jenkinsService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        JenkinsEvent event = testBeansGenerator.createJenkinsEvent("jenkinshost", "job");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/jenkins/started");
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
        assertThat("Wrong event type", events.get(0).getType(), is("build"));
        assertThat("Wrong event projectId", events.get(0).getProjectId(), is("ProjectJenkinsTest"));
    }

    @DirtiesContext
    @Test
    public void acceptedJenkinsMultipleServicesTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectJenkinsTest");

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

        JenkinsServiceImpl jenkinsService = new JenkinsServiceImpl();
        jenkinsService.setHost("jenkinshost");
        jenkinsService.getJobs().add("job");
        services.getJenkinsServices().add(jenkinsService);

        jenkinsService = new JenkinsServiceImpl();
        jenkinsService.setHost("jenkinshost2");
        jenkinsService.getJobs().add("Jenkins Job 3");
        jenkinsService.getJobs().add("Jenkins Job 4");
        services.getJenkinsServices().add(jenkinsService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        JenkinsEvent event = testBeansGenerator.createJenkinsEvent("jenkinshost", "job");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/jenkins/started");
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
        assertThat("Wrong event type", events.get(0).getType(), is("build"));
        assertThat("Wrong event projectId", events.get(0).getProjectId(), is("ProjectJenkinsTest"));
    }

    @DirtiesContext
    @Test
    public void notAcceptedJenkinsTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectJenkinsTest");

        Services services = new Services();

        JenkinsServiceImpl jenkinsService = new JenkinsServiceImpl();
        jenkinsService.setHost("jenkinshost");
        jenkinsService.getJobs().add("job");
        services.getJenkinsServices().add(jenkinsService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        JenkinsEvent event = testBeansGenerator.createJenkinsEvent("WRONGjenkinshost", "job");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/jenkins/started");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(0);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);
        Assert.assertThat("Unxepected events were found for project", events.size(), is(0));
    }

    @DirtiesContext
    @Test
    public void notAcceptedJenkinsMultipleServicesTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectJenkinsTest");

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

        JenkinsServiceImpl jenkinsService = new JenkinsServiceImpl();
        jenkinsService.setHost("jenkinshost");
        jenkinsService.getJobs().add("job");
        services.getJenkinsServices().add(jenkinsService);

        jenkinsService = new JenkinsServiceImpl();
        jenkinsService.setHost("jenkinshost2");
        jenkinsService.getJobs().add("Jenkins Job 3");
        jenkinsService.getJobs().add("Jenkins Job 4");
        services.getJenkinsServices().add(jenkinsService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        JenkinsEvent event = testBeansGenerator.createJenkinsEvent("WRONGjenkinshost", "job");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/jenkins/started");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(0);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);
        Assert.assertThat("Unexpected events were found for project", events.size(), is(0));
    }

    @DirtiesContext
    @Test
    public void jenkinsFSMCorrectTest() throws InterruptedException, DatatypeConfigurationException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectJenkinsTest");

        Services services = new Services();

        JenkinsServiceImpl jenkinsService = new JenkinsServiceImpl();
        jenkinsService.setHost("jenkinshost");
        jenkinsService.getJobs().add("job");
        services.getJenkinsServices().add(jenkinsService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        JenkinsEvent event = testBeansGenerator.createJenkinsEvent("jenkinshost", "job");
        event.setStatus("started");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/jenkins/started");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(1);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(1));
        BuildEvent buildEvent = (BuildEvent) events.get(0);
        assertNotNull(buildEvent);
        assertThat("Wrong event type", buildEvent.getType(), is("build"));
        assertNotNull("Wrong event start time", buildEvent.getStartTime());
        assertThat("Wrong event stop time", buildEvent.getStopTime().toGregorianCalendar().getTimeInMillis(),
                is(buildEvent.getStartTime().toGregorianCalendar().getTimeInMillis()));
        assertThat("Wrong event status", buildEvent.getStatus(), is("started"));


        //Build is finished now
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        event.setStopTime(datatypeFactory.newXMLGregorianCalendar(gregorianCalendar));

        event.setStatus("finished");
        webResource = client.resource(EVENT_SERVICE_PATH + "/jenkins/finished");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(2);
        eventSaveConfirm.assertIsSatisfied();

        events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(1));
        buildEvent = (BuildEvent) events.get(0);
        assertNotNull(buildEvent);
        assertThat("Wrong event type", buildEvent.getType(), is("build"));
        assertTrue("Wrong event stop time", buildEvent.getStopTime().toGregorianCalendar().getTimeInMillis() !=
                buildEvent.getStartTime().toGregorianCalendar().getTimeInMillis());
        assertThat("Wrong event status", buildEvent.getStatus(), is("finished"));
    }

    @DirtiesContext
    @Test
    public void jenkinsFSMInCorrectTest() throws InterruptedException, DatatypeConfigurationException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectJenkinsTest");

        Services services = new Services();

        JenkinsServiceImpl jenkinsService = new JenkinsServiceImpl();
        jenkinsService.setHost("jenkinshost");
        jenkinsService.getJobs().add("job");
        services.getJenkinsServices().add(jenkinsService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        JenkinsEvent event = testBeansGenerator.createJenkinsEvent("jenkinshost", "job");
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        event.setStopTime(datatypeFactory.newXMLGregorianCalendar(gregorianCalendar));
        event.setStatus("finished");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/jenkins/finished");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(0);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("Unexpected events were found for project", events.size(), is(0));

        event.setStatus("started");

        gregorianCalendar = new GregorianCalendar();
        datatypeFactory = DatatypeFactory.newInstance();
        event.setStartTime(datatypeFactory.newXMLGregorianCalendar(gregorianCalendar));
        webResource = client.resource(EVENT_SERVICE_PATH + "/jenkins/started");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(1);
        eventSaveConfirm.assertIsSatisfied();

        events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(1));
        BuildEvent buildEvent = (BuildEvent) events.get(0);
        assertNotNull(buildEvent);
        assertThat("Wrong event type", buildEvent.getType(), is("build"));
        assertTrue("Wrong event stop time", buildEvent.getStopTime().toGregorianCalendar().getTimeInMillis() !=
                buildEvent.getStartTime().toGregorianCalendar().getTimeInMillis());
        assertThat("Wrong event status", buildEvent.getStatus(), is("finished"));
    }
}
