
VersionOne = {};

VersionOne.SettingsForm = OO.extend(BS.AbstractPasswordForm, {
  setupEventHandlers : function() {
    var that = this;

    this.setUpdateStateHandlers({
      updateState: function() {
        that.storeInSession();
      },
      saveState: function() {
        that.submitSettings();
      }
    });
  },

  storeInSession : function() {
    $("submitSettings").value = 'storeInSession';

    BS.PasswordFormSaver.save(this, this.formElement().action, BS.StoreInSessionListener);
  },

  submitSettings : function() {
    $("submitSettings").value = 'store';

    this.removeUpdateStateHandlers();

    BS.PasswordFormSaver.save(this, this.formElement().action,
            OO.extend(BS.ErrorsAwareListener, this.createErrorListener()));

    return false;
  },

  createErrorListener: function() {
    var that = this;
    return {
      onEmptyUrlError : function(elem) {
        $("errorUrl").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("url"));
      },

      onInvalidUrlError : function(elem) {
          this.onEmptyUrlError(elem);
      },

      onEmptyUserNameError : function(elem) {
        $("errorUserName").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("userName"));
      },

      onEmptyPasswordError : function(elem) {
        $("errorPassword").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("password"));
      },

      onEmptyReferenceFieldError : function(elem) {
        $("errorReferenceField").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("referenceField"));
      },

      onEmptyPatternError : function(elem) {
        $("errorPattern").innerHTML = elem.firstChild.nodeValue;
        that.highlightErrorField($("pattern"));
      },

      onInvalidPatternError : function(elem) {
          this.onEmptyPatternError(elem);
      },

      onCompleteSave : function(form, responseXML, err) {
        BS.ErrorsAwareListener.onCompleteSave(form, responseXML, err);
        if (!err) {
          BS.XMLResponse.processRedirect(responseXML);
        } else {
          that.setupEventHandlers();
        }
      }
    }
  }
});
