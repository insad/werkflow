package com.werken.werkflow.definition.fundamental;

import com.werken.werkflow.definition.MessageInitiator;

import org.apache.commons.jelly.XMLOutput;

public class MessageInitiatorTag
    extends FundamentalTagSupport
    implements DocumentableTag
{
    private String id;
    private String type;
    private String documentation;

    public MessageInitiatorTag()
    {

    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return this.type;
    }

    public void setDocumentation(String documentation)
    {
        this.documentation = documentation;
    }

    public String getDocumentation()
    {
        return this.documentation;
    }

    public void doTag(XMLOutput output)
        throws Exception
    {
        requireStringAttribute( "id",
                                getId() );

        requireStringAttribute( "type",
                                getType() );

        ProcessTag process = (ProcessTag) requiredAncestor( "process",
                                                            ProcessTag.class );

        setDocumentation( null );

        invokeBody( output );

        MessageInitiator initiator = new MessageInitiator( getType(),
                                                           getId() );

        initiator.setDocumentation( getDocumentation() );

        process.addMessageInitiator( initiator );
    }
}
