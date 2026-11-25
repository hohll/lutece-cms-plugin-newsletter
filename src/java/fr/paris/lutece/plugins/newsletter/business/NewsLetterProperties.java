/*
 * Copyright (c) 2002-2021, City of Paris
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

import org.apache.commons.lang3.StringUtils;

/**
 * This class represents business NewsLetterProperties Object
 */
public class NewsLetterProperties
{
    /**
     * The editor of the composition of the newsletters: GrapesJS, the visual email builder shipped with the plugin
     */
    public static final String EDITOR_GRAPESJS = "grapesjs";

    /**
     * The editor of the composition of the newsletters: the rich text editor of the back office of the site, with its profile for the newsletter
     * plugin. Any other value but {@link #EDITOR_GRAPESJS} names a rich text editor of the back office chosen for the newsletters.
     */
    public static final String EDITOR_LUTECE = "lutece";

    private boolean _bIsValidationActive;
    private boolean _bIsCaptchaActive;
    private String _strTermOfService;
    private String _strEditor = EDITOR_GRAPESJS;

    /**
     *
     * Set if the user must validate by mail
     * 
     * @param bIsValidationActive
     *            the boolean
     */
    public void setValidationActive( boolean bIsValidationActive )
    {
        _bIsValidationActive = bIsValidationActive;
    }

    /**
     * return if the validation by mail is active
     * 
     * @return _bIsValidationActive the boolean
     */
    public boolean isValidationActive( )
    {
        return _bIsValidationActive;
    }

    /**
     *
     * Set if the user must enter a captcha to subscribe
     * 
     * @param bIsCaptchaActive
     *            the boolean
     */
    public void setCaptchaActive( boolean bIsCaptchaActive )
    {
        _bIsCaptchaActive = bIsCaptchaActive;
    }

    /**
     * return if the user must enter a captcha to subscribe
     * 
     * @return bIsCaptchaActive the boolean
     */
    public boolean isCaptchaActive( )
    {
        return _bIsCaptchaActive;
    }

    /**
     * Returns the tos content of the newsletter
     *
     * @return the tos content of the newsletter
     */
    public String getTOS( )
    {
        return _strTermOfService;
    }

    /**
     * Sets the tos content of the newsletter
     *
     * @param strTermOfService
     *            the tos content of the newsletter
     */
    public void setTOS( String strTermOfService )
    {
        _strTermOfService = strTermOfService;
    }

    /**
     * Returns the editor of the composition of the newsletters
     *
     * @return {@link #EDITOR_GRAPESJS}, {@link #EDITOR_LUTECE} or the name of a rich text editor of the back office
     */
    public String getEditor( )
    {
        return _strEditor;
    }

    /**
     * Sets the editor of the composition of the newsletters. No value selects GrapesJS.
     *
     * @param strEditor
     *            {@link #EDITOR_GRAPESJS}, {@link #EDITOR_LUTECE} or the name of a rich text editor of the back office
     */
    public void setEditor( String strEditor )
    {
        _strEditor = StringUtils.isBlank( strEditor ) ? EDITOR_GRAPESJS : strEditor.trim( );
    }

    /**
     * Tells whether the newsletters are composed with a rich text editor of the back office
     *
     * @return true for a rich text editor of the back office, false for GrapesJS
     */
    public boolean isLuteceEditor( )
    {
        return !EDITOR_GRAPESJS.equals( _strEditor );
    }
}
