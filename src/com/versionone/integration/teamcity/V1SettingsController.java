/*(c) Copyright 2012, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.sun.jndi.toolkit.url.Uri;
import com.versionone.integration.ciCommon.V1Config;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.controllers.FormUtil;
import jetbrains.buildServer.controllers.PublicKeyUtil;
import jetbrains.buildServer.controllers.RememberState;
import jetbrains.buildServer.controllers.XmlResponseUtil;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.CustomTab;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import jetbrains.buildServer.controllers.admin.NotificatorSettingsController;

public class V1SettingsController extends BaseFormXmlController implements CustomTab {

    public static final String PAGE_URL = "/plugins/TeamCityNotificator/editSettings.html";
    private static final String SETTINGS_BEAN_KEY = "settingsBean";
    private static final String FILE_NAME = "editSettings.jsp";

    private PluginDescriptor descriptor;
    private FileConfig myV1NotificatorConfig;
    private V1Connector connector;
    private WebControllerManager webControllerManager;
    protected final PagePlaces myPagePlaces;
    private PlaceId myPlaceId;

    public V1SettingsController(V1Connector connector, PagePlaces places, WebControllerManager webControllerManager,
                                PluginDescriptor descriptor, ServerPaths serverPaths) {
        //super(server);

        myV1NotificatorConfig = new FileConfig(serverPaths.getConfigDir());
        this.descriptor = descriptor;
        this.connector = connector;
        this.webControllerManager = webControllerManager;
        this.myPagePlaces = places;
        this.myPlaceId = PlaceId.ADMIN_SERVER_CONFIGURATION_TAB;

        register();
    }

    protected void register() {
        myPagePlaces.getPlaceById(myPlaceId).addExtension(this);
        webControllerManager.registerController(PAGE_URL, this);
    }

    protected ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
        RememberState bean = createSettingsBean();
        ModelAndView view = new ModelAndView(descriptor.getPluginResourcesPath() + FILE_NAME);
        view.getModel().put(SETTINGS_BEAN_KEY, bean);
        return view;
    }

    private SettingsBean createSettingsBean() {
        return new SettingsBean(myV1NotificatorConfig);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response, Element xmlResponse) {
        if (PublicKeyUtil.isPublicKeyExpired(request)) {
            PublicKeyUtil.writePublicKeyExpiredError(xmlResponse);
            return;
        }
        SettingsBean bean = getSettingsBean(request);
        FormUtil.bindFromRequest(request, bean);
        if (isStoreInSessionRequest(request)) {
            XmlResponseUtil.writeFormModifiedIfNeeded(xmlResponse, bean);
            return;
        }
        ActionErrors errors = validate(bean);
        if (!errors.hasNoErrors()) {
            writeErrors(xmlResponse, errors);
            return;
        }

        String testConnectionResult = testSettings(bean);
        if (isTestConnectionRequest(request)) {
            XmlResponseUtil.writeTestResult(xmlResponse, testConnectionResult);
        } else {
            if (testConnectionResult == null) {
                saveSettings(bean);
                FormUtil.removeFromSession(request.getSession(), bean.getClass());
                writeRedirect(xmlResponse, (request.getContextPath() + "admin.html?item=" + getTabId()));
            } else {
                errors.addError("invalidConnection", testConnectionResult);
                writeErrors(xmlResponse, errors);
            }
        }
    }

    protected final boolean isStoreInSessionRequest(HttpServletRequest request) {
        return "storeInSession".equals(request.getParameter("submitSettings"));
    }

    protected final boolean isTestConnectionRequest(HttpServletRequest request) {
        return "testConnection".equals(request.getParameter("submitSettings"));
    }

    protected SettingsBean getSettingsBean(HttpServletRequest request) {
        final SettingsBean bean = createSettingsBean();
        return FormUtil.getOrCreateForm(request, (Class<SettingsBean>) bean.getClass(),
                new FormUtil.FormCreator<SettingsBean>() {
                    public SettingsBean createForm(HttpServletRequest request) {
                        return bean;
                    }
                });
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
        target.setFullyQualifiedBuildName(getBooleanByString(bean.getFullyQualifiedBuildName().toString()));
        target.setProxyUsed(getBooleanByString(bean.getProxyUsed().toString()));
        target.setProxyUri(bean.getProxyUri());
        target.setProxyUsername(bean.getProxyUsername());
        target.setProxyPassword(bean.getProxyPassword());
    }

    private static Boolean getBooleanByString(String value) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception ex) {
            return false;
        }
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

    public String testSettings(SettingsBean bean) {
        V1Connector testConnector = createConnectorToVersionOne(bean);
        if (!testConnector.isConnectionValid()) {
            return "Connection not valid.";
        }
        if (!testConnector.isReferenceFieldValid()) {
            return "Connection is valid.\nReference field NOT valid.";
        }
        return null;
    }

    private V1Connector createConnectorToVersionOne(SettingsBean bean) {
        final FileConfig testConfig = new FileConfig(bean);
        V1Connector testConnector = new V1Connector();
        testConnector.setConnectionSettings(testConfig);

        return testConnector;
    }

    @NotNull
    public String getTabId() {
        return "VersionOneNotifier";
    }

    @NotNull
    public String getTabTitle() {
        return "VersionOne Notifier";
    }

    @NotNull
    public String getIncludeUrl() {
        return PAGE_URL;
    }

    @NotNull
    public String getPluginName() {
        return "TeamCityNotificator";
    }

    @NotNull
    public List<String> getCssPaths() {
        return new ArrayList<String>();
    }

    @NotNull
    public List<String> getJsPaths() {
        return new ArrayList<String>();
    }

    public boolean isAvailable(@NotNull final HttpServletRequest request) {
        return true;
    }

    public boolean isVisible() {
        return true;
    }

    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    }
}