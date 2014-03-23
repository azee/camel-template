package ru.yandex.qatools.yard.camel.utils;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.springframework.stereotype.Service;
import ru.yandex.qatools.yard.projects.*;
import ru.yandex.qatools.yard.events.*;
import ru.yandex.qatools.yard.states.AquaFinished;

/**
 * Created by azee on 2/3/14.
 */
@Service
public class TestBeansGenerator {
    public Event getEvent(String title){
        Event event = new Event();
        event.setTitle(title);
        event.setId(title);
        event.setDescription(title + " Description");
        return event;
    }

    public Project createProject(String title){
        Project project = new Project();
        project.setId(title);
        project.setTitle(title);
        project.setDescription("Project " + title);
        return project;
    }

    public DefaultEvent createDefaultEvent(String title){
        DefaultEvent event = new DefaultEvent();
        event.setId(title);
        event.setTitle(title);
        event.setDescription(title + " descr");
        event.setStartTime(new XMLGregorianCalendarImpl());
        return event;
    }

    public CommitEvent createCommitEvent(String title){
        CommitEvent event = new CommitEvent();
        event.setId(title);
        event.setTitle(title);
        event.setDescription(title + " description");
        event.setStartTime(new XMLGregorianCalendarImpl());
        event.setRepository("MyRepo");
        event.setSha("MySha");
        event.setUser("MyUser");
        return event;
    }


    public GithubEvent createGithubEvent(){
        GithubEvent githubEvent = new GithubEvent();
        githubEvent.setBefore("Sha before");
        githubEvent.setAfter("Sha after");
        githubEvent.setCompare("compare");
        githubEvent.setDeleted(false);
        githubEvent.setForced(false);
        githubEvent.setHeadCommit(createGithubCommit("Github commit 2"));
        githubEvent.setPusher(createGithubUser("UserName"));
        githubEvent.setRef("ref");

        GithubRepository githubRepository = new GithubRepository();
        githubRepository.setUrl("github.url");
        githubEvent.setRepository(githubRepository);

        githubEvent.getCommits().add(createGithubCommit("Github commit 1"));
        githubEvent.getCommits().add(createGithubCommit("Github commit 2"));
        return githubEvent;
    }

    public GithubCommit createGithubCommit(String title){
        GithubCommit githubCommit = new GithubCommit();
        GithubUser githubUser = createGithubUser("UserName");

        githubCommit.setAuthor(githubUser);
        githubCommit.setCommitter(githubUser);
        githubCommit.setMessage(title + " message");
        githubCommit.setTimestamp(new XMLGregorianCalendarImpl());
        githubCommit.setUrl(title + " commit url");
        githubCommit.setId(title);
        return githubCommit;
    }

    public GithubUser createGithubUser(String name){
        GithubUser githubUser = new GithubUser();
        githubUser.setEmail(name + "@devnull.null");
        githubUser.setName(name);
        githubUser.setUsername(name);
        return githubUser;
    }

    public JenkinsEvent createJenkinsEvent(String host, String jobName){
        JenkinsEvent jenkinsEvent = new JenkinsEvent();
        jenkinsEvent.setUrl(host + jobName);
        jenkinsEvent.setHost(host);
        jenkinsEvent.setDescription(jobName + " description");
        jenkinsEvent.setStartTime(new XMLGregorianCalendarImpl());
        jenkinsEvent.setStopTime(null);
        jenkinsEvent.setStatus("passed");
        jenkinsEvent.setTests(10);
        jenkinsEvent.setFailed(1);
        jenkinsEvent.setPassed(9);
        jenkinsEvent.setJob(jobName);
        return jenkinsEvent;
    }

    public TeamcityEvent createTeamcityEvent(String host, String jobName){
        TeamcityEvent teamcityEvent = new TeamcityEvent();
        teamcityEvent.setUrl(host + jobName);
        teamcityEvent.setHost(host);
        teamcityEvent.setDescription(jobName + " description");
        teamcityEvent.setStartTime(new XMLGregorianCalendarImpl());
        teamcityEvent.setStopTime(null);
        teamcityEvent.setStatus("passed");
        teamcityEvent.setTests(10);
        teamcityEvent.setFailed(1);
        teamcityEvent.setPassed(9);
        teamcityEvent.setJob(jobName);
        return teamcityEvent;
    }

    public AquaStartedEvent createAquaStartedEvent(String host){
        AquaEvent aquaEvent = new AquaStartedEvent();
        aquaEvent.setStartTime(new XMLGregorianCalendarImpl());
        return (AquaStartedEvent) fillAquaEvent(aquaEvent, host);
    }

    public AquaFinishedEvent createAquaFinishedEvent(String host){
        AquaEvent aquaEvent = new AquaFinishedEvent();
        aquaEvent.setStopTime(new XMLGregorianCalendarImpl());
        return (AquaFinishedEvent) fillAquaEvent(aquaEvent, host);
    }

    public AquaEvent fillAquaEvent(AquaEvent aquaEvent, String host){
        aquaEvent.setHost(host);
        aquaEvent.setDescription(aquaEvent.getHost() + " description");
        aquaEvent.setTests(10);
        aquaEvent.setFailed(1);
        aquaEvent.setPassed(9);
        return aquaEvent;
    }
}
