package ru.yandex.qatools.yard.camel;

import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.yard.events.CommitEvent;
import ru.yandex.qatools.yard.events.EventType;
import ru.yandex.qatools.yard.events.GithubCommit;
import ru.yandex.qatools.yard.events.GithubEvent;
import ru.yandex.qatools.yard.projects.Project;
import ru.yandex.qatools.yard.services.ProjectService;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by azee on 1/31/14.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class GithubCommitMessageProcessor {

    @Autowired
    ProjectService projectService;

    public List<CommitEvent> commits(GithubEvent githubEvent){
        List<CommitEvent> commits = new LinkedList<CommitEvent>();

        for (GithubCommit githubCommit : githubEvent.getCommits()){
            commits.add(transformCommitEvent(githubCommit, githubEvent));
        }
        return commits;
    }


    private CommitEvent transformCommitEvent(GithubCommit githubCommit, GithubEvent githubEvent){
        CommitEvent commitEvent = new CommitEvent();
        commitEvent.setProjectId(githubEvent.getProjectId());
        commitEvent.setSha(githubCommit.getId());
        commitEvent.setUser(githubCommit.getCommitter().getName());
        commitEvent.setRepository(githubEvent.getRepository().getUrl());
        commitEvent.setTitle("GitHub commit " + githubCommit.getId().substring(0, 6));
        commitEvent.setSource("github");
        commitEvent.setUrl(githubCommit.getUrl());
        commitEvent.setDescription(githubCommit.getMessage());
        commitEvent.setStartTime(githubCommit.getTimestamp());
        commitEvent.setType(EventType.COMMIT.value());
        return commitEvent;
    }
}

