/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import jetbrains.buildServer.web.openapi.SimpleWebExtension;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.WebPlace;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

//public class SettingsExtension extends SimplePageExtension {
public class SettingsExtension extends SimpleWebExtension {
    private final V1ServerListener sl;

    public SettingsExtension(WebControllerManager manager, V1ServerListener sl) {
        super(manager);
        this.sl = sl;
        setPluginName("TeamCityNotificator");
//        setIncludeUrl("viewSettings.jsp");
        setJspPath("viewSettings.jsp");
//        setPlaceId(PlaceId.ADMIN_SERVER_CONFIGURATION);
        setPlace(WebPlace.ADMIN_SERVER_CONFIGURATION);
        setTitle("V1 Title");
        register();
    }

    public void fillModel(final Map model, @NotNull final HttpServletRequest request) {
        super.fillModel(model, request);
        SettingsBean settings = new SettingsBean(sl.getConfig());
        model.put("settingsBean", settings);
    }
}
