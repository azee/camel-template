package com.mycompany.template;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;


/**
 * Created by azee on 3/11/14.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/camel-context.xml", "classpath:camel-test-beans.xml", "classpath:jms-test.xml"})
@DirtiesContext
@MockEndpoints("*")
public class PollTest {

    @EndpointInject(uri = "mock:activemq:queue:poll.aggregate")
    protected MockEndpoint pollAggregate;

    @Produce(uri = "activemq:queue:poll")
    protected ProducerTemplate pollQueue;

    @Before
    public void before() {
        pollAggregate.reset();
        pollAggregate.setAssertPeriod(3000);
    }

    @DirtiesContext
    @Test
    public void createPollTest() throws InterruptedException {

    }

}
