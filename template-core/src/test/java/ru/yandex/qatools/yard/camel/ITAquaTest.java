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

/**
 * Created by azee on 3/11/14.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/camel-context.xml", "classpath:camel-test-beans.xml", "classpath:jms-test.xml"})
@DirtiesContext
@MockEndpoints("*")
public class ITAquaTest {
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
    public void acceptedAquaTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectAquaTest");

        Services services = new Services();

        AquaServiceImpl aquaService = new AquaServiceImpl();
        aquaService.setHost("aquahost/");
        aquaService.getPacks().add("packId");
        services.getAquaServices().add(aquaService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        AquaEvent event = testBeansGenerator.createAquaStartedEvent("aquahost");
        event.setLaunch("lunchId");
        event.setPack("packId");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/aqua/started");
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
        Assert.assertThat("Wrong event type", events.get(0).getType(), is("aqua"));
        Assert.assertThat("Wrong event projectId", events.get(0).getProjectId(), is("ProjectAquaTest"));
    }

    @DirtiesContext
    @Test
    public void acceptedAquaMultipleServicesTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectAquaTest");

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

        TeamcityServiceImpl teamcityService = new TeamcityServiceImpl();
        teamcityService.setHost("teamcityshost");
        teamcityService.getJobs().add("job");
        services.getTeamcityServices().add(teamcityService);

        teamcityService = new TeamcityServiceImpl();
        teamcityService.setHost("teamcityshost2");
        teamcityService.getJobs().add("job2");
        services.getTeamcityServices().add(teamcityService);

        AquaServiceImpl aquaService = new AquaServiceImpl();
        aquaService.setHost("aquahost");
        aquaService.getPacks().add("packId");
        services.getAquaServices().add(aquaService);

        aquaService = new AquaServiceImpl();
        aquaService.setHost("aquahost1");
        aquaService.getPacks().add("packId2");
        services.getAquaServices().add(aquaService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        AquaEvent event = testBeansGenerator.createAquaStartedEvent("aquahost");
        event.setLaunch("lunchId");
        event.setPack("packId");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/aqua/started");
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
        Assert.assertThat("Wrong event type", events.get(0).getType(), is("aqua"));
        Assert.assertThat("Wrong event projectId", events.get(0).getProjectId(), is("ProjectAquaTest"));
    }

    @DirtiesContext
    @Test
    public void notAcceptedAquaTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectAquaTest");

        Services services = new Services();

        AquaServiceImpl aquaService = new AquaServiceImpl();
        aquaService.setHost("aquahost");
        aquaService.getPacks().add("packId");
        services.getAquaServices().add(aquaService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        AquaEvent event = testBeansGenerator.createAquaStartedEvent("WRONGaquahost");
        event.setLaunch("lunchId");
        event.setPack("packId");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/aqua/started");
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
    public void notAcceptedAquaMultipleServicesTest() throws InterruptedException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectAquaTest");

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

        TeamcityServiceImpl teamcityService = new TeamcityServiceImpl();
        teamcityService.setHost("teamcityshost");
        teamcityService.getJobs().add("job");
        services.getTeamcityServices().add(teamcityService);

        teamcityService = new TeamcityServiceImpl();
        teamcityService.setHost("teamcityshost3");
        teamcityService.getJobs().add("job3");
        services.getTeamcityServices().add(teamcityService);

        AquaServiceImpl aquaService = new AquaServiceImpl();
        aquaService.setHost("aquahost");
        aquaService.getPacks().add("packId");
        services.getAquaServices().add(aquaService);

        aquaService = new AquaServiceImpl();
        aquaService.setHost("aquahost1");
        aquaService.getPacks().add("packId2");
        services.getAquaServices().add(aquaService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        AquaEvent event = testBeansGenerator.createAquaStartedEvent("WRONGaquahost");
        event.setLaunch("lunchId");
        event.setPack("packId");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/aqua/started");
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
    public void aquaFSMCorrectTest() throws InterruptedException, DatatypeConfigurationException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectAquaTest");

        Services services = new Services();

        AquaServiceImpl aquaService = new AquaServiceImpl();
        aquaService.setHost("aquahost");
        aquaService.getPacks().add("packId");
        services.getAquaServices().add(aquaService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        AquaEvent event = testBeansGenerator.createAquaStartedEvent("aquahost");
        event.setLaunch("lunchId");
        event.setPack("packId");

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/aqua/started");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(event);

        eventSaveConfirm.setExpectedMessageCount(1);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(1));
        AquaEvent aquaEvent = (AquaEvent) events.get(0);
        assertNotNull(aquaEvent);
        Assert.assertThat("Wrong event type", aquaEvent.getType(), is("aqua"));
        assertNotNull("Wrong event start time", aquaEvent.getStartTime());
        Assert.assertThat("Wrong event stop time", aquaEvent.getStopTime().toGregorianCalendar().getTimeInMillis(),
                is(aquaEvent.getStartTime().toGregorianCalendar().getTimeInMillis()));
        Assert.assertThat("Wrong event status", aquaEvent.getStatus(), is("STARTED"));


        //Aqua is finished now
        AquaEvent aquaFinishedEvent = testBeansGenerator.createAquaFinishedEvent("aquahost");
        aquaFinishedEvent.setLaunch("lunchId");
        aquaFinishedEvent.setPack("packId");
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        aquaFinishedEvent.setStopTime(datatypeFactory.newXMLGregorianCalendar(gregorianCalendar));

        webResource = client.resource(EVENT_SERVICE_PATH + "/aqua/finished");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(aquaFinishedEvent);

        eventSaveConfirm.setExpectedMessageCount(2);
        eventSaveConfirm.assertIsSatisfied();

        events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(1));
        aquaEvent = (AquaEvent) events.get(0);
        assertNotNull(aquaEvent);
        Assert.assertThat("Wrong event type", aquaEvent.getType(), is("aqua"));
        assertTrue("Wrong event stop time", aquaEvent.getStopTime().toGregorianCalendar().getTimeInMillis() !=
                aquaEvent.getStartTime().toGregorianCalendar().getTimeInMillis());
        Assert.assertThat("Wrong event status", aquaEvent.getStatus(), is("FINISHED"));
    }

    @DirtiesContext
    @Test
    public void aquaFSMInCorrectTest() throws InterruptedException, DatatypeConfigurationException {
        Client client = Client.create();
        WebResource webResource = client.resource(PROJECT_SERVICE_PATH);
        Project project = testBeansGenerator.createProject("ProjectAquaTest");

        Services services = new Services();

        AquaServiceImpl aquaService = new AquaServiceImpl();
        aquaService.setHost("aquahost");
        aquaService.getPacks().add("packId");
        services.getAquaServices().add(aquaService);

        project.setServices(services);

        projectService.saveProject(project);

        //Send an event
        AquaEvent aquaFinishedEvent = testBeansGenerator.createAquaFinishedEvent("aquahost");
        aquaFinishedEvent.setLaunch("lunchId");
        aquaFinishedEvent.setPack("packId");
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        aquaFinishedEvent.setStopTime(datatypeFactory.newXMLGregorianCalendar(gregorianCalendar));

        //Send an event
        webResource = client.resource(EVENT_SERVICE_PATH + "/aqua/finished");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(aquaFinishedEvent);

        eventSaveConfirm.setExpectedMessageCount(0);
        eventSaveConfirm.assertIsSatisfied();

        //Retrieve events
        List<Event> events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("Unexpected events were found for project", events.size(), is(0));

        AquaEvent aquaStartedEvent = testBeansGenerator.createAquaStartedEvent("aquahost");
        aquaStartedEvent.setLaunch("lunchId");
        aquaStartedEvent.setPack("packId");
        gregorianCalendar = new GregorianCalendar();
        datatypeFactory = DatatypeFactory.newInstance();
        aquaStartedEvent.setStartTime(datatypeFactory.newXMLGregorianCalendar(gregorianCalendar));
        webResource = client.resource(EVENT_SERVICE_PATH + "/aqua/started");
        webResource.type(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .post(aquaStartedEvent);

        eventSaveConfirm.setExpectedMessageCount(1);
        eventSaveConfirm.assertIsSatisfied();

        events = eventService.getEventsByProject(project.getId(), 0, 10);

        assertNotNull(events);
        Assert.assertThat("No events were found for project", events.size(), is(1));
        AquaEvent aquaEvent = (AquaEvent) events.get(0);
        assertNotNull(aquaEvent);
        Assert.assertThat("Wrong event type", aquaEvent.getType(), is("aqua"));
        assertTrue("Wrong event stop time", aquaEvent.getStopTime().toGregorianCalendar().getTimeInMillis() !=
                aquaEvent.getStartTime().toGregorianCalendar().getTimeInMillis());
        Assert.assertThat("Wrong event status", aquaEvent.getStatus(), is("FINISHED"));
    }
}
