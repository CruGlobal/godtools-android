snuffy-android
==============

Android project that produces snuffy based apps (eg GodTools)

#### Notification Types
1 - app not used for 2 weeks
2 - after 1 presentation of 4SL/KGP
3 - after 10 presentation of 4SL/KGP
4 - 24 hours after a share longer than 1.5 minutes (4SL/KGP)
5 - app not opened after downloading (no longer used)
6 - After 3 uses longer than 1.5 minutes

#### Gradle
Gradle is now used to handle API base URLs. While being developed, a "debug" version of the APK will be created, which will cause the stage API URL to be used. Once the application is released, a "release" version of the APK is created, which will then use the production version of the API URL. Both API URLs can be found in the build.gradle file.