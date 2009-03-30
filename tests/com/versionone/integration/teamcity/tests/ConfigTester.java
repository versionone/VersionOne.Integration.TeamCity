/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import com.versionone.integration.common.IConfig;
import com.versionone.integration.teamcity.Config;

import org.junit.Assert;
import org.junit.Test;


public class ConfigTester {

    @Test
    public void testGetV1Instance() {
        final Config settings = new Config("http://localhost", "1", "2", null, "field");

        try {
            settings.getV1Instance();
            Assert.fail("This test has to call exception");
        } catch (RuntimeException e) {
            
        }
    }

    @Test
    public void testIsConnectionValidIncorrectUrl() {
        final Config settings = new Config("http://incorrecturl", "1", "2", null, "field");

        Assert.assertFalse(settings.isConnectionValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsConnectionUrlIsNull() {
        final IConfig settings = new Config(null, "1", "2", null, "field");
    }

    public void testIsConnectionUrlIsEmpty() {
        final Config settings = new Config("", "1", "2", null, "field");
        Assert.assertFalse(settings.isConnectionValid());
    }

    @Test
    public void testLoginIsEmpty() {
        final IConfig settings = new Config("http://url", "", "2", null, "field");

        Assert.assertNull(settings.getUserName());
    }

}
