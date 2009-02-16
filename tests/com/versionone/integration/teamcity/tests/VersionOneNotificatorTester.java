package com.versionone.integration.teamcity.tests;

import org.junit.Test;
import org.junit.Assert;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.Mockery;
import org.jmock.Expectations;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.users.SUser;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;

import com.versionone.integration.teamcity.VersionOneNotificator;
import com.versionone.integration.teamcity.Settings;


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


    @Test
    public void testGetStartType() {
        final SUser user = mockery.mock(SUser.class, "user");
        VersionOneNotificator notification = new VersionOneNotificator(null, null);

        Assert.assertEquals("forced", notification.getStartType(user));
        Assert.assertEquals("trigger", notification.getStartType(null));
    }

    @Test
    public void testTaskId() {
        final VersionOneNotificator notification = new VersionOneNotificator(null, null);
        //final Settings settings = new Settings("http://qqq.qqq","1","1", , "Number");

        final Map<String, List<String>> comments = new HashMap<String, List<String>>();

        comments.put("testing TD-123 dflkxbc", newList("TD-123"));
        comments.put("TD-1232", newList("TD-1232"));
        comments.put("-------TC-12--------", newList("TC-12"));
        comments.put("------- TC-12 --------", newList("TC-12"));
        comments.put("-------TC-12-----TC-223---", newList("TC-12", "TC-223"));
        comments.put("Comment without id", new LinkedList<String>());
        comments.put("------- TSC-12 --------", newList("SC-12"));
        comments.put("------- Tc-12 --------", new LinkedList<String>());
        comments.put("------- _T-12 --------", newList("T-12"));
        comments.put("------- TC12 --------", new LinkedList<String>());
        comments.put("------- TC- --------", new LinkedList<String>());
        comments.put("------- TESTING-12/24 done --------", newList("NG-12"));

        final Pattern pattern = Pattern.compile("[A-Z]{1,2}-[0-9]+");

        for (String comment : comments.keySet()) {
            List<String> actuals = notification.getTasksId(comment, pattern);
            List<String> expected = comments.get(comment);
            Assert.assertTrue(actuals.containsAll(expected));
            Assert.assertEquals(actuals.toString(), expected.size(), actuals.size());
        }
    }

    private static <T> List<T> newList(T... s) {
        return Arrays.asList(s);
    }

}