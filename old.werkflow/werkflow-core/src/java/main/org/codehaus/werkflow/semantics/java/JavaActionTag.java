package org.codehaus.werkflow.semantics.java;

/*
 $Id$

 Copyright 2003 (C) The Codehaus. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "werkflow" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "werkflow"
    nor may "werkflow" appear in their names without prior written
    permission of The Codehaus. "werkflow" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://werkflow.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

import org.codehaus.werkflow.syntax.fundamental.AbstractActionTag;

import org.apache.bsf.BSFException;

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.JellyTagException;

/** Jelly tag for &lt;java:action&gt;.
 *
 *  @see JavaAction
 *  @see org.codehaus.werkflow.work.Action
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 *
 *  @version $Id$
 */
public class JavaActionTag
    extends AbstractActionTag
{
    // ----------------------------------------------------------------------
    //     Constants
    // ----------------------------------------------------------------------

    /** Default exeuction/functor method name. */
    public static final String DEFAULT_METHOD_NAME = "execute";

    // ----------------------------------------------------------------------
    //     Instance members
    // ----------------------------------------------------------------------

    /** Bean class name. */
    private String className;

    /** Bean method name. */
    private String methodName;

    /** Live Bean to use as the action. */
    private Object bean;

    // ----------------------------------------------------------------------
    //     Constructors
    // ----------------------------------------------------------------------

    /** Construct.
     */
    public JavaActionTag()
    {
        this.methodName = DEFAULT_METHOD_NAME;
    }

    // ----------------------------------------------------------------------
    //     Instance methods
    // ----------------------------------------------------------------------

    public Object getBean()
    {
        return bean;
    }

    public void setBean( Object bean )
    {
        this.bean = bean;
    }

    /** Set the bean class type.
     *
     *  @param className The class-name type.
     */
    public void setType(String className)
    {
        this.className = className;
    }

    /** Retrieve the bean class type.
     *
     *  @return The class-name type.
     */
    public String getType()
    {
        return this.className;
    }

    /** Set the bean execute method name.
     *
     *  @param method The method name.
     */
    public void setMethod(String method)
    {
        this.methodName = method;
    }

    /** Retrieve the bean execute method name.
     *
     *  @return The method name.
     */
    public String getMethod()
    {
        return this.methodName;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /** @see org.apache.commons.jelly.Tag
     */
    public void doTag( XMLOutput output )
        throws JellyTagException
    {
        if ( this.bean != null )
        {
            setupBean( bean );
        }
        else
        {
            if ( this.className == null )
            {
                doScriptTag( output );
            }
            else
            {
                doClassTag( output );
            }
        }
    }

    public void doScriptTag(XMLOutput output)
        throws JellyTagException
    {
        String text = getBodyText( false );

        try
        {
            JavaBsfAction action = new JavaBsfAction( text );

            setAction( action );
        }
        catch (BSFException e)
        {
            throw new JellyTagException( e );
        }
    }

    public void doClassTag(XMLOutput output)
        throws JellyTagException
    {
        ClassLoader cl = getContext().getClassLoader();

        if ( cl == null )
        {
            cl = Thread.currentThread().getContextClassLoader();
        }

        if ( cl == null )
        {
            cl = getClass().getClassLoader();
        }

        try
        {
            Class beanClass = cl.loadClass( getType() );

            Object bean = beanClass.newInstance();

            setupBean( bean );
        }
        catch (Exception e)
        {
            throw new JellyTagException( e );
        }
    }

    private void setupBean( Object bean )
        throws JellyTagException
    {
        JavaAction action = new JavaAction( bean,
                                            getMethod() );

        setAction( action );
    }
}
