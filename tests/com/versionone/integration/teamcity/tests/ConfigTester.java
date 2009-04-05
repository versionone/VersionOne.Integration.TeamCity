/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import com.versionone.integration.teamcity.Config;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.regex.Pattern;


public class ConfigTester {
    private static final String DIR = ".";

    @After
    public void deleteCfgFile() {
        new File(DIR, Config.CONFIG_FILENAME).delete();
    }

    @Test
    public void testDefaults() {
        final Config cfg = new Config(DIR);

        Assert.assertEquals("http://localhost/VersionOne/", cfg.getUrl());
        Assert.assertEquals("admin", cfg.getUserName());
        Assert.assertEquals("admin", cfg.getPassword());
        Assert.assertEquals("Number", cfg.getReferenceField());
        Assert.assertEquals("[A-Z]{1,2}-[0-9]+", cfg.getPatternObj().pattern());
    }

    @Test
    public void testConnectionValid() {
        final Config cfg = new Config(DIR);
        cfg.setUrl("http://v1.co/V1");
        Assert.assertFalse(cfg.isConnectionValid());
    }

    @Test
    public void testFileReadWrite() {
        final String url = "http://v1.com/V1";
        final String userName = "usr";
        final String pass = "psw";
        final Pattern pattern = Pattern.compile("ptr");
        final String ref = "r";

        final Config cfg = new Config(DIR);
        cfg.setUrl(url);
        cfg.setUserName(userName);
        cfg.setPassword(pass);
        cfg.setPattern(pattern);
        cfg.setReferenceField(ref);
        cfg.save();

        final Config cfg2 = new Config(DIR);
        Assert.assertEquals(url, cfg2.getUrl());
        Assert.assertEquals(userName, cfg2.getUserName());
        Assert.assertEquals(pass, cfg2.getPassword());
        Assert.assertEquals(ref, cfg2.getReferenceField());
        Assert.assertEquals(pattern.pattern(), cfg2.getPatternObj().pattern());
    }
}
