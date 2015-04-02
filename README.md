## Installation

1. Put TeamCityNotificator.zip into <TeamCity Data Directory>\plugins folder.  
2. Restart the server.

TeamCity Data Directory>: <system disc>\Documents and Settings\<Name>\.BuildServer\

## Settings
1. Create project in the TeamCity.  
2. Enable the "Build Integration" option in the Version One. (Admin->Configuration->System->Build Integration)  
3. Create Build Project. Reference of new build project has to named as project in TeamCity.  
4. Create project in the Version One and assign created build project to this project.  
5. Create user (or use existing user) and login by this user to the Team City.  
6. Go to Administration | Server Configuration page.  
7. Press "edit" in the "VersionOne Integration Settings" section.  
8. Change settings and press the "Save" button.  

## Configurations
* Server URL:			The url to VersionOne server  
* Server user:		User name for VersionOne  
* Server password:	Password for VersionOne  
* Reference field:	The system name of an attribute to search when matching primary workitems (stories and defects) with change comments. Required when using changeset integration.  
* Pattern: 	        The regular expression to use when matching primary workitems (stories and defects) with change comments.   * * Required when using changeset integration.  String  (varies)  

If the "Pattern" filed is filled than the "Reference field" will has to be filled to (and vice-versa)

## Want to contribute?
If you are interested in contributing to this project, please contact [VersionOne openAgile Team](mailto:openAgileSupport@versionone.com).
