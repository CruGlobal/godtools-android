# Credential Manager loads its Play Services provider by reflection
# see: https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
