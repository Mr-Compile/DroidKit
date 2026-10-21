# DroidKit release rules — keep this file minimal.
# Hilt, Room, Compose and AndroidX ship their own consumer rules;
# the app uses no reflection-based serialization (HttpURLConnection +
# manual JSON, no Gson/Moshi/kotlinx-serialization on app classes).

# Room entities/DAOs are kept by Room's own consumer ProGuard rules.
# ElevatedAction/Tool/UiProblem are constructed directly — no keeps needed.

# kotlinx-serialization arrives transitively via DataStore-proto;
# keep its generated serializers if it ever gets used on our classes.
-keepclassmembers class **$Companion { *; }
-if @kotlinx.serialization.Serializable class **
-keep class <1>$Companion { *; }


