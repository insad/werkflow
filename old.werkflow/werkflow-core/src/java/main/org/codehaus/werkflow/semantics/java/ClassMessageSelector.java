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

import org.codehaus.werkflow.expr.Expression;
import org.codehaus.werkflow.expr.SimpleExpressionContext;
import org.codehaus.werkflow.service.messaging.simple.SimpleMessageSelector;

/** Java class-based <code>SimpleMessageSelector</code> implementation.
 *
 *  <p>
 *  This implementation differentiates/selects messages based upon
 *  their <code>java.lang.Class</code> and optionally provides for
 *  additional filtering using a jelly <code>Expression</code>.
 *  </p>
 *
 *  @see Expression
 *  @see java.lang.Object#getClass
 *
 *  @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 *
 *  @version $Id$
 */
public class ClassMessageSelector
    extends SimpleMessageSelector
{
    // ----------------------------------------------------------------------
    //     Instance members
    // ----------------------------------------------------------------------

    /** Message differention class. */
    private Class messageClass;

    /** Filtering expression, possibly null. */
    private Expression expression;

    // ----------------------------------------------------------------------
    //     Constructors
    // ----------------------------------------------------------------------

    /** Construct.
     *
     *  @param messageClass The message selection class.
     */
    public ClassMessageSelector( Class messageClass )
    {
        this( messageClass,
              null );
    }

    /** Construct.
     *
     *  @param messageClass The message selection class.
     *  @param expression The message filtering expression.
     */
    public ClassMessageSelector( Class messageClass,
                                 Expression expression )
    {
        this.messageClass = messageClass;
        this.expression = expression;
    }

    // ----------------------------------------------------------------------
    //     Instance methods
    // ----------------------------------------------------------------------

    /** Retrieve the message differentiator class.
     *
     *  @return The selection class.
     */
    public Class getMessageClass()
    {
        return this.messageClass;
    }

    /** Retrieve the optional filtering <code>Expression</code>.
     *
     *  @return The filter expression, or <code>null</code> if none.
     */
    public Expression getExpression()
    {
        return this.expression;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /** @see org.codehaus.werkflow.service.messaging.simple.SimpleMessageSelector
     */
    public boolean selects( Object message )
        throws Exception
    {
        boolean selects = getMessageClass().isInstance( message );

        if ( !selects )
        {
            return false;
        }

        if ( getExpression() == null )
        {
            return true;
        }

        SimpleExpressionContext context = new SimpleExpressionContext();

        context.setValue( "message", message );

        boolean result = getExpression().evaluateAsBoolean( context );

        return result;
    }
}
