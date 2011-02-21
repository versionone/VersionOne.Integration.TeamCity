/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.teamcity.SettingsBean;
import com.versionone.integration.teamcity.V1Connector;
import com.versionone.integration.teamcity.V1ServerListener;
import com.versionone.integration.teamcity.V1SettingsController;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.WebControllerManager;

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
        final V1Connector v1Connector = mockery.mock(V1Connector.class);
        mockery.checking(new Expectations() {
            {
                allowing(v1Connector).getConfig();
                will(returnValue(null));
            }
        });

        final V1SettingsController v1Controller = new V1SettingsController(null, v1Connector, null, null, null) {

            @Override
            protected void registerController(WebControllerManager webControllerManager, PagePlaces places) {
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
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb(""));
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = new SettingsBean(config);
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb(""));
        bean.setUserName("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

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
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());
    }

    @Test
    public void testPasswordEncription() {
        final V1Config config = new V1Config();
        config.setDefaults();
        SettingsBean bean = new SettingsBean(config);

        Assert.assertFalse(bean.getEncryptedPassword().contains(bean.getPassword()));
    }

    @Test
    public void testValidateConnection() {
        final V1Connector v1Connector = mockery.mock(V1Connector.class);
        mockery.checking(new Expectations() {
            {
                allowing(v1Connector).getConfig();
                will(returnValue(null));
            }
        });

        final V1SettingsController v1Controller = new V1SettingsController(null, v1Connector, null, null, null) {

            @Override
            protected void registerController(WebControllerManager webControllerManager, PagePlaces places) {
                //do nothing
            }
        };

        final V1Config config = new V1Config();
        config.setDefaults();
        SettingsBean bean = new SettingsBean(config);

        bean.setUrl("http://eval.versionone.net/ExigenTest/");
        bean.setUserName("badName");
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb("admin"));

        Assert.assertEquals("Connection not valid.", v1Controller.testSettings(bean, null));

        bean = new SettingsBean(config);
        bean.setUrl("http://eval.versionone.net/ExigenTest/");
        bean.setUserName("admin");
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb("admin"));

        Assert.assertNull(v1Controller.testSettings(bean, null));

        bean = new SettingsBean(config);
        bean.setUrl("http://eval.versionone.net/ExigenTest/");
        bean.setUserName("badName");
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb("admin"));

        Assert.assertEquals("Connection not valid.", v1Controller.testSettings(bean, null));
    }
}
