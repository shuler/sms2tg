# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.shuler.sms2tg.**$$serializer { *; }
-keepclassmembers class com.shuler.sms2tg.** {
    *** Companion;
}
-keepclasseswithmembers class com.shuler.sms2tg.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Tink (via androidx.security:security-crypto) references compile-only
# errorprone annotations that aren't on the runtime classpath.
-dontwarn com.google.errorprone.annotations.**

# WorkManager: workers are instantiated reflectively by name.
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
