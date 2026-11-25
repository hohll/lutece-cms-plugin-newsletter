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
package fr.paris.lutece.plugins.newsletter.service;

import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

import java.util.Locale;

/**
 * Resource Id service for RBAC features to control the actions of the management of the subscribers of all the newsletters. The permissions are
 * granted on all the subscribers (resource id *); the newsletters whose subscribers an administrator sees come from the permission of managing
 * the subscribers of the newsletters and from the workgroups.
 */
public class SubscriberResourceIdService extends ResourceIdService
{
    /**
     * Permission for viewing the subscribers and a subscriber
     */
    public static final String PERMISSION_VIEW = "VIEW";

    /**
     * Permission for adding a subscriber
     */
    public static final String PERMISSION_CREATE = "CREATE";

    /**
     * Permission for suspending and activating subscribers
     */
    public static final String PERMISSION_MODIFY_STATUS = "MODIFY_STATUS";

    /**
     * Permission for validating the pending subscriptions and sending again the confirmation email
     */
    public static final String PERMISSION_VALIDATE = "VALIDATE";

    /**
     * Permission for subscribing a subscriber to newsletters
     */
    public static final String PERMISSION_SUBSCRIBE = "SUBSCRIBE";

    /**
     * Permission for unsubscribing a subscriber from newsletters
     */
    public static final String PERMISSION_UNSUBSCRIBE = "UNSUBSCRIBE";

    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "newsletter.permission.subscriber.resourceType";
    private static final String PROPERTY_LABEL_VIEW = "newsletter.permission.subscriber.label.view";
    private static final String PROPERTY_LABEL_CREATE = "newsletter.permission.subscriber.label.create";
    private static final String PROPERTY_LABEL_MODIFY_STATUS = "newsletter.permission.subscriber.label.modifyStatus";
    private static final String PROPERTY_LABEL_VALIDATE = "newsletter.permission.subscriber.label.validate";
    private static final String PROPERTY_LABEL_SUBSCRIBE = "newsletter.permission.subscriber.label.subscribe";
    private static final String PROPERTY_LABEL_UNSUBSCRIBE = "newsletter.permission.subscriber.label.unsubscribe";

    /**
     * Creates a new instance of SubscriberResourceIdService
     */
    public SubscriberResourceIdService( )
    {
        setPluginName( NewsletterPlugin.PLUGIN_NAME );
    }

    /**
     * Initializes the service
     */
    @Override
    public void register( )
    {
        ResourceType rt = new ResourceType( );
        rt.setResourceIdServiceClass( SubscriberResourceIdService.class.getName( ) );
        rt.setPluginName( NewsletterPlugin.PLUGIN_NAME );
        rt.setResourceTypeKey( Subscriber.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        registerPermission( rt, PERMISSION_VIEW, PROPERTY_LABEL_VIEW );
        registerPermission( rt, PERMISSION_CREATE, PROPERTY_LABEL_CREATE );
        registerPermission( rt, PERMISSION_MODIFY_STATUS, PROPERTY_LABEL_MODIFY_STATUS );
        registerPermission( rt, PERMISSION_VALIDATE, PROPERTY_LABEL_VALIDATE );
        registerPermission( rt, PERMISSION_SUBSCRIBE, PROPERTY_LABEL_SUBSCRIBE );
        registerPermission( rt, PERMISSION_UNSUBSCRIBE, PROPERTY_LABEL_UNSUBSCRIBE );

        ResourceTypeManager.registerResourceType( rt );
    }

    /**
     * Returns no resource: the permissions are granted on all the subscribers
     *
     * @param locale
     *            The current locale
     * @return an empty list
     */
    @Override
    public ReferenceList getResourceIdList( Locale locale )
    {
        return new ReferenceList( );
    }

    /**
     * Returns no title: the permissions are granted on all the subscribers
     *
     * @param strId
     *            The Id of the resource
     * @param locale
     *            The current locale
     * @return null
     */
    @Override
    public String getTitle( String strId, Locale locale )
    {
        return null;
    }

    /**
     * Registers a permission of the resource type
     *
     * @param rt
     *            the resource type
     * @param strPermissionKey
     *            the key of the permission
     * @param strTitleKey
     *            the key of the label of the permission
     */
    private static void registerPermission( ResourceType rt, String strPermissionKey, String strTitleKey )
    {
        Permission permission = new Permission( );
        permission.setPermissionKey( strPermissionKey );
        permission.setPermissionTitleKey( strTitleKey );
        rt.registerPermission( permission );
    }
}
