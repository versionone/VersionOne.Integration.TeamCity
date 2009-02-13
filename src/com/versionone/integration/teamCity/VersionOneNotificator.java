/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamCity;

import jetbrains.buildServer.Build;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import jetbrains.buildServer.notification.NotificatorAdapter;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.users.SUser;

import java.util.Set;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import com.versionone.om.V1Instance;
import com.versionone.om.BuildProject;
import com.versionone.om.BuildRun;
import com.versionone.om.filters.BuildProjectFilter;
import com.versionone.DB;


public class VersionOneNotificator extends NotificatorAdapter {    
    // plugin UID
    static final String TYPE = "V1Integration";
    // plugun Name
    static final String TYPE_NAME = "Version One Integraion";

    private final WebLinks weblinks;

    public VersionOneNotificator(NotificatorRegistry notificatorRegistry, WebLinks weblinks) {
        this.weblinks = weblinks;

        Settings.registerSettings(this, notificatorRegistry);
    }


//    public void notifyBuildStarted(SRunningBuild build, Set<SUser> users) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }

    public void notifyBuildSuccessful(SRunningBuild build, Set<SUser> users) {
        notifyAllUsers("passed", build, users);
    }

    public void notifyBuildFailed(SRunningBuild build, Set<SUser> users) {
        notifyAllUsers("failed", build, users);
    }

    public void notifyLabelingFailed(Build build, VcsRoot root, Throwable exception, Set<SUser> users) {

    }

    private void notifyAllUsers(String status, SRunningBuild sRunningBuild, Set<SUser> users) {
        for (SUser user : users) {
            notification(status, sRunningBuild, user);
        }
    }

    private void notification(String status, SRunningBuild sRunningBuild, SUser user) {
        Settings settings = new Settings(user);

        //cancel notification if connection is not valide
        if (!settings.isConnectionValid()) {
            return;
        }

        //cancel notification if BuildType is empty
        if (sRunningBuild.getBuildType() == null) {
            return;
        }
        
        String projectName = "Unknown project";
        projectName = sRunningBuild.getBuildType().getProjectName();
        String buildName = projectName + " - build." + sRunningBuild.getBuildId() ;
        BuildProject buildProject = getBuildProject(projectName, settings.getV1Instance());

        if (buildProject != null) {
            List<SVcsModification> changes = sRunningBuild.getChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, true);
            BuildRun run = getBuildRun(status, sRunningBuild, buildName, buildProject, changes);
        }
        
    }




//
//    public void notifyBuildFailing(SRunningBuild build, Set<SUser> users) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void notifyBuildProbablyHanging(SRunningBuild build, Set<SUser> users) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    public void notifyResponsibleChanged(SBuildType buildType, Set<SUser> users) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }

    @NotNull
    public String getNotificatorType() {
        return TYPE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    public String getDisplayName() {
        return TYPE_NAME;  //To change body of implemented methods use File | Settings | File Templates.
    }

    ///

    /**
     * Find the first BuildProject where the Reference matches the projectName.
     *
     * @param projectName name if the project to find.
     * @param v1Instance connection to the Version One
     * @return V1 representation of the project if match; otherwise, null.
     */
    //TODO add test test
    private BuildProject getBuildProject(String projectName, V1Instance v1Instance) {
        BuildProjectFilter filter = new BuildProjectFilter();

        filter.references.add(projectName);
        Collection<BuildProject> projects = v1Instance.get().buildProjects(filter);
        if (projects.isEmpty()) {
            return null;
        }
        return projects.iterator().next();
    }


    private BuildRun getBuildRun(String status, SRunningBuild sRunningBuild, String buildName, BuildProject buildProject, List<SVcsModification> changes) {
        // Generate the BuildRun instance to be saved to the recipient

        BuildRun run = buildProject.createBuildRun(buildName, new DB.DateTime(sRunningBuild.getClientStartDate()));
        //run.setElapsed(getElapsed(elapsedSecound));
        run.setElapsed(sRunningBuild.getElapsedTime() * 1000D);
        run.setReference(String.valueOf(sRunningBuild.getBuildId()));
        run.getSource().setCurrentValue(getStartType(sRunningBuild.getTriggeredBy().getUser()));
        run.getStatus().setCurrentValue(status);

        if (!changes.isEmpty()) {
            run.setDescription(getModificationDescription(changes));
        }
        run.save();

        final String str = getUrlToTÑ(sRunningBuild);
        if (str != null) {
            run.createLink("Build Report", str, true);
        }
        return run;
    }

    public String getUrlToTÑ(SRunningBuild sRunningBuild) {

        String url = weblinks.getRootUrl() + "/";
        url += "viewLog.html?buildId=" + sRunningBuild.getBuildId();
        url += "&tab=buildResultsDiv&buildTypeId=";
        url += sRunningBuild.getBuildTypeId();

        return url;
    }

    private String getStartType(SUser user) {
        return user == null ? "trigger" : "forced";
    }

    /**
     * Evaluate BuildRun description.
     *
     * @param changes - set of changes affected by this BuildRun.
     * @return description string.
     */
    public String getModificationDescription(List<SVcsModification> changes) {

        //Create Set to filter changes uniquee by User and Comment
        Set<SVcsModification> comments = new TreeSet<SVcsModification>(

                //Compares only by UserName and Comment
                new Comparator<SVcsModification>() {
                    public int compare(SVcsModification o1, SVcsModification o2) {
                        int equal = o1.getUserName().compareTo(o2.getUserName());
                        if (equal == 0) {
                            equal = o1.getDescription().compareTo(o2.getDescription());
                        }
                        return equal;
                    }
                });
        comments.addAll(changes);

        StringBuilder result = new StringBuilder(256);
        for (Iterator<SVcsModification> it = comments.iterator(); it.hasNext();) {
            SVcsModification mod = it.next();
            result.append(mod.getUserName());
            result.append(": ");
            result.append(mod.getDescription());
            if (it.hasNext()) {
                result.append("<br>");
            }
        }

        return result.toString();
    }
}
