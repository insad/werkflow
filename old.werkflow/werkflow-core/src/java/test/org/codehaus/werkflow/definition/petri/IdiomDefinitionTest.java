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

import junit.framework.TestCase;

import java.util.Arrays;

public class IdiomDefinitionTest
    extends TestCase
{
    public void testConstruct()
    {
        IdiomDefinition idiomDef = new IdiomDefinition( "uri",
                                                        "empty",
                                                        IdiomDefinition.CONTAINS_NONE );

        assertEquals( "empty",
                      idiomDef.getId() );

        assertEquals( 0,
                      idiomDef.getParameters().length );

        assertEquals( 0,
                      idiomDef.getPlaces().length );

        assertEquals( 0,
                      idiomDef.getTransitions().length );

        assertEquals( 0,
                      idiomDef.getArcs().length );
    }

    public void testStaticTransitions()
        throws Exception
    {
        IdiomDefinition idiomDef = new IdiomDefinition( "uri",
                                                        "idiom",
                                                        IdiomDefinition.CONTAINS_NONE );

        TransitionDefinition transDef = new TransitionDefinition( "transition",
                                                                  "transition" );

        idiomDef.addTransition( transDef );

        assertEquals( 1,
                      idiomDef.getTransitions().length );

        assertEquals( transDef,
                      idiomDef.getTransitions()[0] );
    }

    public void testStaticPlaces()
        throws Exception
    {
        IdiomDefinition idiomDef = new IdiomDefinition( "uri",
                                                        "idiom",
                                                        IdiomDefinition.CONTAINS_NONE );

        PlaceDefinition placeDef = new PlaceDefinition( "place",
                                                        "place" );

        idiomDef.addPlace( placeDef );

        assertEquals( 1,
                      idiomDef.getPlaces().length );

        assertEquals( placeDef,
                      idiomDef.getPlaces()[0] );
    }

    public void testStaticArcs()
        throws Exception
    {
        IdiomDefinition idiomDef = new IdiomDefinition( "uri",
                                                        "idiom",
                                                        IdiomDefinition.CONTAINS_NONE );

        ArcDefinition arcDef = ArcDefinition.newArcFromPlaceToTransition( "my.place",
                                                                          "my.transition",
                                                                          "" );

        idiomDef.addArc( arcDef );

        assertEquals( 1,
                      idiomDef.getArcs().length );

        assertEquals( arcDef,
                      idiomDef.getArcs()[0] );
    }

    public void testStaticNewIdiom()
        throws Exception
    {
        IdiomDefinition idiomDef = new IdiomDefinition( "uri",
                                                        "idiom",
                                                        IdiomDefinition.CONTAINS_NONE );

        PlaceDefinition placeDef = new PlaceDefinition( "my.place",
                                                        "place.docs" );

        idiomDef.addPlace( placeDef );

        TransitionDefinition transDef = new TransitionDefinition( "my.transition",
                                                                  "transition.docs" );

        idiomDef.addTransition( transDef );

        ArcDefinition arcDef = ArcDefinition.newArcFromPlaceToTransition( "my.place",
                                                                          "my.transition",
                                                                          "" );

        idiomDef.addArc( arcDef );

        Idiom idiom = idiomDef.newIdiom();

        idiom.build();

        assertEquals( 3,
                      idiom.getPlaces().length );

        assertContainsPlace( "in",
                             idiom.getPlaces() );

        assertContainsPlace( "out",
                             idiom.getPlaces() );

        assertContainsPlace( "/idiom:my.place",
                             idiom.getPlaces() );

        assertEquals( 1,
                      idiom.getTransitions().length );

        Place place = idiom.getPlace( "/idiom:my.place" );

        assertEquals( 1,
                      place.getArcsToTransitions().length );

        Transition transition = idiom.getTransitions()[0];

        assertEquals( "/idiom:my.transition",
                      transition.getId() );

        assertEquals( 1,
                      transition.getArcsFromPlaces().length );

        assertSame( place.getArcsToTransitions()[0],
                    transition.getArcsFromPlaces()[0] );
    }

    public void testGraft()
        throws Exception
    {
        IdiomDefinition parentDef = new IdiomDefinition( "uri",
                                                        "parent",
                                                         IdiomDefinition.CONTAINS_MULTI_COMPONENTS );

        parentDef.addPlace( new PlaceDefinition( "head",
                                                 "head" ) );

        parentDef.addPlace( new PlaceDefinition( "tail",
                                                 "tail" ) );

        parentDef.addTransition( new TransitionDefinition( "component",
                                                           "replace me" ) );

        parentDef.addArc( ArcDefinition.newArcFromPlaceToTransition( "head",
                                                                     "component",
                                                                     "" ) );

        parentDef.addArc( ArcDefinition.newArcFromTransitionToPlace( "component",
                                                                     "tail",
                                                                     "" ) );

        IdiomDefinition childDef = new IdiomDefinition( "uri",
                                                        "child",
                                                        IdiomDefinition.CONTAINS_NONE );

        childDef.addTransition( new TransitionDefinition( "trans",
                                                          "trans" ) );

        childDef.addArc( ArcDefinition.newArcFromPlaceToTransition( "in",
                                                                    "trans",
                                                                    "" ) );

        childDef.addArc( ArcDefinition.newArcFromTransitionToPlace( "trans",
                                                                    "out",
                                                                    "" ) );

        Idiom parentIdiom = parentDef.newIdiom();

        parentIdiom.build();

        Idiom childIdiom = parentIdiom.addComponent( childDef );

        childIdiom.build();
        childIdiom.complete();
        parentIdiom.complete();

        Place place = null;
        Arc[] arcs = null;

        place = parentIdiom.getPlace( "/parent:head" );

        arcs = place.getArcsToTransitions();

        assertEquals( 1,
                      arcs.length );

        assertEquals( "/parent/child[0]:trans",
                      arcs[0].getTransition().getId() );

        place = parentIdiom.getPlace( "/parent:tail" );

        arcs = place.getArcsFromTransitions();

        assertEquals( 1,
                      arcs.length );

        assertEquals( "/parent/child[0]:trans",
                      arcs[0].getTransition().getId() );

        Transition trans = parentIdiom.getTransition( "/parent/child[0]:trans" );

        arcs = trans.getArcsFromPlaces();

        assertEquals( 1,
                      arcs.length );

        assertEquals( "/parent:head",
                      arcs[0].getPlace().getId() );

        arcs = trans.getArcsToPlaces();

        assertEquals( 1,
                      arcs.length );

        assertEquals( "/parent:tail",
                      arcs[0].getPlace().getId() );
    }

    protected void assertContainsPlace(String id,
                                       Place[] places)
    {
        for ( int i = 0 ; i < places.length ; ++i )
        {
            if ( places[i].getId().equals( id ) )
            {
                return;
            }
        }

        fail( "no place [" + id + "] in " + Arrays.asList( places ) );
    }
}
