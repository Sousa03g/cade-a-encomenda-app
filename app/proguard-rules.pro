# ============================================================
# Cadê a Encomenda - ProGuard Rules
# ============================================================

# --- Retrofit + OkHttp ---
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# --- Moshi ---
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier interface *
-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
# Keep generated JsonAdapters
-keep class **JsonAdapter { *; }

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Data classes used in API / DB ---
-keep class com.cadeaencomenda.app.data.remote.dto.** { *; }
-keep class com.cadeaencomenda.app.data.local.entity.** { *; }

# --- Hilt ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# --- Google Mobile Ads ---
-keep class com.google.android.gms.ads.** { *; }

# --- Firebase ---
-keep class com.google.firebase.** { *; }

# --- Enums ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Parcelable ---
-keep class * implements android.os.Parcelable { *; }

# --- Kotlin Coroutines ---
-dontwarn kotlinx.coroutines.**
