package ru.yandex.qatools.yard.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Created by azee on 2/3/14.
 */
public class SaveConfirmProcessor implements Processor {
    /**
     * Used to clear up a seda queue
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
    }
}
