# Gson 2.10.1 uses reflection for these JSON models. Preserve field names
# for the bundled locality catalog, saved preferences and navigation JSON.
# Keep only these models, not the entire app or its dependencies.
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

-keep class com.cfsd.meteocadaaveres.ProvinciaLocalidades { <fields>; }
-keep class com.cfsd.meteocadaaveres.LocalidadWrapper { <fields>; }
-keep class com.cfsd.meteocadaaveres.Localidad { <fields>; }
-keep class com.cfsd.meteocadaaveres.DiaPrediccion { <fields>; }
# DiaPrediccion contains List<Pair<String, String>>, also read by Gson.
-keep class kotlin.Pair { <fields>; }

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
