/*(c) Copyright 2012, VersionOne, Inc. All rights reserved. (c)*/
package com.versionone.integration.ciCommon;

import com.versionone.DB;
import com.versionone.integration.teamcity.FileConfig;
import com.versionone.integration.teamcity.V1Connector;
import com.versionone.om.BuildProject;
import com.versionone.om.BuildRun;
import com.versionone.om.ChangeSet;
import com.versionone.om.PrimaryWorkitem;
import com.versionone.om.filters.BuildProjectFilter;
import com.versionone.om.filters.BuildRunFilter;
import com.versionone.om.filters.ChangeSetFilter;
import jetbrains.buildServer.vcs.SVcsModification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private final FileConfig config;
    private V1Connector connector;

    public V1Worker(FileConfig config, V1Connector connector) {
        this.config = config;
        this.connector = connector;
        connector.setConnectionSettings(config);
    }

    /**
     * Adds to the VersionOne BuildRun and ChangesSet.
     */
    public int submitBuildRun(BuildInfo info) {
        //cancel notification if connection is not valid
        if (!connector.isConnectionValid()) {
            return NOTIFY_FAIL_CONNECTION;
        }

        BuildProject buildProject = getBuildProject(info);

        if (buildProject != null) {
            if (isNoBuildExist(buildProject, info)) {
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

        Collection<BuildRun> buildRuns = connector.getV1Instance().get().buildRuns(filter);

        return buildRuns == null || buildRuns.size() == 0;
    }

    /**
     * Find the first BuildProject where the Reference matches the projectName.
     *
     * @return V1 representation of the project if match; otherwise - null.
     */
    private BuildProject getBuildProject(BuildInfo info) {
        BuildProjectFilter filter = new BuildProjectFilter();

        filter.references.add(info.getProjectName());
        Collection<BuildProject> projects = connector.getV1Instance().get().buildProjects(filter);
        if (projects.isEmpty()) {
            return null;
        }
        return projects.iterator().next();
    }


    private static BuildRun createBuildRun(BuildProject buildProject, BuildInfo info) {
        // Generate the BuildRun instance to be saved to the recipient
        BuildRun run = buildProject.createBuildRun(getBuildName(info), new DB.DateTime(info.getStartTime()));

        run.setElapsed((double) info.getElapsedTime());
        run.setReference(Long.toString(info.getBuildId()));
        run.getSource().setCurrentValue(getSourceName(info.isForced()));
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
     * @param isForced true - if build was forced.
     * @return V1 source name, "trigger" or "forced".
     */
    private static String getSourceName(boolean isForced) {
        return isForced ? "forced" : "trigger";
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
            String id = change.getDisplayVersion();

            filter.reference.add(id);
            Collection<ChangeSet> changeSetList = connector.getV1Instance().get().changeSets(filter);
            if (changeSetList.isEmpty()) {
                // We don't have one yet. Create one.
                String name = '\'' + change.getUserName() + "\' on \'" + new DB.DateTime(change.getVcsDate()) + '\'';
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("Description", change.getDescription());
                ChangeSet changeSet = connector.getV1Instance().create().changeSet(name, id, attributes);
                changeSetList = new ArrayList<ChangeSet>(1);
                changeSetList.add(changeSet);
            }

            Set<PrimaryWorkitem> workitems = determineWorkitems(change.getDescription());
            associateWithBuildRun(buildRun, changeSetList, workitems);
        }
    }

    private static void associateWithBuildRun(BuildRun buildRun, Collection<ChangeSet> changeSets,
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

    private Set<PrimaryWorkitem> determineWorkitems(String comment) {
        List<String> ids = getWorkitemsIds(comment, config.getPatternObj());
        Set<PrimaryWorkitem> result = new HashSet<PrimaryWorkitem>(ids.size());

        for (String id : ids) {
            result.addAll(connector.getPrimaryWorkitemsByReference(id));
        }
        return result;
    }

    /**
     * Return list of workitems got from the comment string.
     *
     * @param comment         string with some text with ids of tasks which cut using pattern set in the
     *                        referenceexpression attribute.
     * @param v1PatternCommit regular expression for comment parse and getting data from it.
     * @return list of cut ids.
     */
    public static List<String> getWorkitemsIds(String comment, Pattern v1PatternCommit) {
        final List<String> result = new LinkedList<String>();

        if (v1PatternCommit != null) {
            Matcher m = v1PatternCommit.matcher(comment);
            while (m.find()) {
                result.add(m.group());
            }
        }

        return result;
    }
}
