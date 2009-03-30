/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

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

    private Config myV1NotificatorConfig;
    private final V1ServerListener myNotificator;
    private static final String SETTINGS_BEAN_KEY = "settingsBean";
    public static final String EDIT_SETTINGS_URL = "/versionone/Settings.html";
    private static final String VIEW_SETTINGS_URL = "/versionone/viewSettings.html";
    private WebResourcesManager myResManager;

    public V1SettingsController(SBuildServer server, V1ServerListener v1Listener, PagePlaces places,
                                WebControllerManager webControllerManager, WebResourcesManager resourcesManager) {
        super(server, resourcesManager, places, webControllerManager, V1ServerListener.PLUGIN_NAME, EDIT_SETTINGS_URL, SETTINGS_BEAN_KEY);
        this.myNotificator = v1Listener;
        myV1NotificatorConfig = this.myNotificator.getConfig();
        myResManager = resourcesManager;
    }

    protected void saveSettings(SettingsBean bean) {
        copySettings(bean, myV1NotificatorConfig);
        myV1NotificatorConfig.save();
    }

    private void copySettings(SettingsBean bean, Config target) {
        target.setUrl(bean.getUrl());
        target.setUserName(bean.getUserName());
        target.setReferenceField(bean.getReferenceField());
        target.setPattern(Pattern.compile(bean.getPattern()));
    }

    protected ActionErrors validate(SettingsBean bean) {
        ActionErrors errors = new ActionErrors();
        if (StringUtil.isEmptyOrSpaces(bean.getUrl())) {
            errors.addError("emptyUrl", "VersionOne server URL must not be empty");
        } else try {
            new URL(bean.getUrl());
        } catch (MalformedURLException e) {
            errors.addError("invalidUrl", "Invalid server URL format");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getUserName())) {
            errors.addError("emptyUserName", "User name must not be empty");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getPassword())) {
            errors.addError("emptyPassword", "Password must not be empty");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getReferenceField())) {
            errors.addError("emptyReferenceField", "ReferenceField must not be empty");
        }
        if (StringUtil.isEmptyOrSpaces(bean.getPattern())) {
            errors.addError("emptyPattern", "Pattern must not be empty");
        } else try {
            Pattern.compile(bean.getPattern());
        } catch (PatternSyntaxException e) {
            errors.addError("invalidPattern", "Pattern must be valid regular expression");
        }
        return errors;
    }

    protected String testSettings(SettingsBean bean, HttpServletRequest request) {
//TODO        Config config = new Config();
//        copySettings(bean, config);
//        return myNotificator.testNotification(request.getParameter("testAddress"), SessionUser.getUser(request), config);
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