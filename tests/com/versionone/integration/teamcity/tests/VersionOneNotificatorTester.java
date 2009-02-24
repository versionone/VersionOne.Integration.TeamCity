/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity.tests;

import com.versionone.DB;
import com.versionone.Duration;
import com.versionone.integration.teamcity.Settings;
import com.versionone.integration.teamcity.VersionOneNotificator;
import com.versionone.om.BuildProject;
import com.versionone.om.BuildRun;
import com.versionone.om.Iteration;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.Project;
import com.versionone.om.Schedule;
import com.versionone.om.Story;
import com.versionone.om.Task;
import com.versionone.om.V1Instance;
import com.versionone.om.filters.BuildProjectFilter;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;


public class VersionOneNotificatorTester {
    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    //these need only for integration tests
    private final String v1Url = "http://domen/VersionOne";
    private final String v1UserName = "login";
    private final String v1Password = "password";

    @Test
    public void testGetModificationDescription() {
        final String userName1 = "user name 1";
        final String userName2 = "user name 2";
        final String desc1 = "description 1";
        final String desc2 = "description 2";

        final SVcsModification modification1 = mockery.mock(SVcsModification.class, "changelist 1");
        final SVcsModification modification2 = mockery.mock(SVcsModification.class, "changelist 2");
        final SVcsModification modification3 = mockery.mock(SVcsModification.class, "changelist 3");
        List<SVcsModification> modifications = Arrays.asList(modification1, modification2, modification3);

        VersionOneNotificator notification = new VersionOneNotificator(null, null);


        mockery.checking(new Expectations() {
            {
                allowing(modification1).getDescription();
                will(returnValue(desc1));
                allowing(modification2).getDescription();
                will(returnValue(desc2));
                allowing(modification1).getUserName();
                will(returnValue(userName1));
                allowing(modification2).getUserName();
                will(returnValue(userName2));

                allowing(modification3).getDescription();
                will(returnValue(desc1));
                allowing(modification3).getUserName();
                will(returnValue(userName2));
            }
        });

        String result = notification.getModificationDescription(modifications);

        Assert.assertEquals(result, userName1 + ": " + desc1 + "<br>" + userName2 + ": " + desc1 + "<br>" + userName2 + ": " + desc2);

    }

    @Test
    public void testGetUrlToTC() {
        final WebLinks links = mockery.mock(WebLinks.class, "weblinks");
        final SRunningBuild sRunningBuild = mockery.mock(SRunningBuild.class, "runningbuild");
        final String domain = "http://localhost";
        final long buildId = 10;
        final String buildType = "bt";
        String extectedUrl = domain + "/" + "viewLog.html?buildId=" + buildId;
        extectedUrl += "&tab=buildResultsDiv&buildTypeId=" + buildType;

        mockery.checking(new Expectations() {
            {
                allowing(links).getRootUrl();
                will(returnValue(domain));
                allowing(sRunningBuild).getBuildId();
                will(returnValue(buildId));
                allowing(sRunningBuild).getBuildTypeId();
                will(returnValue(buildType));
            }
        });

        VersionOneNotificator notification = new VersionOneNotificator(null, links);
        String url = notification.getUrlToTÑ(sRunningBuild);

        Assert.assertEquals(url, extectedUrl);
    }


    @Test
    public void testTaskId() {
        final VersionOneNotificator notification = new VersionOneNotificator(null, null);

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

        List<String> actuals = notification.getTasksId("testing TD-123 dflkxbc", null);
        Assert.assertEquals(0, actuals.size());
    }

    private static <T> List<T> newList(T... s) {
        return Arrays.asList(s);
    }

    //Inegration test
    @Ignore
    @Test
    public void testResolveReference() {
        final Settings settings = new Settings(v1Url, v1UserName, v1Password, null, "Number");

        if (settings.isConnectionValid()) {
            final String storyName = "TeamCity integ test story";
            final String taskName = "TeamCity integ test task";
            final V1Instance v1Instace = settings.getV1Instance();
            final Project project = v1Instace.get().projectByID("Scope:0");

            final Story story = v1Instace.create().story(storyName, project);
            final VersionOneNotificator notification = new VersionOneNotificator(null, null);
            final Task task = story.createTask(taskName);

            List<PrimaryWorkitem> workItems = notification.resolveReference(story.getDisplayID(), settings);

            Assert.assertEquals(1, workItems.size());
            Assert.assertEquals(storyName, workItems.iterator().next().getName());

            workItems = notification.resolveReference(task.getDisplayID(), settings);

            Assert.assertEquals(1, workItems.size());
            Assert.assertEquals(storyName, workItems.iterator().next().getName());

            story.delete();
        } else {
            Assert.fail("Connection is not valide");
        }
    }

