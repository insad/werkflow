package org.codehaus.werkflow.definition.petri;

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

import org.codehaus.werkflow.definition.Scope;
import org.codehaus.werkflow.definition.Waiter;
import org.codehaus.werkflow.expr.Expression;
import org.codehaus.werkflow.task.DefaultTask;
import org.codehaus.werkflow.work.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Idiom
    implements Net
{
    public static final Idiom[] EMPTY_ARRAY = new Idiom[0];

    private boolean staticBuilt;

    private Idiom parent;
    private int index;
    private Scope scope;

    private IdiomDefinition idiomDef;
    private Map parameters;
    private List components;
    private Map stashed;

    private Map places;
    private Map transitions;

    Idiom(IdiomDefinition idiomDef)
    {
        this( idiomDef,
              null,
              -1 );
    }

    Idiom(IdiomDefinition idiomDef,
          Idiom parent,
          int index)
    {
        this.idiomDef   = idiomDef;
        this.parameters = new HashMap();
        this.components = new ArrayList();
        this.stashed    = new HashMap();

        this.places      = new HashMap();
        this.transitions = new HashMap();

        this.parent = parent;
        this.index  = index;
    }

    public String getId()
    {
        String id = "";

        if ( this.parent != null )
        {
            id = parent.getId();
        }

        id = id + "/" + idiomDef.getId();

        if ( this.index >= 0 )
        {
            id = id + "[" + this.index + "]";
        }

        return id;
    }

    public IdiomDefinition getIdiomDefinition()
    {
        return this.idiomDef;
    }

    public Idiom getParent()
    {
        return this.parent;
    }

    public int getIndex()
    {
        return this.index;
    }

    public void setParameter(String name,
                             Object value)
        throws NoSuchParameterException, InvalidParameterTypeException
    {
        IdiomParameter param = getIdiomDefinition().getParameter( name );

        this.parameters.put( name,
                             value );
    }

    public Object getParameter(String name)
        throws NoSuchParameterException
    {
        // just call to see if no-such should throw.

        getIdiomDefinition().getParameter( name );

        return this.parameters.get( name );
    }

    public void verify()
        throws IdiomException
    {
        verifyParameters();
    }

    public void build()
        throws IdiomException
    {
        verify();

        getIdiomDefinition().buildStatic( this );

        /*
        if ( getParent() != null )
        {
            getParent().addComponent( this );
        }
        */
    }

    public void complete()
        throws IdiomException
    {
        getIdiomDefinition().buildComplete( this );

        if ( getParent() != null )
        {
            getParent().addComponent( this );
        }
    }

    protected void verifyParameters()
        throws IdiomException
    {
        IdiomParameter[] params = getIdiomDefinition().getParameters();

        for ( int i = 0 ; i < params.length ; ++i )
        {
            if ( params[i].isRequired() )
            {
                if ( ! this.parameters.containsKey( params[i].getId() ) )
                {
                    throw new MissingParameterException( params[i] );
                }
            }
        }
    }

    public void addAction(Action action)
        throws IdiomException
    {
        getIdiomDefinition().addAction( this,
                                        action );
    }

    public Idiom addComponent(IdiomDefinition idiomDef)
    {
        Idiom idiom = idiomDef.newIdiom( this,
                                         this.components.size() );

        this.components.add( idiom );

        return idiom;
    }

    private void addComponent(Idiom component)
        throws IdiomException
    {
        getIdiomDefinition().addComponent( this,
                                           component );
    }

    public Idiom[] getComponents()
    {
        return (Idiom[]) this.components.toArray( EMPTY_ARRAY );
    }

    void addPlace(String id,
                  String documentation)
    {
        DefaultPlace place = new DefaultPlace( id );

        place.setDocumentation( documentation );

        this.places.put( id,
                         place );
    }

    void removePlace(String id)
    {
        this.places.remove( id );
    }

    public Place getPlace(String id)
    {
        Place place = (Place) this.places.get( id );
        
        if ( place == null )
        {
            Idiom[] components = getComponents();

            for ( int i = 0 ; ( i < components.length ) && ( place == null ) ; ++i )
            {
                place = components[i].getPlace( id );
            }
        }
        return place;
    }

    public Place[] getPlaces()
    {
        List places = new ArrayList();

        getPlaces( places );

        return (Place[]) places.toArray( Place.EMPTY_ARRAY );
    }

    void getPlaces(List dest)
    {
        dest.addAll( this.places.values() );

        Idiom[] components = getComponents();

        for ( int i = 0 ; i < components.length ; ++i )
        {
            components[i].getPlaces( dest );
        }
    }

    DefaultPlace getMutablePlace(String id)
    {
        if ( id.equals( IdiomDefinition.GLOBAL_OUT ) )
        {
            if ( getParent() != null )
            {
                return getParent().getMutablePlace( id );
            }
            else
            {
                return getOutPlace();
            }
        }
        return (DefaultPlace) this.places.get( id );
    }

    DefaultPlace getInPlace()
    {
        return (DefaultPlace) this.places.get( IdiomDefinition.IN_PLACE );
    }

    DefaultPlace getOutPlace()
    {
        return (DefaultPlace) this.places.get( IdiomDefinition.OUT_PLACE );
    }

    void addTransition(String id,
                       String documentation,
                       Action action,
                       Waiter waiter)
    {
        DefaultTransition transition = new DefaultTransition( id );

        transition.setActivationRule( AndInputRule.getInstance() );

        transition.setDocumentation( documentation );

        transition.setWaiter( waiter );

        try
        {
            if ( action != null )
            {
                DefaultTask task = new DefaultTask();

                task.setAction( action );

                transition.setTask( task );
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.transitions.put( id,
                              transition );
    }

    public Transition getTransition(String id)
    {
        Transition trans = (Transition) this.transitions.get( id );

        if ( trans == null )
        {
            Idiom[] components = getComponents();

            for ( int i = 0 ; ( i < components.length ) && ( trans == null ) ; ++i )
            {
                trans = components[i].getTransition( id );
            }
        }

        return trans;
    }

    public Transition[] getTransitions()
    {
        List transitions = new ArrayList();

        getTransitions( transitions );

        return (Transition[]) transitions.toArray( Transition.EMPTY_ARRAY );
    }

    void getTransitions(List dest)
    {
        dest.addAll( this.transitions.values() );

        Idiom[] components = getComponents();

        for ( int i = 0 ; i < components.length ; ++i )
        {
            components[i].getTransitions( dest );
        }
    }

    DefaultTransition getMutableTransition(String id)
    {
        return (DefaultTransition) this.transitions.get( id );
    }

    void connectPlaceToTransition(String placeId,
                                  String transitionId,
                                  Expression expression)
    {
        DefaultPlace place = getMutablePlace( placeId );
        DefaultTransition transition = getMutableTransition( transitionId );

        DefaultArc arc = new DefaultArc( place,
                                         transition );

        place.addOutboundArc( arc );
        transition.addInboundArc( arc );

        arc.setExpression( expression );
    }

    void connectTransitionToPlace(String transitionId,
                                  String placeId,
                                  Expression expression)
    {
        DefaultPlace place = getMutablePlace( placeId );
        DefaultTransition transition = getMutableTransition( transitionId );

        DefaultArc arc = new DefaultArc( place,
                                         transition );

        transition.addOutboundArc( arc );
        place.addInboundArc( arc );

        arc.setExpression( expression );

    }

    void graftComponentInput(String placeId,
                             Idiom component,
                             Expression expression)
    {
        DefaultPlace place = getMutablePlace( placeId );

        DefaultPlace componentIn = component.getInPlace();

        Arc[] componentArcs = componentIn.getArcsToTransitions();
        DefaultTransition componentTransition = null;

        DefaultArc graftArc = null;

        for ( int i = 0 ; i < componentArcs.length ; ++i )
        {
            componentTransition = (DefaultTransition) componentArcs[i].getTransition();

            graftArc = new DefaultArc( place,
                                       componentTransition );

            graftArc.setExpression( componentArcs[i].getExpression() );

            componentIn.removeOutboundArc( componentArcs[i] );
            componentTransition.removeInboundArc( componentArcs[i] );

            place.addOutboundArc( graftArc );
            componentTransition.addInboundArc( graftArc );
        }

        component.replaceIn( place );

        // component.removePlace( componentIn.getId() );
    }

    void graftComponentOutput(Idiom component,
                              String placeId,
                              Expression expression)
    {
        DefaultPlace place = getMutablePlace( placeId );

        DefaultPlace componentOut = component.getOutPlace();

        Arc[] componentArcs = componentOut.getArcsFromTransitions();
        DefaultTransition componentTransition = null;

        DefaultArc graftArc = null;

        for ( int i = 0 ; i < componentArcs.length ; ++i )
        {
            componentTransition = (DefaultTransition) componentArcs[i].getTransition();

            graftArc = new DefaultArc( place,
                                       componentTransition );

            graftArc.setExpression( componentArcs[i].getExpression() );

            componentOut.removeInboundArc( componentArcs[i] );
            componentTransition.removeOutboundArc( componentArcs[i] );

            place.addInboundArc( graftArc );
            componentTransition.addOutboundArc( graftArc );
        }


        // component.removePlace( componentOut.getId() );
    }

    void replaceIn(DefaultPlace newIn)
    {
        Iterator          transIter = this.transitions.values().iterator();
        DefaultTransition eachTrans = null;

        while ( transIter.hasNext() )
        {
            eachTrans = (DefaultTransition) transIter.next();

            replaceIn( newIn,
                       eachTrans );
        }

        Iterator componentIter = this.components.iterator();
        Idiom    eachComponent = null;

        while ( componentIter.hasNext() )
        {
            eachComponent = (Idiom) componentIter.next();

            eachComponent.replaceIn( newIn );
        }
    }

    void replaceIn(DefaultPlace newIn,
                   DefaultTransition transition)
    {
        Arc[] arcs = null;

        arcs = transition.getInboundArcs();

        for ( int i = 0 ; i < arcs.length ; ++i )
        {
            if ( arcs[i].getPlace().getId().equals( getInPlace().getId() ) )
            {
                DefaultArc newArc = new DefaultArc( newIn,
                                                    transition );

                newArc.setExpression( arcs[i].getExpression() );

                transition.replaceInboundArc( arcs[i],
                                              newArc );
            }
        }

        arcs = transition.getOutboundArcs();

        for ( int i = 0 ; i < arcs.length ; ++i )
        {
            if ( arcs[i].getPlace().getId().equals( getInPlace().getId() ) )
            {
                DefaultArc newArc = new DefaultArc( newIn,
                                                    transition );

                newArc.setExpression( arcs[i].getExpression() );

                transition.replaceOutboundArc( arcs[i],
                                               newArc );
            }
        }
    }

    /*
    void connectPlaceToIdiom(String placeId,
                             Idiom idiom)
    {
        DefaultPlace place = getMutablePlace( placeId );
        DefaultTransition transition = idiom.getMutableInTransition();

        DefaultArc arc = new DefaultArc( place,
                                         transition );

        place.addOutboundArc( arc );
        transition.addInboundArc( arc );
    }

    void connectIdiomToPlace(Idiom idiom,
                             String placeId)
    {
        DefaultPlace place = getMutablePlace( placeId );
        DefaultTransition transition = idiom.getMutableInTransition();

        DefaultArc arc = new DefaultArc( place,
                                         transition );

        transition.addOutboundArc( arc );
        place.addInboundArc( arc );
    }
    */

    void stash(String id,
               String value)
    {
        this.stashed.put( id,
                          value );
    }

    void stashAll(Map stashings)
    {
        this.stashed.putAll( stashings );
    }

    String getStashed(String id)
    {
        return (String) this.stashed.get( id );
    }

    public void setScope(Scope scope)
    {
        this.scope = scope;
    }

    public Scope getScope()
    {
        return this.scope;
    }

    public String toString()
    {
        return "[Idiom: " + getIdiomDefinition() + "]";
    }

    public void dump()
    {
        Set seen = new HashSet();

        dump( seen,
              getInPlace(),
              "" );
    }

    protected void dump(Set seen,
                        Place place,
                        String indent)
    {
        System.err.println( indent + place.getId() );

        if ( seen.contains( place ) )
        {
            return;
        }

        seen.add( place );

        Arc[] arcs = place.getArcsToTransitions();

        for ( int i = 0 ; i < arcs.length ; ++i )
        {
            //System.err.println( indent + "  " + arcs[i] );

            dump( seen,
                  arcs[i].getTransition(),
                  indent + "    " );
        }
    }

    protected void dump(Set seen,
                        Transition transition,
                        String indent)
    {
        System.err.println( indent + transition.getId() );

        if ( seen.contains( transition ) )
        {
            return;
        }

        seen.add( transition );

        Arc[] arcs = transition.getArcsToPlaces();

        for ( int i = 0 ; i < arcs.length ; ++i )
        {
            //System.err.println( indent + "  " + arcs[i] );

            dump( seen,
                  arcs[i].getPlace(),
                  indent + "    " );
        }

    }
}
