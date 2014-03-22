package com.mycompany.template.api.jackson;

import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created by IntelliJ IDEA.
 * User: azee
 * Date: 2/20/13
 * Time: 6:08 PM
 */
@Component
@Path("/poll")
public class PollService {

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/")
    public Response createPoll(Poll poll) throws Exception {
        //ToDo: push into a queue
        return Response.ok().build();
    }

    @DELETE
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/")
    public Response stopPoll(String pollId) throws Exception {
        //ToDo: push into a queue
        return Response.ok().build();
    }

}
