# compose-otp-field uses no reflection on its own classes.
# rememberSmsRetriever uses Google Play services' SMS User Consent API only when the app adds
# play-services-auth-api-phone itself, so R8 must not fail when those classes are missing.
-dontwarn com.google.android.gms.auth.api.phone.**
-dontwarn com.google.android.gms.common.api.**
-dontwarn com.google.android.gms.tasks.**
# The availability check looks the class up by name.
-keepnames class com.google.android.gms.auth.api.phone.SmsRetriever
