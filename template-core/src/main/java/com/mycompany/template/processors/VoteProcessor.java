package com.mycompany.template.processors;

import com.mycompany.template.beans.Vote;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by azee on 3/28/14.
 */
public class VoteProcessor implements Processor {

    private static final Logger log = LogManager.getLogger(VoteProcessor.class);

    @Override
    public void process(Exchange exchange) {
        Vote vote = exchange.getIn().getBody(Vote.class);

        //ToDo: update header
        exchange.getIn().setBody(vote);
    }
}
