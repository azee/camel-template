package com.mycompany.template.utils;

import com.mycompany.template.beans.Competitor;
import com.mycompany.template.beans.Poll;
import com.mycompany.template.beans.StopPollMessage;
import com.mycompany.template.beans.Vote;
import org.springframework.stereotype.Service;

/**
 * Created by azee on 2/3/14.
 */
@Service
public class TestBeansGenerator {

    public Poll getPoll(String pollId){
        Poll poll = new Poll();
        poll.setPollId(pollId);

        poll.getCompetitors().add(getCompetitor("1"));
        poll.getCompetitors().add(getCompetitor("2"));
        poll.getCompetitors().add(getCompetitor("3"));

        return poll;
    }

    public Competitor getCompetitor(String id){
        Competitor competitor = new Competitor();
        competitor.setId(id);
        competitor.setName(id);
        return competitor;
    }

    public Vote getVote(String pollId, String competitorId){
        Vote vote = new Vote();
        vote.setCompetitorId(competitorId);
        vote.setPollId(pollId);
        return vote;
    }

    public StopPollMessage getStopPollMessage(String pollId){
        StopPollMessage message = new StopPollMessage();
        message.setPollId(pollId);
        return message;
    }

}
