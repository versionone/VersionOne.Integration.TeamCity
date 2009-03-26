<%@include file="/include.jsp"%>
<jsp:useBean id="settingsBean" scope="request" type="com.versionone.integration.teamcity.SettingsBean"/>

<h2>VersionOne Integration Settings</h2>
<p>
  VersionOne server URL: <strong><c:out value="${settingsBean.url}"/></strong>
</p>
<p>
  Server user: <strong><c:out value="${settingsBean.userName}"/></strong>
</p>
<%--
<p>
  <a href="<c:url value='/versionone/notificatorSettings.html?init=1'/>">Edit settings &raquo;</a>
</p>
--%>

<br clear="all"/>
