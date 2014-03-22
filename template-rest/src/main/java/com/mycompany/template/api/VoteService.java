package com.mycompany.template.api;

import com.mycompany.template.services.SomeBeanService;
import com.mycompany.template.beans.Pager;
import com.mycompany.template.beans.SomeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: azee
 * Date: 2/20/13
 * Time: 6:08 PM
 */
@Component
@Path("/vote")
public class VoteService {

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/")
    public Response pushVote(Vote vote) throws Exception {
        //ToDo: push into a queue
        return Response.ok().build();
    }

}
