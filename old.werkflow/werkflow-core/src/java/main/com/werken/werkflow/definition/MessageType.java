package com.werken.werkflow.definition;

import com.werken.werkflow.service.messaging.MessageSelector;

public class MessageType
{
    public static final MessageType[] EMPTY_ARRAY = new MessageType[0];

    private String id;
    private String documentation;
    private MessageSelector selector;

    public MessageType(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }

    public void setDocumentation(String documentation)
    {
        this.documentation = documentation;
    }

    public String getDocumentation()
    {
        return this.documentation;
    }

    public void setMessageSelector(MessageSelector selector)
    {
        this.selector = selector;
    }

    public MessageSelector getMessageSelector()
    {
        return this.selector;
    }
}