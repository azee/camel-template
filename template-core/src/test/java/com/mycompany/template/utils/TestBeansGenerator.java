package com.mycompany.template.utils;

import com.mycompany.template.beans.*;
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

    public VoteMessage getVote(String pollId, String competitorId){
        VoteMessage voteMessage = new VoteMessage();
        voteMessage.setPollId(pollId);
        voteMessage.setCompetitorId(competitorId);
        return voteMessage;
    }

    public StopPollMessage getStopPollMessage(String pollId){
        StopPollMessage message = new StopPollMessage();
        message.setPollId(pollId);
        return message;
    }

    public StartPollMessage getStartPollMessage(String pollId){
        StartPollMessage message = new StartPollMessage();
        message.setPoll(getPoll(pollId));
        message.setPollId(pollId);
        return message;
    }
}