    //integration test of all process
    @Ignore
    @Test
    public void testNotify() {
        final String projectName = "TeamCity Project test";
        final String domain = "http://localhost";
        final String buildType = "bt";
        final long buildId = 4000;
        final long elapsedTime = 10;
        final long commitId = Math.abs(new Random().nextInt(2000000));
        final Date startDate = new Date();
        final Date commitDate = DB.DateTime.now().add(Calendar.MINUTE, -1).getValue();
        final String userNameCommiter = "commiter";
        final Settings settings = new Settings(v1Url, v1UserName, v1Password, Pattern.compile("[A-Z]{1,2}-[0-9]+"), "Number");
        final boolean isTriggeredByUser = true;

        final SRunningBuild sRunningBuild = mockery.mock(SRunningBuild.class, "runningbuild");
        final SBuildType sBuildType = mockery.mock(SBuildType.class, "sbuildtype");
        final SVcsModification svcsModification = mockery.mock(SVcsModification.class, "SVcsModification");
        final WebLinks links = mockery.mock(WebLinks.class, "weblinks");
        final List<SVcsModification> changes = Arrays.asList(svcsModification);
        final TriggeredBy triggeredBy = mockery.mock(TriggeredBy.class, "TriggeredBy");

        //create data in versionOne
        Project project = null;
        Story story = null;
        Task task = null;
        Schedule schedule = null;
        Iteration iteration = null;
        BuildProject buildProject = null;
        final String scopeZero = "Scope:0";
        final String addName = "teamcity";
        final String scheduleName = "Schedule " + addName + " test" + " " + new Date().toString();
        final String itarationName = "Iteration for test " + addName + " " + new Date().toString();
        final String storyName = "Story " + addName + " " + new Date().toString();
        final String taskName = "Task " + addName + " " + new Date().toString();
        final String buildProjectName = projectName + " " + new Date().toString();
        final String status = "Passed";
        String extectedUrl = domain + "/" + "viewLog.html?buildId=" + buildId;
        extectedUrl += "&tab=buildResultsDiv&buildTypeId=" + buildType;


        try {
            final V1Instance v1 = new V1Instance(settings.getV1Url(), settings.getV1UserName(), settings.getV1Password());
            v1.validate();
            schedule = v1.create().schedule(scheduleName, new Duration(14, Duration.Unit.Days), new Duration(0, Duration.Unit.Days));

            buildProject = v1.create().buildProject(buildProjectName, buildProjectName);

            final Project rootProject = v1.get().projectByID(scopeZero);
            project = rootProject.createSubProject(projectName, DB.DateTime.now());
            project.setSchedule(schedule);
            project.getBuildProjects().add(buildProject);
            project.save();

            final Date dateStart = new Date();
            final Date dateEnd = new Date();
            dateEnd.setTime(new Date().getTime() + 100 * 60 * 60 * 24 * 31);
            iteration = project.createIteration(itarationName, new DB.DateTime(dateStart), new DB.DateTime(dateEnd));
            iteration.activate();
            iteration.save();

            story = project.createStory(storyName);
            story.setIteration(iteration);
            story.save();

            final String commitDesctription = "comment form user" + story.getDisplayID();

            task = story.createTask(taskName);
            task.save();

            mockery.checking(new Expectations() {
                {
                    allowing(sRunningBuild).getBuildId();
                    will(returnValue(buildId));
                    allowing(sRunningBuild).getBuildType();
                    will(returnValue(sBuildType));
                    allowing(sBuildType).getProjectName();
                    will(returnValue(buildProjectName));
                    allowing(sRunningBuild).getChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, true);
                    will(returnValue(changes));

                    //change set
                    allowing(svcsModification).getId();
                    will(returnValue(commitId));
                    allowing(svcsModification).getUserName();
                    will(returnValue(userNameCommiter));
                    allowing(svcsModification).getVcsDate();
                    will(returnValue(commitDate));
                    allowing(svcsModification).getDescription();
                    will(returnValue(commitDesctription));

                    //getBuildRun
                    allowing(sRunningBuild).getElapsedTime();
                    will(returnValue(elapsedTime));
                    allowing(sRunningBuild).getTriggeredBy();
                    will(returnValue(triggeredBy));
                    allowing(triggeredBy).isTriggeredByUser();
                    will(returnValue(isTriggeredByUser));
                    allowing(sRunningBuild).getClientStartDate();
                    will(returnValue(startDate));

                    //getUrlToTÑ
                    allowing(links).getRootUrl();
                    will(returnValue(domain));
                    allowing(sRunningBuild).getBuildTypeId();
                    will(returnValue(buildType));
                }
            });
            final VersionOneNotificator notification = new VersionOneNotificator(null, links);

            notification.notify(status, sRunningBuild, settings);

            BuildProjectFilter buildProjectFilter = new BuildProjectFilter();
            buildProjectFilter.references.add(buildProjectName);

            buildProject = v1.get().buildProjects(buildProjectFilter).iterator().next();

            Collection<BuildRun> newBuildRuns = buildProject.getBuildRuns(null);

            final BuildRun run = newBuildRuns.iterator().next();

            Assert.assertEquals(buildProjectName + " - build." + buildId, run.getName());
            Assert.assertEquals(extectedUrl, run.getLinks(null).iterator().next().getURL());
            Assert.assertEquals(elapsedTime * 1000, run.getElapsed(), 0.001);
            Assert.assertEquals(buildProject.getName(), run.getBuildProject().getName());
            Assert.assertEquals(status, run.getStatus().getCurrentValue());
            Assert.assertEquals("Forced", run.getSource().getCurrentValue());
            Assert.assertEquals(String.valueOf(buildId), run.getReference());
            Assert.assertEquals(userNameCommiter + ": " + commitDesctription, run.getDescription());

            Assert.assertEquals(1, run.getChangeSets().size());
            Assert.assertEquals(userNameCommiter + " on " + commitDate.toString(), run.getChangeSets().iterator().next().getName());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (task != null) {
                task.delete();
            }

            if (story != null) {
                story.setIteration(null);
                story.delete();
            }

            if (project != null) {
                project.setSchedule(null);
                project.delete();
            }

            if (iteration != null) {
                iteration.delete();
            }

            if (schedule != null) {
                schedule.delete();
            }

            if (buildProject != null) {
                buildProject.close();
                if (buildProject.canDelete()) {
                    buildProject.delete();
                }
            }
        }


    }

}