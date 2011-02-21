/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.MetaException;
import com.versionone.apiclient.ProxyProvider;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.ProxySettings;
import com.versionone.om.SDKException;
import com.versionone.om.V1Instance;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class V1Config {

    protected String url = "";
    protected String userName;
    protected String password;
    protected Pattern pattern;
    protected String referenceField;
    protected Boolean isFullyQualifiedBuildName;
    protected boolean isProxyUsed;
    protected String proxyUri;
    protected String proxyUser;
    protected String proxyPassword;

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Pattern getPatternObj() {
        return pattern;
    }

    public String getReferenceField() {
        return referenceField;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public void setReferenceField(String referenceField) {
        this.referenceField = referenceField;
    }

    public Boolean isFullyQualifiedBuildName() {
        return isFullyQualifiedBuildName;
    }

    public void setFullyQualifiedBuildName(Boolean fullyQualifiedBuildName) {
        isFullyQualifiedBuildName = fullyQualifiedBuildName;
    }

    public boolean getProxyUsed() {
        return isProxyUsed;
    }

    public void setProxyUsed(Boolean proxyUsed) {
        isProxyUsed = proxyUsed;
    }

    public String getProxyUri() {
        return proxyUri;
    }

    public void setProxyUri(String proxyUri) {
        this.proxyUri = proxyUri;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUsername(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public void setDefaults() {
        url = "http://localhost/VersionOne/";
        userName = "admin";
        password = "admin";
        pattern = Pattern.compile("[A-Z]{1,2}-[0-9]+");
        referenceField = "Number";
        isFullyQualifiedBuildName = true;
        isProxyUsed = false;
        proxyUri = "";
        proxyUser = "";
        proxyPassword = "";
    }

    @Override
    public String toString() {
        return "Config{" +
                "referenceField='" + referenceField + '\'' +
                ", pattern=" + pattern +
                ", password='" + password + '\'' +
                ", userName='" + userName + '\'' +
                ", url='" + url + '\'' +
                ", FullyQualifiedBuildName='" + isFullyQualifiedBuildName + '\'' +
                ", useProxy='" + isProxyUsed + '\'' +
                ", proxyUri='" + proxyUri + '\'' +
                ", proxyUser='" + proxyUser + '\'' +
                ", proxyPassword='" + proxyPassword + '\'' +
                '}';
    }
}
