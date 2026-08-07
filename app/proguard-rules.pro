-keepattributes Signature
-keepclassmembers,allowobfuscation class * { @com.google.gson.annotations.SerializedName <fields>; }

# Gson models
-keep class com.meteo.app.domain.** { *; }
-keep class com.meteo.app.data.api.** { *; }

# Keep sealed classes for UI state
-keep class com.meteo.app.ui.WeatherUiState { *; }
-keep class com.meteo.app.ui.WeatherUiState$** { *; }
-keep class com.meteo.app.ui.DayHourlyState { *; }
-keep class com.meteo.app.ui.DayHourlyState$** { *; }

# Gson needs to keep TypeToken
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * extends com.google.gson.reflect.TypeToken
