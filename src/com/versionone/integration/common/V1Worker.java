/*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.common;

import com.versionone.DB;
import com.versionone.integration.common.BuildInfo;
import com.versionone.integration.teamcity.Settings;
import com.versionone.om.BuildProject;
import com.versionone.om.BuildRun;
import com.versionone.om.ChangeSet;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.SecondaryWorkitem;
import com.versionone.om.Workitem;
import com.versionone.om.filters.BuildProjectFilter;
import com.versionone.om.filters.BuildRunFilter;
import com.versionone.om.filters.ChangeSetFilter;
import com.versionone.om.filters.WorkitemFilter;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.web.openapi.PluginException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class V1Worker {

    //Statuses of notify
    public final static int NOTIFY_SUCCESS = 0;
    public final static int NOTIFY_FAIL_CONNECTION = 1;
    public final static int NOTIFY_FAIL_DUPLICATE = 2;
    public final static int NOTIFY_FAIL_NO_BUILDPROJECT = 3;

    private final Settings settings;

    public V1Worker() {
        this.settings = new Settings();
    }

    /**
     * Adds to the VersionOne BuildRun and ChangesSet.
     *
     */
//    public int submitBuildRun(String projectName, long buildId, boolean successfull, List<SVcsModification> changes) {
    public int submitBuildRun(BuildInfo info) {
        //cancel notification if connection is not valide
        if (!settings.isConnectionValid()) {
            return NOTIFY_FAIL_CONNECTION;
        }

        BuildProject buildProject = getBuildProject(info);

        if (buildProject != null) {
            if (isNoBuildExist(buildProject,info)) {
                BuildRun buildRun = createBuildRun(buildProject, info);
                setChangeSets(buildRun, info);
                return NOTIFY_SUCCESS;
            } else {
                return NOTIFY_FAIL_DUPLICATE;
            }
        }
        return NOTIFY_FAIL_NO_BUILDPROJECT;
    }

    private static String getBuildName(BuildInfo info) {
        String buildName;
        buildName = info.getProjectName() + " - build." + info.getBuildName();
        return buildName;
    }

    private boolean isNoBuildExist(BuildProject buildProject, BuildInfo info) {
        BuildRunFilter filter = new BuildRunFilter();
        filter.references.add(Long.toString(info.getBuildId()));
        filter.name.add(getBuildName(info));
        filter.buildProjects.add(buildProject);

        Collection<BuildRun> buildRuns = settings.getV1Instance().get().buildRuns(filter);

        return buildRuns == null || buildRuns.size() == 0;
    }

    /**
     * Find the first BuildProject where the Reference matches the projectName.
     *
     * @return V1 representation of the project if match; otherwise - null.
     */
    @Nullable
    private BuildProject getBuildProject(BuildInfo info) {
        BuildProjectFilter filter = new BuildProjectFilter();

        filter.references.add(info.getProjectName());
        Collection<BuildProject> projects = settings.getV1Instance().get().buildProjects(filter);
        if (projects.isEmpty()) {
            return null;
        }
        return projects.iterator().next();
    }


    private static BuildRun createBuildRun(BuildProject buildProject, BuildInfo info) {
        // Generate the BuildRun instance to be saved to the recipient
        BuildRun run = buildProject.createBuildRun(getBuildName(info), new DB.DateTime(info.getStartTime()));

        run.setElapsed((double)info.getElapsedTime());
        run.setReference(Long.toString(info.getBuildId()));
        run.getSource().setCurrentValue(getSourceName(info.isTriggered()));
        run.getStatus().setCurrentValue(getStatusName(info.isSuccessful()));

        if (!info.getChanges().isEmpty()) {
            run.setDescription(getModificationDescription(info.getChanges()));
        }
        run.save();

        run.createLink("Build Report", info.getUrl(), true);
        return run;
    }

    /**
     * Returns the V1 BuildRun source name.
     *
     * @param isTriggered true - if build is triggered.
     * @return V1 source name, "trigger" or "forced".
     */
    private static String getSourceName(boolean isTriggered) {
        return isTriggered ? "trigger" : "forced";
    }

    /**
     * Returns the V1 BuildRun status name.
     *
     * @param isSuccessful true - if build is successful.
     * @return V1 source name, "trigger" or "forced".
     */
    private static String getStatusName(boolean isSuccessful) {
        return isSuccessful ? "passed" : "failed";
    }

    /**
     * Evaluates BuildRun description.
     *
     * @param changes - set of changes affected by this BuildRun.
     * @return description string.
     */
    public static String getModificationDescription(List<SVcsModification> changes) {
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

    private void setChangeSets(BuildRun buildRun, BuildInfo info) {
        for (SVcsModification change : info.getChanges()) {
            // See if we have this ChangeSet in the system.
            ChangeSetFilter filter = new ChangeSetFilter();
            String id = Long.toString(change.getId());

            filter.reference.add(id);
            Collection<ChangeSet> changeSets = settings.getV1Instance().get().changeSets(filter);
            if (changeSets.isEmpty()) {
                // We don't have one yet. Create one.
                String name = '\''+change.getUserName() + "\' on \'" + new DB.DateTime(change.getVcsDate())+'\'';
                ChangeSet changeSet = settings.getV1Instance().create().changeSet(name, id);
                changeSets = new ArrayList<ChangeSet>(1);
                changeSets.add(changeSet);
            }

            Set<PrimaryWorkitem> workitems = determineWorkitems(change.getDescription());
            associateWithBuildRun(buildRun, changeSets, workitems);
        }
    }

    private static void associateWithBuildRun(BuildRun buildRun, Collection<ChangeSet> changeSets,
                                       Set<PrimaryWorkitem> workitems) {
        //TODO Associate every changeSet with its own Workitems
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

    private Set<PrimaryWorkitem> determineWorkitems(String comment) {
        List<String> ids = getTasksIds(comment, settings.getPattern());
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
    public static List<PrimaryWorkitem> resolveReference(String reference, Settings settings) {
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
    public static List<String> getTasksIds(String comment, Pattern v1PatternCommit) {
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
