# Keep Firebase model constructors and fields used by Firestore reflection.
-keepclassmembers class com.example.saferide.models.** {
    public <init>();
    public *;
}
