/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import com.versionone.integration.ciCommon.IConfig;
import com.versionone.integration.teamcity.SettingsBean;
import com.versionone.integration.teamcity.V1ServerListener;
import com.versionone.integration.teamcity.V1SettingsController;
import com.versionone.om.V1Instance;
import jetbrains.buildServer.serverSide.crypt.RSACipher;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class V1SettingsControllerTest {
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

        final V1SettingsController v1Controller = new V1SettingsController(null, v1Listener, null, null, null) {

            @Override
            protected void registerController(WebControllerManager webControllerManager, PagePlaces places) {
                //do nothing
            }
        };

        SettingsBean bean = getBeanWithDefaults();
        Assert.assertFalse(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setUrl("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setUrl("ggg://dfg");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setUserName(null);
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setUserName("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb(null));
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb(""));
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setReferenceField(null);
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setReferenceField("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setReferenceField("GGG");
//        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setPattern(null);
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setPattern("");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());

        bean = getBeanWithDefaults();
        bean.setPattern("\\");
        Assert.assertTrue(v1Controller.validate(bean).hasErrors());
    }

    private static SettingsBean getBeanWithDefaults() {
        return new SettingsBean(new IConfig() {

            @NotNull
            public String getUrl() {
                return "http://localhost/VersionOne/";
            }

            public String getUserName() {
                return "admin";
            }

            public String getPassword() {
                return "admin";
            }

            public Pattern getPatternObj() {
                return Pattern.compile("[A-Z]{1,2}-[0-9]+");
            }

            public String getReferenceField() {
                return "Number";
            }

            public boolean isConnectionValid() {
                return false;
            }

            public V1Instance getV1Instance() {
                return null;
            }
        });
    }
}
