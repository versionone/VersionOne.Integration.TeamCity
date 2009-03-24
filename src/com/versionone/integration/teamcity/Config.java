/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.SDKException;
import com.versionone.om.V1Instance;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

public class Config implements ChangeListener {

    private static final Logger LOG = Logger.getInstance(Config.class.getName());

    static final String CONFIG_FILENAME = "v1-config.properties";

    private String url;
    private String userName;
    private String password;
    private Pattern pattern;
    private String referenceField;

    private V1Instance v1Instance;
    private File myConfigFile;
    private FileWatcher myChangeObserver;
    private static final String DEFAULT_CONFIG =
            "# Default VersionOne configuration\n" +
            "url = http://localhost:8080/VersionOne/\n" +
                    "userName = admin\n" +
                    "password = admin\n" +
                    "pattern = [A-Z]{1,2}-[0-9]+\n" +
                    "referenceField = Number";

    /**
     * Creates settings instance.
     *
     * @param url            URL to the VersionOne system. URL can't be null
     * @param userName       user name to the VersionOne.
     * @param password       password to the VersionOne.
     * @param pattern        regular expression for finding story
     * @param referenceField name of field
     */
    public Config(String url, String userName, String password, Pattern pattern, String referenceField) {
        if (url == null) {
            throw new IllegalArgumentException("The VersionOne URL Parameter cannot be null");
        }
        this.url = url;
        if (StringUtil.isEmptyOrSpaces(userName)) {
            userName = null;
        }
        this.userName = userName;
        this.password = password;
        this.pattern = pattern;
        this.referenceField = referenceField;
    }

    public Config() {
        url = "http://jsdksrv01:8080/VersionOne/";
        userName = "admin";
        password = "admin";
        pattern = Pattern.compile("[A-Z]{1,2}-[0-9]+");
        referenceField = "Number";
    }

    public Config(String configDir) {
        myConfigFile = new File(configDir, CONFIG_FILENAME);
        if (!myConfigFile.exists()) {
//            throw new RuntimeException("There is no VersionOne config file: " + myConfigFile);
            FileUtil.writeFile(myConfigFile, DEFAULT_CONFIG);
            LOG.warn("Default VersionOne config file created.");
        }
        try {
            loadConfiguration();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load VersionOne config file: " + myConfigFile, e);
        }
        myChangeObserver = new FileWatcher(myConfigFile);
        myChangeObserver.setSleepingPeriod(10000L);
        myChangeObserver.registerListener(this);
        myChangeObserver.start();
    }

    private synchronized void loadConfiguration() throws IOException {
        LOG.info("Loading configuration file: " + myConfigFile.getAbsolutePath());
        final Properties p = new Properties();
        p.load(new FileInputStream(myConfigFile));
        url = p.getProperty("url");
        userName = p.getProperty("userName");
        if (p.contains("password")) {
            password = EncryptUtil.unscramble(p.getProperty("password"));
        }
        pattern = Pattern.compile(p.getProperty("pattern"));
        referenceField = p.getProperty("referenceField");
    }

    public synchronized void save() {
        LOG.info("Saving configuration file: " + myConfigFile.getAbsolutePath());
        myChangeObserver.runActionWithDisabledObserver(new Runnable() {

            public void run() {
                final Properties p = new Properties();
                p.setProperty("url", url);
                p.setProperty("userName", userName);
                if (!StringUtil.isEmptyOrSpaces(password)) {
                    p.setProperty("password", EncryptUtil.scramble(password));
                }
                p.setProperty("pattern", pattern.pattern());
                p.setProperty("referenceField", referenceField);
                try {
                    p.store(new FileOutputStream(myConfigFile), null);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot save configuration file: " + myConfigFile, e);
                }
            }
        });
    }

    @NotNull
    public String getUrl() {
        return url;
    }

    @Nullable
    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Nullable
    public Pattern getPattern() {
        return pattern;
    }

    @Nullable
    public String getReferenceField() {
        return referenceField;
    }

    private V1Instance connect() throws AuthenticationException, ApplicationUnavailableException {
        if (v1Instance == null) {
            if (getUserName() == null) {
                v1Instance = new V1Instance(getUrl());
            } else {
                v1Instance = new V1Instance(getUrl(), getUserName(), getPassword());
            }
            v1Instance.validate();
        }
        return v1Instance;
    }

    /**
     * getting connection to VersionOne server this method MAY BE called ONLY after {@link #isConnectionValid()}
     *
     * @return connection to VersionOne
     */
    public V1Instance getV1Instance() {
        if (v1Instance == null) {
            throw new RuntimeException("You must call isConnectionValid() before calling getV1Instance()");
        }
        return v1Instance;
    }

    /**
     * Validate connection to the VersionOne server
     *
     * @return true if all settings is correct and connection to V1 is valid, false - otherwise
     */
    public boolean isConnectionValid() {
        try {
            connect();
        } catch (SDKException e) {
            LOG.warn("VersionOne connection is invalid: \n" + toString(), e);
            v1Instance = null;
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Config{" +
                "referenceField='" + referenceField + '\'' +
                ", pattern=" + pattern +
                ", password='" + password + '\'' +
                ", userName='" + userName + '\'' +
                ", url='" + url + '\'' +
                '}';
    }

    public void changeOccured(String requestor) {
        //TODO
    }
}
