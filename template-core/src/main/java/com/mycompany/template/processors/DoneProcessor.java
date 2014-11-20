package com.mycompany.template.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;


/**
 * Created by azee on 3/28/14.
 */
public class DoneProcessor implements Processor {

    private static final Logger logger = Logger.getLogger(DoneProcessor.class);

    @Override
    public void process(Exchange exchange) {
        logger.info("Done: " + exchange.getIn().getBody().getClass());
    }
}
