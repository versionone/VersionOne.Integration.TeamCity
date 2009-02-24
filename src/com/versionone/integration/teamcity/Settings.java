/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.SDKException;
import com.versionone.om.V1Instance;
import jetbrains.buildServer.notification.Notificator;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.serverSide.UserPropertyInfo;
import jetbrains.buildServer.users.NotificatorPropertyKey;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.regex.Pattern;


public class Settings {

    //Settings
    static final String VERSION_ONE_URL = "UrlToVersionOne";
    static final String VERSION_ONE_LOGIN = "Login";
    static final String VERSION_ONE_PASSWORD = "Password";
    static final String VERSION_ONE_REGEXP = "Regexp";
    static final String VERSION_ONE_REFERENCE_FIELD = "ReferenceField";
    //Settings titles
    static final String VERSION_ONE_URL_TITLE = "V1 url";
    static final String VERSION_ONE_LOGIN_TITLE = "V1 login";
    static final String VERSION_ONE_PASSWORD_TITLE = "V1 password";
    static final String VERSION_ONE_REGEXP_TITLE = "Regexp for comments";
    static final String VERSION_ONE_REFERENCE_FIELD_TITLE = "Reference field";
    //Settings keys
    static final PropertyKey VERSION_ONE_URL_KEY = new NotificatorPropertyKey(VersionOneNotificator.TYPE, VERSION_ONE_URL);
    static final PropertyKey VERSION_ONE_LOGIN_KEY = new NotificatorPropertyKey(VersionOneNotificator.TYPE, VERSION_ONE_LOGIN);
    static final PropertyKey VERSION_ONE_PASSWORD_KEY = new NotificatorPropertyKey(VersionOneNotificator.TYPE, VERSION_ONE_PASSWORD);
    static final PropertyKey VERSION_ONE_REGEXP_KEY = new NotificatorPropertyKey(VersionOneNotificator.TYPE, VERSION_ONE_REGEXP);
    static final PropertyKey VERSION_ONE_REFERENCE_FIELD_KEY = new NotificatorPropertyKey(VersionOneNotificator.TYPE, VERSION_ONE_REFERENCE_FIELD);

    private final String v1Url;
    private final String v1UserName;
    private final String v1Password;
    private final Pattern pattern;
    private final String referenceField;

    private V1Instance v1Instance;

    public Settings(SUser user) {
        this(
                user.getPropertyValue(VERSION_ONE_URL_KEY),
                user.getPropertyValue(VERSION_ONE_LOGIN_KEY),
                user.getPropertyValue(VERSION_ONE_PASSWORD_KEY),
                Pattern.compile(user.getPropertyValue(VERSION_ONE_REGEXP_KEY)),
                user.getPropertyValue(VERSION_ONE_REFERENCE_FIELD_KEY)
        );
    }

    public Settings(@NotNull String v1Url, String v1UserName, String v1Password, Pattern pattern, String referenceField) {
        this.v1Url = v1Url;
        if (isNullOrEmpty(v1UserName)) {
            v1UserName = null;
        }
        this.v1UserName = v1UserName;
        this.v1Password = v1Password;
        this.pattern = pattern;
        this.referenceField = referenceField;
    }

    @NotNull
    public String getV1Url() {
        return v1Url;
    }

    @Nullable
    public String getV1UserName() {
        return this.v1UserName;
    }

    public String getV1Password() {
        return v1Password;
    }

    @Nullable
    public Pattern getPattern() {
        return pattern;
    }

    @Nullable
    public String getReferenceField() {
        return referenceField;
    }

    private static boolean isNullOrEmpty(String string) {
        return (string == null) || (string.trim().length() == 0);
    }

    private V1Instance connect() throws AuthenticationException, ApplicationUnavailableException {
        if (v1Instance == null) {
            if (getV1UserName() == null) {
                v1Instance = new V1Instance(getV1Url());
            } else {
                v1Instance = new V1Instance(getV1Url(), getV1UserName(), getV1Password());
            }
            v1Instance.validate();
        }
        return v1Instance;
    }

    /**
     * getting connection to VersionOne server
     * this method MAY BE called ONLY after {@link #isConnectionValid()}
     *
     * @return connection to VersionOne
     */
    public V1Instance getV1Instance() {
        if (v1Instance == null) {
            throw new RuntimeException("The getV1Instance() method MAY BE called ONLY after isConnectionValid()");
        }

        return v1Instance;
    }

    /**
     * Validate connection to the VersionOne server
     *
     * @return true if all settings is correct and connection to V1 is valid, false - otherwise
     */
    public boolean isConnectionValid() {
        Boolean result = true;

        try {
            connect();
        } catch (SDKException e) {
            System.out.println("Warning, Connection to V1 not valid: " + e.getMessage());
            v1Instance = null;
            result = false;
        }

        return result;
    }

    /**
     * Registers plugin into TeamCity system.
     *
     * @param notificator               Notififator instance which need to register in the TeamCity system.
     * @param notificatorRegistry       system for registration or null; if nill - do nothing.
     */
    static void registerSettings(@NotNull Notificator notificator, @Nullable NotificatorRegistry notificatorRegistry) {
        if (notificatorRegistry != null) {
            ArrayList<UserPropertyInfo> userProps = new ArrayList<UserPropertyInfo>(5);
            userProps.add(new UserPropertyInfo(VERSION_ONE_URL, VERSION_ONE_URL_TITLE));
            userProps.add(new UserPropertyInfo(VERSION_ONE_LOGIN, VERSION_ONE_LOGIN_TITLE));
            userProps.add(new UserPropertyInfo(VERSION_ONE_PASSWORD, VERSION_ONE_PASSWORD_TITLE));
            userProps.add(new UserPropertyInfo(VERSION_ONE_REGEXP, VERSION_ONE_REGEXP_TITLE));
            userProps.add(new UserPropertyInfo(VERSION_ONE_REFERENCE_FIELD, VERSION_ONE_REFERENCE_FIELD_TITLE));

            notificatorRegistry.register(notificator, userProps);
        }
    }
}
