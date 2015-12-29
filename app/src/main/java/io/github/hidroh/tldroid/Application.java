package io.github.hidroh.tldroid;

import android.graphics.Typeface;

public class Application extends android.app.Application {
    public static Typeface MONOSPACE_TYPEFACE;

    @Override
    public void onCreate() {
        super.onCreate();
        MONOSPACE_TYPEFACE = Typeface.createFromAsset(getAssets(), "RobotoMono-Regular.ttf");
    }
}
