-keep class com.meituan.robust.**{*;}
-keep class meituan.robust.**{*;}
-keep class com.google.gson.**{*;}
-keepattributes *Annotation*
-keepclassmembers class **{
public static com.meituan.robust.ChangeQuickRedirect *;
}

