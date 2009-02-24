Installation:

1 Put TeamCityNotificator.x.x.x.zip into <TeamCity Data Directory>\plugins folder.
2 Restart the server.

<TeamCity Data Directory> by default - <system disc>\Documents and Settings\<Name>\.BuildServer\


Settings:
1 Create project in the TeamCity.
2 Enable the "Build Integration" option in the Version One. (Admin->Configuration->System->Build Integration)
3 Create Build Project. Reference of new build project has to named as project in TeamCity
4 Create project in the Version One and assign created build project to this project.
5 Create user (or use existing user) and login by this user to the Team City.
6 Go to My Setting & Tools page
7 Press "edit" near the "Version One Integration" title.
8 Press "Add new rule".
9 
	a. select created project (from 1pt)
	b. check the "The build fails" and "The build is successful" checkboxes
10 Fill 5 textboxes proper data(*) and press the "Save" button

(*)
V1 url:			- The url to VersionOne server
V1 login:		- User name for VersionOne
V1 password:		- Password for VersionOne
Regexp for comments: 	- The regular expression to use when matching primary workitems (stories and defects) with change comments. Required when using changeset integration.  String  (varies)
Reference field:	- The system name of an attribute to search when matching primary workitems (stories and defects) with change comments. Required when using changeset integration.


If the "Regexp for comments" filed is filled than the "Reference field" will has to be filled to (and vice-versa)

