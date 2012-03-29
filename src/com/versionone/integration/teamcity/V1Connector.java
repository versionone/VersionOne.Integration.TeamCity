/*(c) Copyright 2012, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.MetaException;
import com.versionone.integration.ciCommon.V1Config;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.ProxySettings;
import com.versionone.om.SDKException;
import com.versionone.om.SecondaryWorkitem;
import com.versionone.om.V1Instance;
import com.versionone.om.Workitem;
import com.versionone.om.filters.WorkitemFilter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Connector to VersionOne server.
 */
public class V1Connector {
    protected V1Instance v1Instance;
    protected V1Config config;

    /**
     * Validate connection to the VersionOne server
     *
     * @return true if all settings is correct and connection to V1 is valid, false - otherwise
     */
    public boolean isConnectionValid() {
        try {
            connect();
            return true;
        } catch (SDKException e) {
            v1Instance = null;
            e.printStackTrace();
            return false;
        }
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

    protected V1Instance connect() throws AuthenticationException, ApplicationUnavailableException {
        if (v1Instance == null) {
            if (config.getUserName() == null) {
                v1Instance = new V1Instance(config.getUrl(), null, null, getProxySettings());
            } else {
                v1Instance = new V1Instance(config.getUrl(), config.getUserName(), config.getPassword(), getProxySettings());
            }
            v1Instance.validate();
        }
        return v1Instance;
    }

    private ProxySettings getProxySettings() {
        if (!config.getProxyUsed()) {
            return null;
        }
        URI uri;
        try {
            uri = new URI(config.getProxyUri());
        } catch (URISyntaxException e) {
            return null;
        }
        return new ProxySettings(uri, config.getProxyUser(), config.getProxyPassword());
    }


    /**
     * Checks whether {@link #} value is valid. Can be called only after {@link #isConnectionValid()
     * returned true}.
     *
     * @return true if reference field is valid, otherwise - false
     */
    public boolean isReferenceFieldValid() {
        try {
            final IMetaModel meta = getV1Instance().getApiClient().getMetaModel();
            meta.getAssetType("PrimaryWorkitem").getAttributeDefinition(config.getReferenceField());
            return true;
        } catch (MetaException e) {
            return false;
        }
    }

    /**
     * Resolve a check-in comment identifier to a PrimaryWorkitem. if the reference matches a SecondaryWorkitem, we need
     * to navigate to the parent.
     *
     * @param reference The identifier in the check-in comment.
     * @return A collection of matching PrimaryWorkitems.
     */
    public List<PrimaryWorkitem> getPrimaryWorkitemsByReference(String reference) {
        List<PrimaryWorkitem> result = new ArrayList<PrimaryWorkitem>();

        WorkitemFilter filter = new WorkitemFilter();
        filter.find.setSearchString(reference);
        filter.find.fields.add(config.getReferenceField());
        Collection<Workitem> workitems = getV1Instance().get().workitems(filter);
        for (Workitem workitem : workitems) {
            if (workitem instanceof PrimaryWorkitem) {
                result.add((PrimaryWorkitem) workitem);
            } else if (workitem instanceof SecondaryWorkitem) {
                result.add(((SecondaryWorkitem) workitem).getParent());
            } else {
                throw new RuntimeException("Found unexpected Workitem type: " + workitem.getClass());
            }
        }

        return result;
    }

    public void disconnect() {
        v1Instance = null;
    }

    public void setConnectionSettings(V1Config config) {
        disconnect();
        this.config = config;
    }

    //public FileConfig getConfig() {
    //    return this.config;
    //}
}
