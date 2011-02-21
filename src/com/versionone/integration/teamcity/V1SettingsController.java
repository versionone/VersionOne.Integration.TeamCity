/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.sun.jndi.toolkit.url.Uri;
import com.versionone.integration.ciCommon.V1Config;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.admin.NotificatorSettingsController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.WebResourcesManager;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class V1SettingsController extends NotificatorSettingsController<SettingsBean> {

    public static final String EDIT_SETTINGS_URL = "/versionone/Settings.html";
    public static final String VIEW_SETTINGS_URL = "/versionone/viewSettings.html";
    private static final String SETTINGS_BEAN_KEY = "settingsBean";

    private WebResourcesManager myResManager;
    private FileConfig myV1NotificatorConfig;
    private V1Connector connector;

    public V1SettingsController(SBuildServer server, V1Connector connector, PagePlaces places,
                                WebControllerManager webControllerManager, WebResourcesManager resourcesManager) {
        super(server, resourcesManager, places, webControllerManager,
                V1ServerListener.PLUGIN_NAME, EDIT_SETTINGS_URL, SETTINGS_BEAN_KEY);
        myV1NotificatorConfig = connector.getConfig();
        myResManager = resourcesManager;
        this.connector = connector;
    }

    protected void saveSettings(SettingsBean bean) {
        copySettings(bean, myV1NotificatorConfig);
        myV1NotificatorConfig.save();
        connector.disconnect();
    }

    private static void copySettings(SettingsBean bean, V1Config target) {
        target.setUrl(bean.getUrl());
        target.setUserName(bean.getUserName());
        target.setPassword(bean.getPassword());
        target.setReferenceField(bean.getReferenceField());
        target.setPattern(Pattern.compile(bean.getPattern()));
        try {
            target.setFullyQualifiedBuildName(Boolean.parseBoolean(bean.getFullyQualifiedBuildName().toString()));
        } catch (Exception ex) {
            target.setFullyQualifiedBuildName(false);
        }
        try {
            target.setProxyUsed(Boolean.parseBoolean(bean.getProxyUsed().toString()));
        } catch (Exception ex) {
            target.setProxyUsed(false);
        }
        target.setProxyUri(bean.getProxyUri());
        target.setProxyUsername(bean.getProxyUsername());
        target.setProxyPassword(bean.getProxyPassword());
    }

    public ActionErrors validate(SettingsBean bean) {
        ActionErrors errors = new ActionErrors();
        if (StringUtil.isEmptyOrSpaces(bean.getUrl())) {
            errors.addError("emptyUrl", "VersionOne Server URL is required.");
        } else try {
            new URL(bean.getUrl());
        } catch (MalformedURLException e) {
            errors.addError("invalidUrl", "Invalid server URL format.");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getUserName())) {
            errors.addError("emptyUserName", "User name is required.");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getPassword())) {
            errors.addError("emptyPassword", "Password is required.");
        }        
        if (StringUtil.isEmptyOrSpaces(bean.getReferenceField())) {
            errors.addError("emptyReferenceField", "Reference Field is required.");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getPattern())) {
        	errors.addError("emptyPattern", "Pattern Field is required.");
        } else {
            try {
                Pattern.compile(bean.getPattern());
            } catch (PatternSyntaxException e) {
                errors.addError("invalidPattern", "Pattern must be valid regular expression");
            }
        }
        if (bean.getProxyUsed() && StringUtil.isEmptyOrSpaces(bean.getProxyUri())) {
            errors.addError("onEmptyProxyUriError", "Proxy URI is required.");
        } else if (bean.getProxyUsed()) {
            try {
                new Uri(bean.getProxyUri());
            } catch (MalformedURLException e) {
                errors.addError("onInvalidProxyUriError", "Invalid proxy URI format.");
            }
        }
        return errors;
    }

    public String testSettings(SettingsBean bean, HttpServletRequest request) {
        final FileConfig testConfig = new FileConfig(bean);
        V1Connector testConnector = new V1Connector();
        testConnector.setConnectionSettings(testConfig);
        if (!testConnector.isConnectionValid()) {
            return "Connection not valid.";
        }
        if (!testConnector.isReferenceFieldValid()) {
            return "Connection is valid.\nReference field NOT valid.";
        }
        return null;
    }

    protected SettingsBean createSettingsBean(HttpServletRequest request) {
        return new SettingsBean(myV1NotificatorConfig);
    }

    protected void registerController(WebControllerManager webControllerManager, PagePlaces places) {
        webControllerManager.registerController(EDIT_SETTINGS_URL, this);
        webControllerManager.registerController(VIEW_SETTINGS_URL,
                new BaseController(myServer) {
                    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response)
                            throws Exception {
                        ModelAndView modelAndView = new ModelAndView(myResManager.resourcePath(
                                V1ServerListener.PLUGIN_NAME, "viewSettings.jsp"));
                        modelAndView.getModel().put(SETTINGS_BEAN_KEY, createSettingsBean(request));
                        return modelAndView;
                    }
                });
        new SimplePageExtension(places, PlaceId.ADMIN_SERVER_CONFIGURATION, V1ServerListener.PLUGIN_NAME, VIEW_SETTINGS_URL).register();
    }

}