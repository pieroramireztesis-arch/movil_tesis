# ── Retrofit 2 ─────────────────────────────────────────────────────────────
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# ── OkHttp3 ─────────────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ── Gson: mantener DTOs para que la deserialización JSON funcione ────────────
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── Todos los DTOs del proyecto (model/dto) ──────────────────────────────────
-keep class com.example.aplicacion_tesis.model.** { *; }
-keep class com.example.aplicacion_tesis.network.** { *; }

# ── Glide ────────────────────────────────────────────────────────────────────
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** { *; }

# ── MPAndroidChart ───────────────────────────────────────────────────────────
-keep class com.github.mikephil.charting.** { *; }

# ── Preservar stack traces legibles en producción ────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile