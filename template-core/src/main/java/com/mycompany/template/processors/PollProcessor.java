package com.mycompany.template.processors;

import com.mycompany.template.beans.Message;
import com.mycompany.template.utils.MessageUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Created by azee on 3/28/14.
 */
public class PollProcessor implements Processor {

    private static final Logger log = LogManager.getLogger(PollProcessor.class);

    @Override
    public void process(Exchange exchange) {
        Message message = exchange.getIn().getBody(Message.class);
        exchange.getIn().setHeaders(MessageUtils.getHeaders(message.getPollId(), message.getClass()));
    }
}
