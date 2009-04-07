/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import com.versionone.integration.teamcity.FileConfig;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.regex.Pattern;


public class FileConfigTester {
    private static final String DIR = ".";

    @After
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void deleteCfgFile() {
        new File(DIR, FileConfig.CONFIG_FILENAME).delete();
    }

    @Test
    public void testFileReadWrite() {
        final String url = "http://v1.com/V1";
        final String userName = "usr";
        final String pass = "psw";
        final Pattern pattern = Pattern.compile("ptr");
        final String ref = "r";
        {
            final FileConfig cfg = new FileConfig(DIR);
            cfg.setUrl(url);
            cfg.setUserName(userName);
            cfg.setPassword(pass);
            cfg.setPattern(pattern);
            cfg.setReferenceField(ref);
            cfg.save();
        }
        {
            final FileConfig cfg = new FileConfig(DIR);
            Assert.assertEquals(url, cfg.getUrl());
            Assert.assertEquals(userName, cfg.getUserName());
            Assert.assertEquals(pass, cfg.getPassword());
            Assert.assertEquals(ref, cfg.getReferenceField());
            Assert.assertEquals(pattern.pattern(), cfg.getPatternObj().pattern());
        }
    }
}
