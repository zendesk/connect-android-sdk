-keepnames class io.outbound.sdk.** { *; }

# Okio
-dontwarn okio.**

# Retrofit
-dontwarn retrofit2.Platform**
-dontwarn javax.annotation.**

# Firebase
-dontwarn com.google.firebase.**