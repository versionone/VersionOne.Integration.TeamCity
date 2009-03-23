/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.SDKException;
import com.versionone.om.V1Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class Settings {

    private final String v1Url;
    private final String v1UserName;
    private final String v1Password;
    private final Pattern pattern;
    private final String referenceField;

    private V1Instance v1Instance;

    /**
     * Creates settings instance.
     *
     * @param v1Url          URL to the VersionOne system. URL can't be null
     * @param v1UserName     user name to the VersionOne.
     * @param v1Password     password to the VersionOne.
     * @param pattern        regular expression for finding story
     * @param referenceField name of field
     */
    public Settings(String v1Url, String v1UserName, String v1Password, Pattern pattern, String referenceField) {
        if (v1Url == null) {
            throw new IllegalArgumentException("The VersionOne URL Parameter cannot be null");
        }
        this.v1Url = v1Url;
        if (isNullOrEmpty(v1UserName)) {
            v1UserName = null;
        }
        this.v1UserName = v1UserName;
        this.v1Password = v1Password;
        this.pattern = pattern;
        this.referenceField = referenceField;
    }

    public Settings() {
        v1Url = "http://jsdksrv01:8080/VersionOne/";
        v1UserName = "admin";
        v1Password = "admin";
        pattern = Pattern.compile("[A-Z]{1,2}-[0-9]+");
        referenceField = "Number";
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
     * getting connection to VersionOne server this method MAY BE called ONLY after {@link #isConnectionValid()}
     *
     * @return connection to VersionOne
     */
    public V1Instance getV1Instance() {
        if (v1Instance == null) {
            throw new RuntimeException("You must call isConnectionValid() before calling getV1Instance()");
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
            System.out.println("Warning, VersionOne connection is invalid: " + e.getMessage());
            v1Instance = null;
            result = false;
        }

        return result;
    }
}
