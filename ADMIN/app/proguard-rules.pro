# Proguard rules for AMAN Admin
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-keep class io.github.jan.supabase.** { *; }
-keep class kotlinx.serialization.** { *; }
-keep class ye.aman.admin.data.remote.dto.** { *; }
-keep class ye.aman.admin.data.local.entity.** { *; }
-keep class ye.aman.admin.domain.model.** { *; }
