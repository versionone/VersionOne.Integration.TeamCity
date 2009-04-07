/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon.tests;

import com.versionone.integration.ciCommon.V1Config;

import org.junit.Assert;
import org.junit.Test;

public class V1ConfigTester {

    //these need only for integration tests
    public static final String URL = "http://localhost/V1JavaSDKTests/";
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
    }

    @Test
    public void testConnectionValid() {
        V1Config cfg = new V1Config();
        Assert.assertFalse(cfg.isConnectionValid());
        cfg = getValidConfig();
        Assert.assertTrue(cfg.isConnectionValid());
    }


    public static V1Config getValidConfig() {
        final V1Config cfg = new V1Config();
        cfg.setDefaults();
        cfg.setUrl(URL);
        cfg.setUserName(USER_NAME);
        cfg.setPassword(PASSWORD);
        return cfg;
    }
}
