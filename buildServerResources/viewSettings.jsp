<%@ page import="com.versionone.integration.teamcity.V1SettingsController" %>
<%@include file="/include.jsp"%>
<jsp:useBean id="settingsBean" scope="request" type="com.versionone.integration.teamcity.SettingsBean"/>
<h2>VersionOne Integration Settings</h2>
<p>
  Server URL: <strong><c:out value="${settingsBean.url}"/></strong>
</p>
<p>
  User name: <strong><c:out value="${settingsBean.userName}"/></strong>
</p>
<p>
  Reference field: <strong><c:out value="${settingsBean.referenceField}"/></strong>
</p>
<p>
  Pattern: <strong><c:out value="${settingsBean.pattern}"/></strong>
</p>
<% String editUrl = V1SettingsController.EDIT_SETTINGS_URL + "?init=1"; %>
<p>
  <a href="<c:url value='<%=editUrl%>'/>">Edit settings &raquo;</a>
</p>
<br clear="all"/>
