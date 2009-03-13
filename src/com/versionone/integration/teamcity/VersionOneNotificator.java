/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.teamcity;

import com.versionone.DB;
import com.versionone.om.BuildProject;
import com.versionone.om.BuildRun;
import com.versionone.om.ChangeSet;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.SecondaryWorkitem;
import com.versionone.om.V1Instance;
import com.versionone.om.Workitem;
import com.versionone.om.filters.BuildProjectFilter;
import com.versionone.om.filters.ChangeSetFilter;
import com.versionone.om.filters.WorkitemFilter;
import com.versionone.om.filters.BuildRunFilter;
import jetbrains.buildServer.Build;
import jetbrains.buildServer.notification.NotificatorAdapter;
import jetbrains.buildServer.notification.NotificatorRegistry;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.WebLinks;
import jetbrains.buildServer.serverSide.TriggeredBy;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.SelectPrevBuildPolicy;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.web.openapi.PluginException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VersionOneNotificator extends NotificatorAdapter {

    //Statuses of notify
    public final static int NOTIFY_SUCCESS = 0;
    public final static int NOTIFY_FAIL_CONNECTION = 1;
    public final static int NOTIFY_FAIL_DUPLICATE = 2;
    public final static int NOTIFY_FAIL_NO_BUILDPROJECT = 3;

    // plugin UID
    static final String TYPE = "VersionOneIntegrationNotificator";
    // plugun display name
    private static final String PLUGIN_NAME = "VersionOne Integration";

    private final WebLinks weblinks;

    public VersionOneNotificator(NotificatorRegistry notificatorRegistry, WebLinks weblinks) {
        this.weblinks = weblinks;
        Settings.registerSettings(this, notificatorRegistry);
    }

    @Override
    public void notifyBuildSuccessful(SRunningBuild build, Set<SUser> users) {
        notifyAllUsers("passed", build, users);
    }

    @Override
    public void notifyBuildFailed(SRunningBuild build, Set<SUser> users) {
        notifyAllUsers("failed", build, users);
    }

    public void notifyLabelingFailed(Build build, VcsRoot root, Throwable exception, Set<SUser> users) {
    }

    private void notifyAllUsers(String status, SRunningBuild sRunningBuild, Set<SUser> users) {
        //notificate only if BuildType is not empty
        if (sRunningBuild != null && sRunningBuild.getBuildType() != null) {
            for (SUser user : users) {
                final Settings settings = new Settings(user);
                final int notifyResult = notify(status, sRunningBuild, settings);
                final String name = user.getName().equals("") ? user.getUsername() : user.getName();

                if (notifyResult == NOTIFY_FAIL_NO_BUILDPROJECT) {
                    outputWarning("Warning: '" + name + "' TeamCity user was not notified because the '" + sRunningBuild.getBuildType().getProjectName() + "' BuildProject not found.");
                } else if (notifyResult == NOTIFY_FAIL_CONNECTION) {
                    outputWarning("Warning: Can't connect to the VersionOne as '" + settings.getV1UserName() + "' user ");
                    outputWarning("Warning: '" + name + "' TeamCity user was not notified because of problem with connection.");
                } else if (notifyResult == NOTIFY_FAIL_DUPLICATE) {
                    outputWarning("Warning: Creating BuildRun in the VersionOne by '" + name + "' user " +
                        "failed because this BuildRun was already created (reference '" + sRunningBuild.getBuildId() + "').  " +
                        "Possible you have 2 users which notify to the same VersionOne instance.");
                }
            }
        }
    }

    /**
     * Adds to the VersionOne BuildRun and ChangesSet.
     *
     * @param status        result of build(passed or failed)
     * @param sRunningBuild build data
     * @param settings      user settings
     * @return  0 - if notification is successful (NOTIFY_SUCCESS),
     *          1 - if was problem with connection (NOTIFY_FAIL_CONNECTION),
     *          2 - if BuildRun was already created (NOTIFY_FAIL_DUPLICATE),
     *          3 - if BuildProject was not found (NOTIFY_FAIL_NO_BUILDPROJECT).
     */
    public int notify(String status, SRunningBuild sRunningBuild, final Settings settings) {
        //cancel notification if connection is not valide
        if (!settings.isConnectionValid()) {
            return NOTIFY_FAIL_CONNECTION;
        }

        String projectName = sRunningBuild.getBuildType().getProjectName();
        String buildName = projectName + " - build." + sRunningBuild.getBuildId();
        BuildProject buildProject = getBuildProject(projectName, settings.getV1Instance());

        if (buildProject != null) {

            if (isNoBuildExist(settings, buildName, sRunningBuild.getBuildId(), buildProject)) {
                List<SVcsModification> changes = sRunningBuild.getChanges(SelectPrevBuildPolicy.SINCE_LAST_BUILD, true);
                BuildRun run = getBuildRun(status, sRunningBuild, buildName, buildProject, changes);
                setChangeSets(run, changes, settings);

                return NOTIFY_SUCCESS;
            } else {
                return NOTIFY_FAIL_DUPLICATE;
            }
        }
        return NOTIFY_FAIL_NO_BUILDPROJECT;
    }

    private boolean isNoBuildExist(Settings settings, String buildName, long buildId, BuildProject buildProject) {
        BuildRunFilter filter = new BuildRunFilter();
        filter.references.add(String.valueOf(buildId));
        filter.name.add(buildName);
        filter.buildProjects.add(buildProject);

        Collection<BuildRun> buildRuns = settings.getV1Instance().get().buildRuns(filter);

        return buildRuns == null || buildRuns.size() == 0;
    }

    @NotNull
    public String getNotificatorType() {
        return TYPE;
    }

    @NotNull
    public String getDisplayName() {
        return PLUGIN_NAME;
    }

    /**
     * Find the first BuildProject where the Reference matches the projectName.
     *
     * @param projectName name if the project to find.
     * @param v1Instance  connection to the VersionOne.
     * @return V1 representation of the project if match; otherwise - null.
     */
    @Nullable
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

        run.setElapsed(sRunningBuild.getElapsedTime() * 1000D);
        run.setReference(String.valueOf(sRunningBuild.getBuildId()));
        run.getSource().setCurrentValue(getSourceName(sRunningBuild.getTriggeredBy()));
        run.getStatus().setCurrentValue(status);

        if (!changes.isEmpty()) {
            run.setDescription(getModificationDescription(changes));
        }
        run.save();

        run.createLink("Build Report", getUrlToTÑ(sRunningBuild), true);
        return run;
    }

    /**
     * Return URL to the current build result.
     *
     * @param sRunningBuild object with data about build
     * @return url to the TeamCity with info about build
     */
    @NotNull
    public String getUrlToTÑ(SRunningBuild sRunningBuild) {

        return weblinks.getRootUrl() + "/"
                + "viewLog.html?buildId=" + sRunningBuild.getBuildId()
                + "&tab=buildResultsDiv&buildTypeId="
                + sRunningBuild.getBuildTypeId();
    }

    /**
     * Gets the buildRun source.
     *
     * @param triggeredBy TriggeredBy instance with information about build start
     * @return V1 source name, "trigger" or "forced"
     */
    private String getSourceName(TriggeredBy triggeredBy) {
        return triggeredBy.isTriggeredByUser() ? "forced" : "trigger";
    }

    /**
     * Evaluates BuildRun description.
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

        if (comments.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder(comments.size() * 64);
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
     * Resolve a check-in comment identifier to a PrimaryWorkitem. if the reference matches a SecondaryWorkitem, we need
     * to navigate to the parent.
     *
     * @param reference The identifier in the check-in comment.
     * @param settings  settings for user.
     * @return A collection of matching PrimaryWorkitems.
     */
    public List<PrimaryWorkitem> resolveReference(String reference, Settings settings) {
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
     * Return list of tasks got from the comment string.
     *
     * @param comment         string with some text with ids of tasks which cut using pattern set in the
     *                        referenceexpression attribute.
     * @param v1PatternCommit regular expression for comment parse and getting data from it.
     * @return list of cut ids.
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

    private void outputWarning(String message) {
        System.out.println(new Date() + ":" + message);        
    }
}
