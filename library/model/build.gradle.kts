android {
    // HACK: disable the NullSafeMutableLiveData check because it throws an error until lifecycle 2.4.0-alpha02
    //       relevant bug: https://issuetracker.google.com/issues/183696616
    lint.disable("NullSafeMutableLiveData")
}

dependencies {
    implementation(project(":library:base"))

    implementation(libs.gtoSupport.jsonapi)

    testImplementation(libs.json)
}
