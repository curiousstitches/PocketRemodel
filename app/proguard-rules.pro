# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.pocketremodel.app.**$$serializer { *; }
-keepclassmembers class com.pocketremodel.app.** {
    *** Companion;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# SceneView / Filament native bindings
-keep class com.google.android.filament.** { *; }
-keep class io.github.sceneview.** { *; }
-keep class com.google.ar.** { *; }
