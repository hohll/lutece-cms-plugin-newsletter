/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.newsletter.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * The properties of the newsletters, and the editor of their composition
 */
public class NewsletterPropertiesHomeTest extends LuteceTestCase
{
    private Plugin _plugin;
    private NewsLetterProperties _propertiesInitial;

    /**
     * Keeps the properties of the database, restored after the test
     *
     * @throws Exception
     *             if the test context cannot be set up
     */
    @BeforeEach
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        _plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        _propertiesInitial = NewsletterPropertiesHome.find( _plugin );
    }

    /**
     * Restores the properties of the database
     *
     * @throws Exception
     *             if the test context cannot be torn down
     */
    @AfterEach
    @Override
    protected void tearDown( ) throws Exception
    {
        NewsletterPropertiesHome.update( _propertiesInitial, _plugin );
        super.tearDown( );
    }

    /**
     * The editor chosen is stored with the other properties
     */
    @Test
    public void testEditor( )
    {
        NewsLetterProperties properties = NewsletterPropertiesHome.find( _plugin );
        properties.setEditor( NewsLetterProperties.EDITOR_LUTECE );
        NewsletterPropertiesHome.update( properties, _plugin );

        NewsLetterProperties propertiesStored = NewsletterPropertiesHome.find( _plugin );
        assertEquals( NewsLetterProperties.EDITOR_LUTECE, propertiesStored.getEditor( ), "the editor of the back office is stored" );
        assertTrue( propertiesStored.isLuteceEditor( ), "the newsletters are composed with the editor of the back office" );
        assertEquals( properties.isValidationActive( ), propertiesStored.isValidationActive( ), "the other properties are kept" );

        properties.setEditor( NewsLetterProperties.EDITOR_GRAPESJS );
        NewsletterPropertiesHome.update( properties, _plugin );
        assertFalse( NewsletterPropertiesHome.find( _plugin ).isLuteceEditor( ), "back to GrapesJS" );
    }

    /**
     * GrapesJS is the editor by default and when no editor is given; a rich text editor of the back office may be named
     */
    @Test
    public void testEditorByDefault( )
    {
        NewsLetterProperties properties = new NewsLetterProperties( );
        assertEquals( NewsLetterProperties.EDITOR_GRAPESJS, properties.getEditor( ), "GrapesJS by default" );
        assertFalse( properties.isLuteceEditor( ), "GrapesJS is not a rich text editor of the back office" );

        properties.setEditor( " " );
        assertEquals( NewsLetterProperties.EDITOR_GRAPESJS, properties.getEditor( ), "no editor is GrapesJS" );

        properties.setEditor( null );
        assertEquals( NewsLetterProperties.EDITOR_GRAPESJS, properties.getEditor( ), "no editor is GrapesJS" );

        properties.setEditor( "milkdown" );
        assertEquals( "milkdown", properties.getEditor( ), "an editor of the back office is named" );
        assertTrue( properties.isLuteceEditor( ), "a named editor is a rich text editor of the back office" );
    }
}
