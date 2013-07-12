/*(c) Copyright 2012, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.versionone.integration.ciCommon.V1Config;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * This class adds ability to read and write configuration fields to Java property file and abbility to watch file
 * changes and reload itself if any.
 */
public class FileConfig extends V1Config implements ChangeListener {

    private static final Logger LOG = Logger.getInstance(FileConfig.class.getName());

    /**
     * Name of configuration file.
     */
    public static final String CONFIG_FILENAME = "versionone-config.properties";
    /**
     * Interval in seconds configuration file is monitored in.
     */
    public static final int FILE_MONITOR_INTERVAL = 10;
    private static final String URL = "url";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "password";
    private static final String PATTERN = "pattern";
    private static final String REFERENCE_FIELD = "referenceField";
    private static final String FULLY_QUALIFIED_BUILD_NAME = "fullyQualifiedBuildName";
    private static final String USE_PROXY = "useProxy";
    private static final String PROXY_URI = "proxyUri";
    private static final String PROXY_USER_NAME = "proxyUsername";
    private static final String PROXY_PASSWORD = "proxyPassword";


    private File myConfigFile;
    private FileWatcher myChangeObserver;

    /**
     * Creates object and initialize it by values from file in specified directory. If file not found default values are
     * loaded, file created and stored to specified directory.
     *
     * @param configDir directory where to find/store config file.
     */
    public FileConfig(String configDir) {
        myConfigFile = new File(configDir, CONFIG_FILENAME);
        myChangeObserver = new FileWatcher(myConfigFile);
        myChangeObserver.setSleepingPeriod(FILE_MONITOR_INTERVAL * 1000L);
        myChangeObserver.registerListener(this);
        myChangeObserver.start();
        if (!myConfigFile.exists()) {
            setDefaults();
            save();
            LOG.warn("Default VersionOne config file created.");
        } else {
            load();
        }
        LOG.info("VersionOne configuraiton file " + myConfigFile.getAbsolutePath() +
                " will be monitored with interval " + FILE_MONITOR_INTERVAL + " seconds.");
    }

    /**
     * Creates object and initialize it by values from specified bean object.
     *
     * @param bean object to get init values from.
     */
    public FileConfig(SettingsBean bean) {
        url = bean.getUrl();
        userName = bean.getUserName();
        password = bean.getPassword();
        pattern = Pattern.compile(bean.getPattern());
        referenceField = bean.getReferenceField();
        isFullyQualifiedBuildName = bean.getFullyQualifiedBuildName();
        isProxyUsed = bean.getProxyUsed();
        proxyUri = bean.getProxyUri();
        proxyUser = bean.getProxyUsername();
        proxyPassword = bean.getProxyPassword();
    }

    /**
     * Loads configuration from file.
     */
    private synchronized void load() {
        FileInputStream stream = null;
        try {
            LOG.info("Loading VersionOne configuration file: " + myConfigFile.getAbsolutePath());
            final Properties prop = new Properties();
            stream = new FileInputStream(myConfigFile);
            prop.load(stream);
            url = prop.getProperty(URL);
            userName = prop.getProperty(USER_NAME);
            final String pass = prop.getProperty(PASSWORD);
            password = StringUtil.isEmptyOrSpaces(pass) ? null : EncryptUtil.unscramble(pass);
            pattern = Pattern.compile(prop.getProperty(PATTERN));
            referenceField = prop.getProperty(REFERENCE_FIELD);
            isFullyQualifiedBuildName = Boolean.parseBoolean(prop.getProperty(FULLY_QUALIFIED_BUILD_NAME));
            isProxyUsed = Boolean.parseBoolean(prop.getProperty(USE_PROXY));
            proxyUri = prop.getProperty(PROXY_URI);
            proxyUser = prop.getProperty(PROXY_USER_NAME);
            final String proxyPass = prop.getProperty(PROXY_PASSWORD);
            proxyPassword = StringUtil.isEmptyOrSpaces(proxyPass) ? null : EncryptUtil.unscramble(proxyPass);
            LOG.info("\t...loading completed seccessfuly.");
        } catch (Exception e) {
            throw new RuntimeException("Cannot load VersionOne config file: " + myConfigFile, e);
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (IOException e) {
                    //do nothing
                }
        }
    }

    /**
     * Saves configuration to file.
     */
    public synchronized void save() {
        LOG.info("Saving configuration file: " + myConfigFile.getAbsolutePath());
        myChangeObserver.runActionWithDisabledObserver(new Runnable() {

            public void run() {
                final Properties p = new Properties();
                p.setProperty(URL, url);
                p.setProperty(USER_NAME, userName);
                if (!StringUtil.isEmptyOrSpaces(password)) {
                    p.setProperty(PASSWORD, EncryptUtil.scramble(password));
                }
                p.setProperty(PATTERN, pattern.pattern());
                p.setProperty(REFERENCE_FIELD, referenceField);
                p.setProperty(FULLY_QUALIFIED_BUILD_NAME, isFullyQualifiedBuildName.toString());
                p.setProperty(USE_PROXY, Boolean.toString(isProxyUsed));
                p.setProperty(PROXY_URI, proxyUri);
                p.setProperty(PROXY_USER_NAME, proxyUser);
                if (!StringUtil.isEmptyOrSpaces(proxyPassword)) {
                    p.setProperty(PROXY_PASSWORD, EncryptUtil.scramble(proxyPassword));
                }

                FileOutputStream stream = null;
                try {
                    stream = new FileOutputStream(myConfigFile);
                    p.store(stream, null);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot save configuration file: " + myConfigFile, e);
                } finally {
                    if (stream != null)
                        try {
                            stream.close();
                        } catch (IOException e) {
                            //do nothing
                        }
                }
            }
        });
    }

    public void changeOccured(String requestor) {
        load();
    }
}
