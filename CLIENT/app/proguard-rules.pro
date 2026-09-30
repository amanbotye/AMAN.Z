-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Supabase & Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class * {
    <fields>;
}

# Domain & DTO models
-keep class ye.aman.client.data.remote.dto.** { *; }
-keep class ye.aman.client.domain.model.** { *; }
-keep class ye.aman.client.data.local.entity.** { *; }
