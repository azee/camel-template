package com.mycompany.template.aggregators;

import com.mycompany.template.beans.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.fsm.annotations.*;

/**
 * Created by azee on 20.11.14.
 */
@Component
@FSM(start = UndefinedState.class)
@Transitions({
        @Transit(from = State.class, on = StopPollMessage.class, stop = true),
        @Transit(from = UndefinedState.class, to = PollRunningState.class, on = StartPollMessage.class),
        @Transit(from = PollRunningState.class, to = PollRunningState.class, on = VoteMessage.class)
})
public class PollFSM {
    private static final Logger logger = Logger.getLogger(PollFSM.class);

    @BeforeTransit
    public void onBeforeTransit(Message message){
        logger.info("Got message " + message.getClass());
    }

    @OnTransit
    public void pollStopped(State oldState, StopPollMessage message){

    }

    @OnTransit
    public void pollStarted(UndefinedState oldState, PollRunningState newState, StartPollMessage message){
        newState.setPoll(message.getPoll());
    }

    @OnTransit
    public void pollVoteRecieved(PollRunningState oldState, PollRunningState newState, VoteMessage message){
        oldState.setPoll(updateVote(oldState.getPoll(), message.getCompetitorId()));
        newState = oldState;
    }

    private Poll updateVote(Poll poll, String competitorId){
        for (Competitor competitor : poll.getCompetitors()){
            if (competitor.getId().equals(competitorId)){
                competitor.setVotes(competitor.getVotes() + 1);
            }
        }
        return poll;
    }

}
