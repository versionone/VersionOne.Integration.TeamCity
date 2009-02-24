/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import org.junit.Test;
import org.junit.Assert;
import com.versionone.integration.teamcity.Settings;
import jetbrains.buildServer.web.openapi.PluginException;


public class SettingsTester {

    @Test
    public void testGetV1Instance() {
        final Settings settings = new Settings("http://localhost", "1", "2", null, "field");

        try {
            settings.getV1Instance();
            Assert.fail("This test has to call exception");
        } catch (RuntimeException e) {
            
        }
    }

    @Test
    public void testIsConnectionValidIncorrectUrl() {
        final Settings settings = new Settings("http://incorrecturl", "1", "2", null, "field");

        Assert.assertFalse(settings.isConnectionValid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsConnectionUrlIsNull() {
        final Settings settings = new Settings(null, "1", "2", null, "field");
    }

    public void testIsConnectionUrlIsEmpty() {
        final Settings settings = new Settings("", "1", "2", null, "field");
        Assert.assertFalse(settings.isConnectionValid());
    }

    @Test
    public void testLoginIsEmpty() {
        final Settings settings = new Settings("http://url", "", "2", null, "field");

        Assert.assertNull(settings.getV1UserName());
    }

}
