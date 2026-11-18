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

import java.sql.Timestamp;
import java.util.Collection;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * The newsletters read by template keep their unsubscribe option (they are updated when the template changes)
 */
public class NewsLetterDAOTest extends LuteceTestCase
{
    private static final int ID_TEMPLATE = 987654;

    @Test
    public void testUnsubscribeOfNewslettersByTemplate( )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        NewsLetter newsletter = new NewsLetter( );
        newsletter.setName( "unsubscribe test" );
        newsletter.setDescription( "unsubscribe test" );
        newsletter.setDateLastSending( new Timestamp( System.currentTimeMillis( ) ) );
        newsletter.setHtml( "<p>letter</p>" );
        newsletter.setNewsLetterTemplateId( ID_TEMPLATE );
        newsletter.setWorkgroup( "all" );
        newsletter.setUnsubscribe( "TRUE" );
        newsletter.setNbSections( 1 );
        NewsLetterHome.create( newsletter, plugin );

        try
        {
            Collection<NewsLetter> listNewsletters = NewsLetterHome.findAllByTemplateId( ID_TEMPLATE, plugin );
            assertEquals( 1, listNewsletters.size( ) );

            // As NewsletterService.modifySectionNumber does when the number of sections of the template changes
            NewsLetter newsletterOfTemplate = listNewsletters.iterator( ).next( );
            newsletterOfTemplate.setNbSections( 2 );
            NewsLetterHome.update( newsletterOfTemplate, plugin );

            NewsLetter newsletterLoaded = NewsLetterHome.findByPrimaryKey( newsletter.getId( ), plugin );
            assertEquals( "TRUE", newsletterLoaded.getUnsubscribe( ) );
            assertEquals( 2, newsletterLoaded.getNbSections( ) );
            assertEquals( "TRUE", NewsLetterHome.findAll( plugin ).stream( ).filter( n -> n.getId( ) == newsletter.getId( ) ).findFirst( ).get( ).getUnsubscribe( ) );
        }
        finally
        {
            NewsLetterHome.remove( newsletter.getId( ), plugin );
        }
    }
}
