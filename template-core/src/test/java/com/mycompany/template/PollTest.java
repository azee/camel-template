package com.mycompany.template;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.mycompany.template.beans.Competitor;
import com.mycompany.template.beans.Poll;
import com.mycompany.template.utils.TestBeansGenerator;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;


/**
 * Created by azee on 3/11/14.
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/camel-context.xml", "classpath:camel-test-beans.xml", "classpath:jms-test.xml"})
@DirtiesContext
@MockEndpoints("*")
@SuppressWarnings("SpringJavaAutowiringInspection")
public class PollTest {

    @Autowired
    TestBeansGenerator testBeansGenerator;

    @Autowired
    HazelcastInstance instance;

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
        String pollId = "Poll1";
        pollQueue.sendBody(testBeansGenerator.getPoll(pollId));
        pollAggregate.setExpectedMessageCount(2);
        pollAggregate.assertIsSatisfied();

        IMap map = instance.getMap("pollRepo");
        assertNotNull(map);

        Object state = map.get(pollId);
        assertNotNull(state);
        assertTrue(state instanceof Exchange);

        Object poll = ((Exchange) state).getIn().getBody();
        assertNotNull(poll);
        assertTrue(poll instanceof Poll);

        List<Competitor> competitors = ((Poll) poll).getCompetitors();
        assertNotNull(competitors);
        Assert.assertThat(competitors.size(), is(3));

        assertNotNull(competitors.get(0));
        assertNotNull(competitors.get(1));
        assertNotNull(competitors.get(2));
    }

    @DirtiesContext
    @Test
    public void ignoreInitialVoteTest() throws InterruptedException {
        pollQueue.sendBody(testBeansGenerator.getVote("Poll1", "1"));
        pollAggregate.setExpectedMessageCount(0);
        pollAggregate.assertIsSatisfied();
    }

}
