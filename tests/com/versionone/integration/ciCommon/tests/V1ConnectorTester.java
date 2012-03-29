package com.versionone.integration.ciCommon.tests;


import com.versionone.apiclient.IAssetType;
import com.versionone.apiclient.IAttributeDefinition;
import com.versionone.apiclient.IMetaModel;
import com.versionone.apiclient.MetaException;
import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.teamcity.V1Connector;
import com.versionone.om.ApiClientInternals;
import com.versionone.om.ApplicationUnavailableException;
import com.versionone.om.AuthenticationException;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.SecondaryWorkitem;
import com.versionone.om.V1Instance;
import com.versionone.om.V1InstanceGetter;
import com.versionone.om.Workitem;
import com.versionone.om.filters.WorkitemFilter;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

public class V1ConnectorTester {
    private Mockery mockery = new Mockery();
    {
        mockery.setImposteriser(ClassImposteriser.INSTANCE);
    }
    private final V1Instance v1InstanceMock = mockery.mock(V1Instance.class);
    private final IMetaModel metaMock = mockery.mock(IMetaModel.class);
    private final ApiClientInternals apiClientInternalsMock = mockery.mock(ApiClientInternals.class);
    private final IAssetType assetTypeMock = mockery.mock(IAssetType.class);
    private final IAttributeDefinition attributeDefinitionMock = mockery.mock(IAttributeDefinition.class);
    private final V1InstanceGetter v1InstanceGetterMock = mockery.mock(V1InstanceGetter.class);

    private V1Connector connector;

    @Before
    public void before() {
        connector = new TestV1Connector();
        connector.setConnectionSettings(new V1Config());
        connector.isConnectionValid();
    }

    @Test
    public void isReferenceFieldValid() {
        mockery.checking(new Expectations() {
            {
                allowing(v1InstanceMock).getApiClient();
                will(returnValue(apiClientInternalsMock));
                allowing(apiClientInternalsMock).getMetaModel();
                will(returnValue(metaMock));
                allowing(metaMock).getAssetType("PrimaryWorkitem");
                will(returnValue(assetTypeMock));
                allowing(assetTypeMock).getAttributeDefinition(null);
                will(returnValue(null));
            }
        });

        boolean result = connector.isReferenceFieldValid();
        Assert.assertTrue(result);
    }

    @Test
    public void referenceFieldIncorrect() {
        mockery.checking(new Expectations() {
            {
                allowing(v1InstanceMock).getApiClient();
                will(returnValue(apiClientInternalsMock));
                allowing(apiClientInternalsMock).getMetaModel();
                will(returnValue(metaMock));
                allowing(metaMock).getAssetType("PrimaryWorkitem");
                will(returnValue(assetTypeMock));
                allowing(assetTypeMock).getAttributeDefinition(null);
                will(throwException(new MetaException("All are bad.")));
            }
        });

        boolean result = connector.isReferenceFieldValid();
        Assert.assertFalse(result);
    }

    @Test
    public void getPrimaryWorkitemsByReference() {
        final Collection<Workitem> workitems = new ArrayList<Workitem>();
        PrimaryWorkitem primaryWorkitem = mockery.mock(PrimaryWorkitem.class);
        workitems.add(primaryWorkitem);
        workitems.add(primaryWorkitem);

        mockery.checking(new Expectations() {
            {
                one(v1InstanceMock).get();
                will(returnValue(v1InstanceGetterMock));
                one(v1InstanceGetterMock).workitems(with(aNonNull(WorkitemFilter.class)));
                will(returnValue(workitems));
            }
        });
        Collection<PrimaryWorkitem> result = connector.getPrimaryWorkitemsByReference("test");
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void getPrimaryWorkitemsByReferenceWithSecondaryWorkitems() {
        final Collection<Workitem> workitems = new ArrayList<Workitem>();
        final PrimaryWorkitem primaryWorkitem = mockery.mock(PrimaryWorkitem.class);
        final SecondaryWorkitem secondaryWorkitem = mockery.mock(SecondaryWorkitem.class);
        workitems.add(secondaryWorkitem);
        workitems.add(primaryWorkitem);

        mockery.checking(new Expectations() {
            {
                one(v1InstanceMock).get();
                will(returnValue(v1InstanceGetterMock));
                one(v1InstanceGetterMock).workitems(with(aNonNull(WorkitemFilter.class)));
                will(returnValue(workitems));
                one(secondaryWorkitem).getParent();
                will(returnValue(primaryWorkitem));
            }
        });
        Collection<PrimaryWorkitem> result = connector.getPrimaryWorkitemsByReference("test");
        Assert.assertEquals(2, result.size());
    }

    private class TestV1Connector extends V1Connector {
        @Override
        protected V1Instance connect() throws AuthenticationException, ApplicationUnavailableException {
            v1Instance = v1InstanceMock;
            return v1InstanceMock;
        }
    }

}
