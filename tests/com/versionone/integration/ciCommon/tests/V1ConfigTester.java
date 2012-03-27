/*(c) Copyright 2012, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon.tests;

import com.versionone.integration.ciCommon.V1Config;

import com.versionone.integration.teamcity.FileConfig;
import com.versionone.integration.teamcity.SettingsBean;
import com.versionone.integration.teamcity.V1Connector;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class V1ConfigTester {
    //these need only for integration tests
    public static final String URL = "http://integsrv01/VersionOneSDK/";
    public static final String USER_NAME = "admin";
    public static final String PASSWORD = "admin";

    @Test
    public void testDefaults() {
        final V1Config cfg = new V1Config();
        cfg.setDefaults();

        Assert.assertEquals("http://localhost/VersionOne/", cfg.getUrl());
        Assert.assertEquals("admin", cfg.getUserName());
        Assert.assertEquals("admin", cfg.getPassword());
        Assert.assertEquals("Number", cfg.getReferenceField());
        Assert.assertEquals("[A-Z]{1,2}-[0-9]+", cfg.getPatternObj().pattern());
        Assert.assertFalse(cfg.getProxyUsed());
        Assert.assertEquals("", cfg.getProxyPassword());
        Assert.assertEquals("", cfg.getProxyUri());
        Assert.assertEquals("", cfg.getProxyUser());
    }

    @Test
    @Ignore("Require VersionOne server")
    public void testConnectionValid() {
        final V1Connector connector = new V1Connector();
        FileConfig cfg = new FileConfig(new SettingsBean(new V1Config()));
        connector.setConnectionSettings(cfg);

        Assert.assertFalse(connector.isConnectionValid());
        connector.setConnectionSettings(getValidConfig());
        Assert.assertTrue(connector.isConnectionValid());
    }


    public static FileConfig getValidConfig() {
        final V1Config cfg = new V1Config();
        cfg.setDefaults();
        cfg.setUrl(URL);
        cfg.setUserName(USER_NAME);
        cfg.setPassword(PASSWORD);
        return new FileConfig(new SettingsBean(cfg));
    }
}
