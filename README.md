GodTools-Android
==============

The Android native version of the GodTools mobile application.

## Gradle
Gradle is now used to handle API base URLs. While being developed, a "debug" version of the APK will be created, which will cause the stage API URL to be used. Once the application is released, a "release" version of the APK is created, which will then use the production version of the API URL. Both API URLs can be found in the build.gradle file.


## Notifications

Notifications are sent to uses based on several different types of interactions with the app.

#### Notification Types

1 - App not used for 2 weeks <br/>
2 - After 1 presentation of 4SL/KGP <br/>
3 - After 10 presentation of 4SL/KGP <br/>
4 - 24 hours after a share longer than 1.5 minutes (4SL/KGP) <br/>
5 - App not opened after downloading (no longer used) <br/>
6 - After 3 uses longer than 1.5 minutes <br/>