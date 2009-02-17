/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import jetbrains.buildServer.Build;
import jetbrains.buildServer.web.openapi.PluginException;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;
import com.versionone.om.V1Instance;
import com.versionone.om.BuildProject;
import com.versionone.om.BuildRun;
import com.versionone.om.ChangeSet;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.Workitem;
import com.versionone.om.SecondaryWorkitem;
import com.versionone.om.filters.BuildProjectFilter;
import com.versionone.om.filters.ChangeSetFilter;
import com.versionone.om.filters.WorkitemFilter;
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
            final Settings settings = new Settings(user);
            notification(status, sRunningBuild, settings);
        }
    }

    /**
     * Add to the VersionOne BuildRun and ChangesSet
     *
     * @param status result of build(passed or failed)
     * @param sRunningBuild build data
     * @param settings user settings 
     */
    public void notification(String status, SRunningBuild sRunningBuild, final Settings settings) {
        //cancel notification if connection is not valide
        if (!settings.isConnectionValid()) {
            throw new PluginException("Warning: '" + settings.getV1UserName() + "'user can't connect to the VersionOne.");
        }

        //cancel notification if BuildType is empty
        if (sRunningBuild == null || sRunningBuild.getBuildType() == null) {
            return;
        }

        String projectName = sRunningBuild.getBuildType().getProjectName();
        String buildName = projectName + " - build." + sRunningBuild.getBuildId();
        BuildProject buildProject = getBuildProject(projectName, settings.getV1Instance());

        if (buildProject != null) {
            List<SVcsModification> changes = sRunningBuild.getChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, true);
            BuildRun run = getBuildRun(status, sRunningBuild, buildName, buildProject, changes);

            setChangeSets(run, changes, settings);
        }

    }


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
     * @param v1Instance  connection to the Version One
     * @return V1 representation of the project if match; otherwise, null.
     */
    //TODO add integration test
    private BuildProject getBuildProject(String projectName, V1Instance v1Instance) {
        BuildProjectFilter filter = new BuildProjectFilter();

        filter.references.add(projectName);
        Collection<BuildProject> projects = v1Instance.get().buildProjects(filter);
        if (projects.isEmpty()) {
            return null;
        }
        return projects.iterator().next();
    }


    private BuildRun getBuildRun(String status, SRunningBuild sRunningBuild, String buildName,
                                 BuildProject buildProject, List<SVcsModification> changes) {
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

    public String getStartType(SUser user) {
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

    private void setChangeSets(BuildRun buildRun, List<SVcsModification> changes, Settings settings) {
        for (SVcsModification change : changes) {
            // See if we have this ChangeSet in the system.
            ChangeSetFilter filter = new ChangeSetFilter();
            String id = String.valueOf(change.getId());
            if (id == null) {
                continue;
            }

            filter.reference.add(id);
            Collection<ChangeSet> changeSets = settings.getV1Instance().get().changeSets(filter);
            if (changeSets.size() == 0) {
                // We don't have one yet. Create one.
                String name = change.getUserName() + " on " + change.getVcsDate();
                ChangeSet changeSet = settings.getV1Instance().create().changeSet(name, id);
                changeSets = new ArrayList<ChangeSet>(1);
                changeSets.add(changeSet);
            }

            Set<PrimaryWorkitem> workitems = determineWorkitems(change.getDescription(), settings);

            associateWithBuildRun(buildRun, changeSets, workitems);
        }
    }

    private void associateWithBuildRun(BuildRun buildRun, Collection<ChangeSet> changeSets,
                                       Set<PrimaryWorkitem> workitems) {
        for (ChangeSet changeSet : changeSets) {
            buildRun.getChangeSets().add(changeSet);
            for (PrimaryWorkitem workitem : workitems) {
                final Collection<BuildRun> completedIn = workitem.getCompletedIn();
                final List<BuildRun> toRemove = new ArrayList<BuildRun>(completedIn.size());

                changeSet.getPrimaryWorkitems().add(workitem);

                for (BuildRun otherRun : completedIn) {
                    if (otherRun.getBuildProject().equals(buildRun.getBuildProject())) {
                        toRemove.add(otherRun);
                    }
                }

                for (BuildRun buildRunDel : toRemove) {
                    completedIn.remove(buildRunDel);
                }

                completedIn.add(buildRun);
            }
        }
    }

    private Set<PrimaryWorkitem> determineWorkitems(String comment, Settings settings) {
        List<String> ids = getTasksId(comment, settings.getPattern());
        Set<PrimaryWorkitem> result = new HashSet<PrimaryWorkitem>(ids.size());

        for (String id : ids) {
            result.addAll(resolveReference(id, settings));
        }
        return result;
    }

   /**
     * Resolve a check-in comment identifier to a PrimaryWorkitem. if the
     * reference matches a SecondaryWorkitem, we need to navigate to the
     * parent.
     *
     * @param reference The identifier in the check-in comment.
     * @param settings settings for user
     * @return A collection of matching PrimaryWorkitems.
     */
    public List<PrimaryWorkitem> resolveReference(String reference, Settings settings){
        List<PrimaryWorkitem> result = new ArrayList<PrimaryWorkitem>();

        WorkitemFilter filter = new WorkitemFilter();
        filter.find.setSearchString(reference);
        filter.find.fields.add(settings.getReferenceField());
        Collection<Workitem> workitems = settings.getV1Instance().get().workitems(filter);
        for (Workitem workitem : workitems) {
            if (workitem instanceof PrimaryWorkitem) {
                result.add((PrimaryWorkitem) workitem);
            } else if (workitem instanceof SecondaryWorkitem) {
                result.add(((SecondaryWorkitem) workitem).getParent());
            } else {
                final String message = "Found unexpected Workitem type: " + workitem.getClass();
                throw new PluginException(message);
            }
        }

        return result;
    }

    /**
     * Return list of tasks got from the comment string
     *
     * @param comment           string with some text with ids of tasks which cut using
     *                          pattern set in the referenceexpression attribute
     * @param v1PatternCommit   regular expression for comment parse and getting data from it
     * @return list of cut ids
     */
    public List<String> getTasksId(String comment, Pattern v1PatternCommit) {
        List<String> result = new LinkedList<String>();

        if (v1PatternCommit != null) {
            Matcher m = v1PatternCommit.matcher(comment);
            while (m.find()) {
                result.add(m.group());
            }
        }

        return result;
    }
}
