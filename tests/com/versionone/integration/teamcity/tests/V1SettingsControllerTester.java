/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.teamcity.SettingsBean;
import com.versionone.integration.teamcity.V1ServerListener;
import com.versionone.integration.teamcity.V1SettingsController;
import com.versionone.integration.teamcity.FileConfig;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.openapi.WebResourcesManager;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PagePlace;
import jetbrains.buildServer.web.openapi.SimplePageExtension;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

public class V1SettingsControllerTester {
    private Mockery mockery = new Mockery();

    {
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
    }

    @Test
    public void testValidate() {
        final V1ServerListener v1Listener = mockery.mock(V1ServerListener.class);
        mockery.checking(new Expectations() {
            {
                allowing(v1Listener).getConfig();
                will(returnValue(null));
            }
        });
        final WebResourcesManager resourcesManager = mockery.mock(WebResourcesManager.class);
        mockery.checking(new Expectations() {
            {

                allowing(resourcesManager).resourcePath("TeamCityNotificator", "editSettings.jsp");
                will(returnValue("fullpath"));
            }
        });

        final PagePlace pagePlace = mockery.mock(PagePlace.class);
        mockery.checking(new Expectations() {
            {

                allowing(pagePlace).addExtension(with(any(SimplePageExtension.class)), with(any(Integer.class)));
                //will(returnValue(pagePlace));
            }
        });

        final PagePlaces places = mockery.mock(PagePlaces.class);
        mockery.checking(new Expectations() {
            {

                allowing(places).getPlaceById(PlaceId.ADMIN_SERVER_CONFIGURATION_TAB);
                will(returnValue(pagePlace));
            }
        });

        final V1SettingsController v1Controller = new V1SettingsController(null, v1Listener, places, null, resourcesManager) {

            @Override
            protected void registerController(WebControllerManager webControllerManager, PagePlaces places, SimpleCustomTab tab) {
                //do nothing
            }
        };

        final V1Config config = new V1Config();
        config.setDefaults();
        SettingsBean bean = new SettingsBean(config);
        Assert.assertFalse(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setUrl("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setUrl("ggg://dfg");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setUserName(null);
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setUserName("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb(null));
        Assert.assertFalse(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb(""));
        Assert.assertFalse(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb(""));
        bean.setUserName("");
        Assert.assertFalse(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setReferenceField(null);
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setReferenceField("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setPattern(null);
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setPattern("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setPattern("\\");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setPattern("");
        bean.setReferenceField("");
        Assert.assertFalse(v1Controller.validate(bean).hasErrors());
    }

    @Test
    public void testPasswordEncription() {
        final V1Config config = new V1Config();
        config.setDefaults();
        SettingsBean bean = new SettingsBean(config);

        Assert.assertFalse(bean.getEncryptedPassword().contains(bean.getPassword()));
    }
}
