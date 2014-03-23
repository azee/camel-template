package com.mycompany.template.api;

import com.mycompany.template.beans.Poll;
import com.mycompany.template.beans.StartPollMessage;
import com.mycompany.template.beans.StopPollMessage;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by IntelliJ IDEA.
 * User: azee
 * Date: 2/20/13
 * Time: 6:08 PM
 */
@Component
@Path("/poll")
public class PollService {

    @Produce(uri = "direct:poll")
    private ProducerTemplate pollQueue;

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/")
    public Response createPoll(Poll poll) throws Exception {
        StartPollMessage startPollMessage = new StartPollMessage();
        startPollMessage.setPoll(poll);
        pollQueue.sendBody(startPollMessage);
        return Response.ok().build();
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/")
    public Response stopPoll(String pollId) throws Exception {
        StopPollMessage stopPollMessage = new StopPollMessage();
        stopPollMessage.setPollId(pollId);
        pollQueue.sendBody(stopPollMessage);
        return Response.ok().build();
    }

}
