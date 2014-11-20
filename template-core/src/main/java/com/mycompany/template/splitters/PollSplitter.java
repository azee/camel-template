package com.mycompany.template.splitters;

import com.mycompany.template.beans.Message;
import com.mycompany.template.beans.Poll;
import com.mycompany.template.beans.StartPollMessage;
import org.apache.commons.lang3.SerializationUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by azee on 3/28/14.
 */
public class PollSplitter {
    public List<Message> split(Message message){
        //Splitter is used just as an example
        List<Message> messages = new LinkedList<>();
        Message newMessage = SerializationUtils.clone(message);
        newMessage.setPollId("inner_" + message.getPollId());
        messages.add(message);
        messages.add(newMessage);
        return messages;
    }
}
