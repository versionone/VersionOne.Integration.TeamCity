/*(c) Copyright 2012, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.versionone.integration.ciCommon.V1Config;
import jetbrains.buildServer.controllers.RememberState;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;

import java.util.regex.Pattern;

public class SettingsBean extends RememberState {
    private String url;
    private String userName;
    private String password;
    private String referenceField;
    private String pattern;
    private Boolean isFullyQualifiedBuildName;
    private Boolean isProxyUsed;
    private String proxyUri;
    private String proxyUsername;
    private String proxyPassword;

    public SettingsBean() {

    }

    public SettingsBean(V1Config cfg) {
        url = cfg.getUrl();
        userName = cfg.getUserName();
        password = cfg.getPassword();
        final Pattern p = cfg.getPatternObj();
        pattern = p == null ? "" : p.pattern();
        referenceField = cfg.getReferenceField();
        isFullyQualifiedBuildName = cfg.isFullyQualifiedBuildName();
        isProxyUsed = cfg.getProxyUsed();
        proxyUri = cfg.getProxyUri();
        proxyUsername = cfg.getProxyUser();
        proxyPassword = cfg.getProxyPassword();
    }

    public String getPassword() {
        return password;
    }

    public String getHexEncodedPublicKey() {
        return RSACipher.getHexEncodedPublicKey();
    }

    public void setEncryptedPassword(String encrypted) {
        password = RSACipher.decryptWebRequestData(encrypted);
    }

    public String getEncryptedPassword() {
        if (!StringUtil.isEmptyOrSpaces(password))
            return RSACipher.encryptDataForWeb(password);
        return "";
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getReferenceField() {
        return referenceField;
    }

    public void setReferenceField(String referenceField) {
        this.referenceField = referenceField;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Boolean getFullyQualifiedBuildName() {
        return isFullyQualifiedBuildName;
    }

    public Boolean getProxyUsed() {
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

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public void setEncryptedProxyPassword(String encrypted) {
        proxyPassword = RSACipher.decryptWebRequestData(encrypted);
    }

    public String getEncryptedProxyPassword() {
        if (!StringUtil.isEmptyOrSpaces(proxyPassword))
            return RSACipher.encryptDataForWeb(proxyPassword);
        return "";
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public void setFullyQualifiedBuildName(Boolean fullyQualifiedBuildName) {
        isFullyQualifiedBuildName = fullyQualifiedBuildName;
    }

    public String getPLUGIN_NAME() {
        return V1ServerListener.PLUGIN_NAME;
    }

    public String getPAGE_URL() {
        return V1SettingsController.PAGE_URL;
    }
}
