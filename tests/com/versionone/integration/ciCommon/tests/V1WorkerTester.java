/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon.tests;

import com.versionone.integration.ciCommon.V1Config;
import com.versionone.integration.ciCommon.V1Worker;
import com.versionone.integration.teamcity.FileConfig;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.Project;
import com.versionone.om.Story;
import com.versionone.om.Task;
import com.versionone.om.V1Instance;
import jetbrains.buildServer.vcs.SVcsModification;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class V1WorkerTester {
    private Mockery mockery = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @After
    public void deleteCfgFile() {
        File file = new File(".", FileConfig.CONFIG_FILENAME);
        file.delete();
    }

    @Test
    public void testGetModificationDescription() {
        final String userName1 = "user name 1";
        final String userName2 = "user name 2";
        final String desc1 = "description 1";
        final String desc2 = "description 2";

        final SVcsModification modification1 = mockery.mock(SVcsModification.class, "changelist 1");
        final SVcsModification modification2 = mockery.mock(SVcsModification.class, "changelist 2");
        List<SVcsModification> modifications = Arrays.asList(modification1, modification2);

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
            }
        });

        final String result = V1Worker.getModificationDescription(modifications);

        Assert.assertTrue(result.contains(userName1));
        Assert.assertTrue(result.contains(userName2));
        Assert.assertTrue(result.contains(desc1));
        Assert.assertTrue(result.contains(desc2));
    }

    //    @Ignore(value = "Integration test")
    @Test
    public void testResolveReference() {
        final V1Config cfg = V1ConfigTest.getValidConfig();
        Assert.assertTrue(cfg.isConnectionValid());

        final String storyName = "TeamCity integ test story";
        final String taskName = "TeamCity integ test task";
        final V1Instance v1Instace = cfg.getV1Instance();
        final Project project = v1Instace.get().projectByID("Scope:0");

        final Story story = v1Instace.create().story(storyName, project);
        final V1Worker worker = new V1Worker(cfg);
        final Task task = story.createTask(taskName);

        List<PrimaryWorkitem> workItems = worker.getPrimaryWorkitemsByReference(story.getDisplayID());

        Assert.assertEquals(1, workItems.size());
        Assert.assertEquals(storyName, workItems.iterator().next().getName());

        workItems = worker.getPrimaryWorkitemsByReference(task.getDisplayID());

        Assert.assertEquals(1, workItems.size());
        Assert.assertEquals(storyName, workItems.iterator().next().getName());

        story.delete();
    }
}