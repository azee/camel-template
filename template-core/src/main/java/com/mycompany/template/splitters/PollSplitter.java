package com.mycompany.template.splitters;

import com.mycompany.template.beans.Poll;
import org.apache.commons.lang3.SerializationUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by azee on 3/28/14.
 */
public class PollSplitter {
    public List<Poll> split(Poll poll){
        //Splitter is used just as an example

        List<Poll> polls = new LinkedList<>();

        Poll newPoll = SerializationUtils.clone(poll);
        newPoll.setPollId("inner_" + poll.getPollId());

        polls.add(poll);
        polls.add(newPoll);
        return polls;
    }
}
