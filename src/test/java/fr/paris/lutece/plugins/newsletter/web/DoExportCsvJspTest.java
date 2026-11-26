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
package fr.paris.lutece.plugins.newsletter.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * DoExportCsv.jsp writes the CSV through the response output stream (NewsletterJspBean.doExportCsv): the JSP must then write nothing
 * more through its own writer, otherwise the container fails at the end of the page with « OutputStream already obtained »
 * (SRVE0199E on Open Liberty) and logs a 500.
 */
public class DoExportCsvJspTest
{
    private static final Path JSP = Path.of( "webapp/jsp/admin/plugins/newsletter/DoExportCsv.jsp" );

    @Test
    public void testNoTemplateTextAroundTheExport( ) throws Exception
    {
        String strJsp = Files.readString( JSP, StandardCharsets.UTF_8 );
        assertTrue( strJsp.contains( "trimDirectiveWhitespaces=\"true\"" ), "the line breaks between directives are written through the JSP writer" );
    }

    @Test
    public void testJspWriterDetachedOnceTheFileIsWritten( ) throws Exception
    {
        String strJsp = Files.readString( JSP, StandardCharsets.UTF_8 );
        assertTrue( strJsp.contains( "out = pageContext.pushBody( );" ),
                "once the CSV is written, the JSP writer must be detached from the response (pushBody), else its final flush asks the writer" );
    }
}
