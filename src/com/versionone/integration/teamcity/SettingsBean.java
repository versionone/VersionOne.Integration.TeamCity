/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
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

    public SettingsBean(V1Config cfg) {
        url = cfg.getUrl();
        userName = cfg.getUserName();
        password = cfg.getPassword();
        final Pattern p = cfg.getPatternObj();
        pattern = p == null ? "" : p.pattern();
        referenceField = cfg.getReferenceField();
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

    public String getPLUGIN_NAME() {
        return V1ServerListener.PLUGIN_NAME;
    }

    public String getEDIT_URL() {
        return V1SettingsController.EDIT_SETTINGS_URL;
    }
}
