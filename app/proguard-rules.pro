# Jetpack Compose rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <fields>;
}
-keepclassmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <fields>;
}
-keep public class * implements androidx.compose.runtime.Composer {
    public <init>(...);
    public final <methods>;
}
-keep public class * implements androidx.compose.runtime.Composition {
    public <init>(...);
    public final <methods>;
}
-keep public class * implements androidx.compose.runtime.Recomposer {
    public <init>(...);
    public final <methods>;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class androidx.compose.runtime.R$* {
    public static <fields>;
}
-keepclassmembers class androidx.compose.ui.R$* {
    public static <fields>;
}

# Kotlin Coroutines rules
-dontwarn kotlinx.coroutines.**
-keepclasseswithmembers class kotlinx.coroutines.** {
    <methods>;
}
-keepclassmembers class kotlinx.coroutines.internal.MainDispatcherFactory {
    <fields>;
    <methods>;
}
-keepclassmembers class kotlinx.coroutines.android.AndroidDispatcherFactory {
    <fields>;
    <methods>;
}
-keepclassmembers class kotlinx.coroutines.scheduling.WorkQueue {
    <fields>;
    <methods>;
}

# General Kotlin rules for reflection (often needed for serialization or other libraries)
-keepattributes Signature
-keepattributes InnerClasses
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepnames class kotlin.coroutines.CombinedContext
-keepnames class kotlin.coroutines.EmptyCoroutineContext

# Keep Kotlin data classes and their properties
-keepclassmembers public class * extends kotlin.jvm.internal.Lambda {
    <fields>;
    <init>(...);
}
-keepclassmembers class * extends kotlin.coroutines.jvm.internal.SuspendLambda {
    <fields>;
    <init>(...);
}
-keepclassmembers class * extends kotlin.coroutines.jvm.internal.RestrictedSuspendLambda {
    <fields>;
    <init>(...);
}
-keepclassmembers class * extends kotlin.coroutines.jvm.internal.ContinuationImpl {
    <fields>;
    <init>(...);
}
-keepclassmembers class * extends kotlin.coroutines.jvm.internal.RunSuspend {
    <fields>;
    <init>(...);
}
-keep class **$$serializer { *; }
-keep class **$$Lambda$* { *; }
-keepclassmembers class kotlin.jvm.internal.DefaultConstructorMarker

# Retain annotations for reflection if needed by libraries
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations
-keepattributes AnnotationDefault
