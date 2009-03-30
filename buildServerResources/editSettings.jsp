<%@ include file="/include.jsp" %>
<%@ taglib prefix="admin" tagdir="/WEB-INF/tags/admin" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<jsp:useBean id="settingsBean" scope="request" type="com.versionone.integration.teamcity.SettingsBean"/>
<c:set var="title" value="Edit VersionOne Notifier Settings" scope="request"/>
<bs:page>
<jsp:attribute name="head_include">
  <style type="text/css">
    @import "<c:url value="/css/forms.css"/>";
    @import "<c:url value='/css/admin/adminMain.css'/>";
    @import "<c:url value='/css/admin/serverConfig.css'/>";
    @import "<c:url value='/plugins/${settingsBean.PLUGIN_NAME}/css/v1Settings.css'/>";
  </style>
  <bs:linkScript>
    /js/crypt/rsa.js
    /js/crypt/jsbn.js
    /js/crypt/prng4.js
    /js/crypt/rng.js
    /js/bs/forms.js
    /js/bs/modalDialog.js
    /js/bs/testConnection.js
    /js/bs/encrypt.js
    /plugins/${settingsBean.PLUGIN_NAME}/js/editSettings.js
  </bs:linkScript>
  <script type="text/javascript">
    BS.Navigation.items = [
    {title: "Administration", url: '<c:url value="/admin/admin.html"/>'},
    {title: "Server Configuration", url: '<c:url value="/admin/serverConfig.html"/>'},
    {title: "${title}", selected:true}
      ];

    Behaviour.addLoadEvent(function() {
      VersionOne.SettingsForm.setupEventHandlers();
      $('url').focus();
    });
  </script>
</jsp:attribute>

<jsp:attribute name="body_include">
  <div id="container">
    <form action="<c:url value='${settingsBean.EDIT_URL}?edit=1'/>" method="post" onsubmit="return VersionOne.SettingsForm.submitSettings()" autocomplete="off">
    <div class="editNotificatorSettingsPage">

      <bs:messages key="settingsSaved"/>
    <table class="runnerFormTable">
      <tr>
      <th><label for="url">Server URL: <l:star/></label></th>
      <td><forms:textField name="url" value="${settingsBean.url}"/>
        <span class="error" id="errorUrl"></span></td>
      </tr>

      <tr>
      <th><label for="userName">Server user: <l:star/></label></th>
      <td><forms:textField name="userName" value="${settingsBean.userName}"/>
        <span class="error" id="errorUserName"></span></td>
      </tr>

      <tr>
      <th><label for="password">Server user password: <l:star/></label></th>
      <td><forms:passwordField name="password" encryptedPassword="${settingsBean.encryptedPassword}"/>
        <span class="error" id="errorPassword"></span></td>
      </tr>

      <tr>
      <th><label for="referenceField">Reference field: <l:star/></label></th>
      <td><forms:textField name="referenceField" value="${settingsBean.referenceField}"/>
        <span class="error" id="errorReferenceField"></span></td>
      </tr>

      <tr>
      <th><label for="pattern">Pattern: <l:star/></label></th>
      <td><forms:textField name="pattern" value="${settingsBean.pattern}"/>
        <span class="error" id="errorPattern"></span></td>
      </tr>
    </table>

      <div class="saveButtonsBlock">
        <a showdiscardchangesmessage='false' class="cancel" href="<c:url value='/admin/serverConfig.html'/>">Cancel</a>
        <input class="submitButton" type="submit" value="Save">
        <%--<input class="submitButton" id="testConnection" type="button" value="Test connection"/>--%>
        <input type="hidden" id="submitSettings" name="submitSettings" value="store"/>
        <input type="hidden" id="testAddress" name="testAddress" value=""/>
        <input type="hidden" id="publicKey" name="publicKey" value="<c:out value='${settingsBean.hexEncodedPublicKey}'/>"/>
        <forms:saving/>
      </div>

    </div>
    </form>
  </div>

  <bs:dialog dialogId="testConnectionDialog" title="Test Connection" closeCommand="BS.TestConnectionDialog.close();"
    closeAttrs="showdiscardchangesmessage='false'">
    <div id="testConnectionStatus"></div>
    <div id="testConnectionDetails"></div>
  </bs:dialog>

  <forms:modified/>
</jsp:attribute>

</bs:page>


