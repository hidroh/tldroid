# appcompat-v7
-keep public class android.support.v7.widget.ShareActionProvider { *; }
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# project
-dontwarn io.github.hidroh.tldroid.**
-keep class io.github.hidroh.tldroid.** { *; }
-keep interface io.github.hidroh.tldroid.** { *; }
