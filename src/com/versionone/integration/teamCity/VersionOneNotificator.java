/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamCity;

import jetbrains.buildServer.Build;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.notification.Notificator;
import jetbrains.buildServer.notification.NotificatorAdapter;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.serverSide.UserPropertyInfo;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.NotificatorPropertyKey;

import java.util.Set;
import java.util.ArrayList;
import java.util.Date;
import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import com.versionone.om.V1Instance;


public class VersionOneNotificator extends NotificatorAdapter {    
    // plugin UID
    private static final String TYPE = "V1Integration";
    // plugun Name
    private static final String TYPE_NAME = "Version One Integraion";

    //Settings
    private static final String VERSION_ONE_URL = "UrlToVersionOne";
    private static final String VERSION_ONE_LOGIN = "Login";
    private static final String VERSION_ONE_PASSWORD = "Password";
    private static final String VERSION_ONE_REGEXP = "Regexp";
    private static final String VERSION_ONE_REFERENCE_FIELD = "ReferenceField";

    //Settings titles
    private static final String VERSION_ONE_URL_TITLE = "V1 url";
    private static final String VERSION_ONE_LOGIN_TITLE = "V1 login";
    private static final String VERSION_ONE_PASSWORD_TITLE = "V1 password";
    private static final String VERSION_ONE_REGEXP_TITLE = "Regexp for comments";
    private static final String VERSION_ONE_REFERENCE_FIELD_TITLE = "Reference field";

    //Settings keys
    private static final PropertyKey VERSION_ONE_URL_KEY = new NotificatorPropertyKey(TYPE, VERSION_ONE_URL);
    private static final PropertyKey VERSION_ONE_LOGIN_KEY = new NotificatorPropertyKey(TYPE, VERSION_ONE_LOGIN);
    private static final PropertyKey VERSION_ONE_PASSWORD_KEY = new NotificatorPropertyKey(TYPE, VERSION_ONE_PASSWORD);
    private static final PropertyKey VERSION_ONE_REGEXP_KEY = new NotificatorPropertyKey(TYPE, VERSION_ONE_REGEXP);
    private static final PropertyKey VERSION_ONE_REFERENCE_FIELD_KEY = new NotificatorPropertyKey(TYPE, VERSION_ONE_REFERENCE_FIELD);

    private V1Instance v1Instance;
    private Date buildStart;
    private final WebLinks weblinks;

    public VersionOneNotificator(NotificatorRegistry notificatorRegistry, WebLinks weblinks) throws IOException {
        this.weblinks = weblinks;

        if (notificatorRegistry != null) {
            ArrayList<UserPropertyInfo> userProps = new ArrayList<UserPropertyInfo>();
            userProps.add(new UserPropertyInfo(VERSION_ONE_URL, VERSION_ONE_URL_TITLE));
            userProps.add(new UserPropertyInfo(VERSION_ONE_LOGIN, VERSION_ONE_LOGIN_TITLE));
            userProps.add(new UserPropertyInfo(VERSION_ONE_PASSWORD, VERSION_ONE_PASSWORD_TITLE));
            userProps.add(new UserPropertyInfo(VERSION_ONE_REGEXP, VERSION_ONE_REGEXP_TITLE));
            userProps.add(new UserPropertyInfo(VERSION_ONE_REFERENCE_FIELD, VERSION_ONE_REFERENCE_FIELD_TITLE));

            notificatorRegistry.register(this, userProps);
        }
    }


    public void notifyBuildStarted(SRunningBuild build, Set<SUser> users) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void notifyBuildSuccessful(SRunningBuild build, Set<SUser> users) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void notifyBuildFailed(SRunningBuild build, Set<SUser> users) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void notifyLabelingFailed(Build build, VcsRoot root, Throwable exception, Set<SUser> users) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
//
//    public void notifyBuildFailing(SRunningBuild build, Set<SUser> users) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void notifyBuildProbablyHanging(SRunningBuild build, Set<SUser> users) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void notifyResponsibleChanged(SBuildType buildType, Set<SUser> users) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }

    @NotNull
    public String getNotificatorType() {
        return TYPE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    public String getDisplayName() {
        return TYPE_NAME;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
