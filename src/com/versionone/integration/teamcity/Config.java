/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.versionone.integration.common.IConfig;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.SDKException;
import com.versionone.om.V1Instance;
import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

public class Config implements ChangeListener, IConfig {

    private static final Logger LOG = Logger.getInstance(Config.class.getName());
    public static final String CONFIG_FILENAME = "versionone-config.properties";
    /**
     * Interval in seconds configuration file is monitored in.
     */
    public static final int FILE_MONITOR_INTERVAL = 10;

    private String url;
    private String userName;
    private String password;
    private Pattern pattern;
    private String referenceField;

    private V1Instance v1Instance;
    private File myConfigFile;
    private FileWatcher myChangeObserver;

    public Config(String configDir) {
        myConfigFile = new File(configDir, CONFIG_FILENAME);
        myChangeObserver = new FileWatcher(myConfigFile);
        myChangeObserver.setSleepingPeriod(FILE_MONITOR_INTERVAL * 1000L);
        myChangeObserver.registerListener(this);
        myChangeObserver.start();
        if (!myConfigFile.exists()) {
//            throw new RuntimeException("There is no VersionOne config file: " + myConfigFile);
//            FileUtil.writeFile(myConfigFile, DEFAULT_CONFIG);
            setDefConfig();
            save();
            LOG.warn("Default VersionOne config file created.");
        } else {
            loadConfiguration();
        }
        LOG.info("VersionOne configuraiton file " + myConfigFile.getAbsolutePath() +
                " will be monitored with interval " + FILE_MONITOR_INTERVAL + " seconds.");
    }

    public void setDefConfig() {
        url = "http://localhost/VersionOne/";
        userName = "admin";
        password = "admin";
        pattern = Pattern.compile("[A-Z]{1,2}-[0-9]+");
        referenceField = "Number";
    }

    private synchronized void loadConfiguration() {
        FileInputStream stream = null;
        try {
            LOG.info("Loading VersionOne configuration file: " + myConfigFile.getAbsolutePath());
            final Properties prop = new Properties();
            stream = new FileInputStream(myConfigFile);
            prop.load(stream);
            url = prop.getProperty("url");
            userName = prop.getProperty("userName");
            final String pass = prop.getProperty("password");
            password = StringUtil.isEmptyOrSpaces(pass) ? null : EncryptUtil.unscramble(pass);
            pattern = Pattern.compile(prop.getProperty("pattern"));
            referenceField = prop.getProperty("referenceField");
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

    @NotNull
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Nullable
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Nullable
    public Pattern getPatternObj() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    @Nullable
    public String getReferenceField() {
        return referenceField;
    }

    public void setReferenceField(String referenceField) {
        this.referenceField = referenceField;
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
            throw new IllegalStateException("You must call isConnectionValid() before calling getV1Instance()");
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
            LOG.warn("VersionOne connection is invalid: \n\t" + toString(), e);
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
        loadConfiguration();
    }
}
