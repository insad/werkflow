package org.codehaus.werkflow.engine;

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

import org.codehaus.werkflow.Wfms;
import org.codehaus.werkflow.WfmsRuntime;
import org.codehaus.werkflow.Attributes;
import org.codehaus.werkflow.ProcessInfo;
import org.codehaus.werkflow.ProcessCase;
import org.codehaus.werkflow.ProcessException;
import org.codehaus.werkflow.QueryException;
import org.codehaus.werkflow.admin.WfmsAdmin;
import org.codehaus.werkflow.admin.DeploymentException;
import org.codehaus.werkflow.core.WorkflowCore;
import org.codehaus.werkflow.definition.ProcessDefinition;
import org.codehaus.werkflow.event.WfmsEventListener;
import org.codehaus.werkflow.service.WfmsServices;
import org.codehaus.werkflow.service.persistence.PersistenceException;

public class WorkflowEngine
    implements Wfms, WfmsAdmin, WfmsRuntime
{
    // ----------------------------------------------------------------------
    //     Instance members
    // ----------------------------------------------------------------------

    private WfmsServices services;
    private WorkflowCore core;

    // ----------------------------------------------------------------------
    //     Constructors
    // ----------------------------------------------------------------------

    public WorkflowEngine(WfmsServices services)
    {
        this.services = services;
        this.core     = WorkflowCore.newCore( services.getPersistenceManager(),
                                              services.getMessagingManager() );
    }

    // ----------------------------------------------------------------------
    //     Instance methods
    // ----------------------------------------------------------------------

    public WfmsAdmin getAdmin()
    {
        return this;
    }

    public WfmsRuntime getRuntime()
    {
        return this;
    }

    protected WorkflowCore getCore()
    {
        return this.core;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    /** @see WfmsRuntime
     */
    public ProcessInfo[] getProcesses()
    {
        return getCore().getProcesses();
    }

    /** @see WfmsRuntime
     */
    public ProcessInfo getProcess(String packageId,
                                  String processId)

        throws ProcessException
    {
        return getCore().getProcess( packageId,
                                     processId );
    }

    /** @see WfmsRuntime
     */
    public ProcessCase getProcessCase(String packageId,
                                      String processId,
                                      String caseId)
        throws ProcessException
    {
        return getCore().getProcessCase( packageId,
                                         processId,
                                         caseId );
    }

    /** @see WfmsRuntime
     */
    public ProcessCase callProcess(String packageId,
                                   String processId,
                                   Attributes attributes)
        throws ProcessException
    {
        try
        {
            return getCore().callProcess( packageId,
                                          processId,
                                          attributes );
        }
        catch (PersistenceException e)
        {
            throw new  ProcessException( packageId,
                                         processId,
                                         e );
        }
    }

    /** @see WfmsRuntime
     */
    public ProcessCase[] selectCases(String packageId,
                                     String processId,
                                     String placeId)
        throws QueryException
    {
        return null;
    }

    /** @see WfmsRuntime
     */
    public ProcessCase[] selectCases(String packageId,
                                     String processId,
                                     Attributes qbeAttrrs)
        throws QueryException
    {
        return null;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

    public void deployProcess(ProcessDefinition processDef)
        throws DeploymentException
    {
        getCore().deployProcess( processDef );
    }

    public void addEventListener(WfmsEventListener listener)
    {

    }

}
