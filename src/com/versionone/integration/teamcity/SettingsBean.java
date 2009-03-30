/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import jetbrains.buildServer.controllers.RememberState;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.util.StringUtil;

import java.util.regex.Pattern;

public class SettingsBean extends RememberState {

    public String url;
    public String userName;
    private String password;
    private String referenceField;
    private String pattern;

    public SettingsBean(Config cfg) {
        url = cfg.getUrl();
        userName = cfg.getUserName();
        password = cfg.getPassword();
        final Pattern p = cfg.getPattern();
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

    public String getUserName() {
        return userName;
    }

    public String getReferenceField() {
        return referenceField;
    }

    public String getPattern() {
        return pattern;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setReferenceField(String referenceField) {
        this.referenceField = referenceField;
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
