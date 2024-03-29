GodTools Android
================

[![codecov](https://codecov.io/gh/CruGlobal/godtools-android/branch/develop/graph/badge.svg)](https://codecov.io/gh/CruGlobal/godtools-android)

# Git LFS

We use [Git LFS](https://git-lfs.com/) for storing Paparazzi snapshots. You will need to setup [Git LFS](https://git-lfs.com/) on your local machine in order to store new paparazzi snapshots or validate existing paparazzi snapshots.

# OneSky

To enable OneSky translation downloads/uploads configure the following [gradle properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties):
```
ONESKY_API_KEY={apiKey}
ONESKY_API_SECRET={apiSecret}
```

Once those properties are configured you can use the following commands:
- Download the latest translations: `./gradlew downloadTranslations --no-parallel`
- Upload the latest base strings: `./gradlew uploadTranslations`
