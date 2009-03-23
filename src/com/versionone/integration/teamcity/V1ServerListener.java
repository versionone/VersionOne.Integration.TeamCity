/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.versionone.integration.common.BuildInfo;
import com.versionone.integration.common.V1Worker;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

/**
 * This class is registered in TeamCity and receives all necessary events.
 */
public class V1ServerListener extends BuildServerAdapter {

    private final SBuildServer myBuildServer;
    private final V1Worker myWorker;
    private final WebLinks weblinks;

    public V1ServerListener(SBuildServer sBuildServer, V1Worker worker, WebLinks weblinks) {
        myBuildServer = sBuildServer;
        myWorker = worker;
        this.weblinks = weblinks;
    }

    public void register() {
        System.out.println("V1ServerListener.register()");
        myBuildServer.addListener(this);
    }

    @Override
    public void buildFinished(SRunningBuild runningBuild) {
        System.out.println("V1ServerListener.buildFinished(): " + runningBuild);

        final TCBuildInfo buildInfo = new TCBuildInfo(runningBuild, weblinks);
        if (buildInfo.isCorrect())
            myWorker.submitBuildRun(buildInfo);
    }

    /**
     * This class is a adapter of TeamCity {@link jetbrains.buildServer.serverSide.SRunningBuild} to {@link BuildInfo}.
     */
    static class TCBuildInfo implements BuildInfo {

        private final SRunningBuild build;
        private final WebLinks weblinks;

        TCBuildInfo(SRunningBuild build, WebLinks weblinks) {
            this.build = build;
            this.weblinks = weblinks;
        }

        @SuppressWarnings({"ConstantConditions"})
        public String getProjectName() {
            return build.getBuildType().getProjectName();
        }

        public long getBuildId() {
            return build.getBuildId();
        }

        public Date getStartTime() {
            return build.getStartDate();
        }

        public long getElapsedTime() {
            return build.getElapsedTime() * 1000;
        }

        public boolean isSuccessful() {
            return build.getBuildStatus().isSuccessful();
        }

        public boolean isTriggered() {
            return build.getTriggeredBy().isTriggeredByUser();
        }

        public List<SVcsModification> getChanges() {
            build.getChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, true);
            return build.getContainingChanges();
        }

        /**
         * Return URL to the current build results.
         *
         * @return url to the TeamCity with info about build
         */
        @NotNull
        public String getUrl() {
            return weblinks.getViewResultsUrl(build);
        }

        public String getBuildName() {
            return build.getBuildNumber();
        }

        public boolean isCorrect() {
            return build.getBuildStatus() != Status.UNKNOWN && build.getBuildType() != null;
        }
    }
}
