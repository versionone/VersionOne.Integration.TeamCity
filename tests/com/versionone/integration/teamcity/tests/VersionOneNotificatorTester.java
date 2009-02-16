package com.versionone.integration.teamcity.tests;

import org.junit.Test;
import org.junit.Assert;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Mockery;
import org.jmock.Expectations;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.serverSide.SRunningBuild;

import java.util.List;
import java.util.Arrays;

import com.versionone.integration.teamCity1.VersionOneNotificator;


public class VersionOneNotificatorTester {
    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };



    @Test
    public void testGetModificationDescription() {
        final String UserName1 = "user name 1";
        final String UserName2 = "user name 2";
        final String desc1 = "description 1";
        final String desc2 = "description 2";

        final SVcsModification modification1 = mockery.mock(SVcsModification.class, "changelist 1");
        final SVcsModification modification2 = mockery.mock(SVcsModification.class, "changelist 2");
        List<SVcsModification> modifications = Arrays.asList(modification1, modification2);

        VersionOneNotificator notification = new VersionOneNotificator(null, null);


        mockery.checking(new Expectations() {
            {
                allowing (modification2).getUserName();
                will(returnValue(UserName2));
                allowing (modification1).getDescription();
                will(returnValue(desc1));
                allowing (modification2).getDescription();
                will(returnValue(desc2));
                allowing (modification1).getUserName();
                will(returnValue(UserName1));

            }
        });

        String result = notification.getModificationDescription(modifications);

        Assert.assertEquals(result, UserName1 + ": " + desc1 + "<br>" + UserName2 + ": " + desc2);

    }

    @Test
    public void testGetUrlToTÑ() {
        final WebLinks links = mockery.mock(WebLinks.class, "weblinks");
        final SRunningBuild sRunningBuild = mockery.mock(SRunningBuild.class, "runningbuild");
        final String domain = "http://localhost";
        final long buildId = 10;
        final String buildType = "bt";
        String extectedUrl = domain + "/" + "viewLog.html?buildId=" + buildId;
        extectedUrl += "&tab=buildResultsDiv&buildTypeId=" + buildType;

        mockery.checking(new Expectations() {
            {
                allowing (links).getRootUrl();
                will(returnValue(domain));
                allowing (sRunningBuild).getBuildId();
                will(returnValue(buildId));
                allowing (sRunningBuild).getBuildTypeId();
                will(returnValue(buildType));
            }
        });

        VersionOneNotificator notification = new VersionOneNotificator(null, links);
        String url = notification.getUrlToTÑ(sRunningBuild);


        Assert.assertEquals(url, extectedUrl);
    }
}