/*(c) Copyright 2012, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.teamcity.SettingsBean;
import com.versionone.integration.teamcity.V1Connector;
import com.versionone.integration.teamcity.V1SettingsController;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class V1SettingsControllerTester {
    private Mockery mockery = new Mockery();

    {
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
    }
    private final V1Connector v1ConnectorMock = mockery.mock(V1Connector.class);
    private final ServerPaths serverPathsMock = mockery.mock(ServerPaths.class);

    private V1SettingsController v1Controller;

    @Before
    public void before() {
        mockery.checking(new Expectations() {
            {
                allowing(serverPathsMock).getConfigDir();
                will(returnValue(null));
            }
        });

        v1Controller = new TestV1SettingsController();
    }
    
    @Test
    public void testValidate() {
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
    public void testSettings() {
        mockery.checking(new Expectations() {
            {
                allowing(v1ConnectorMock).isConnectionValid();
                will(returnValue(true));
                allowing(v1ConnectorMock).isReferenceFieldValid();
                will(returnValue(true));
            }
        });

        String result = v1Controller.testSettings(null);
        Assert.assertNull(result);
    }

    @Test
    public void testSettingsIncorrectConnection() {
        mockery.checking(new Expectations() {
            {
                allowing(v1ConnectorMock).isConnectionValid();
                will(returnValue(false));
                allowing(v1ConnectorMock).isReferenceFieldValid();
                will(returnValue(true));
            }
        });

        String result = v1Controller.testSettings(null);
        Assert.assertNotNull(result);
    }

    @Test
    public void testSettingsIncorrectReferenceField() {
        mockery.checking(new Expectations() {
            {
                allowing(v1ConnectorMock).isConnectionValid();
                will(returnValue(true));
                allowing(v1ConnectorMock).isReferenceFieldValid();
                will(returnValue(false));
            }
        });

        String result = v1Controller.testSettings(null);
        Assert.assertNotNull(result);
    }

    @Test
    @Ignore("Integration test. Required VersionOne server.")
    public void testValidateConnection() {
        final V1Config config = new V1Config();
        config.setDefaults();
        SettingsBean bean = new SettingsBean(config);

        bean.setUrl("http://integsrv01/VersionOneSDK/");
        bean.setUserName("badName");
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb("admin"));

        Assert.assertEquals("Connection not valid.", v1Controller.testSettings(bean));

        bean = new SettingsBean(config);
        bean.setUrl("http://integsrv01/VersionOneSDK/");
        bean.setUserName("admin");
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb("admin"));

        Assert.assertNull(v1Controller.testSettings(bean));

        bean = new SettingsBean(config);
        bean.setUrl("http://integsrv01/VersionOneSDK/");
        bean.setUserName("badName");
        bean.setEncryptedPassword(RSACipher.encryptDataForWeb("admin"));

        Assert.assertEquals("Connection not valid.", v1Controller.testSettings(bean));
    }

    private class TestV1SettingsController extends V1SettingsController {
        public TestV1SettingsController() {
            super(v1ConnectorMock, null, null, null, serverPathsMock);
        }

        @Override
        protected void register() {
            //do nothing
        }

        @Override
        protected V1Connector createConnectorToVersionOne(SettingsBean bean) {
            return v1ConnectorMock;
        }

    }
}
