package io.github.hidroh.tldroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.v4.content.ContextCompat;

public class Utils {
    private static final String FORMAT_HTML_COLOR = "%06X";

    public static String wrapHtml(Context context, String html) {
        return context.getString(R.string.command_html,
                html,
                toHtmlColor(context, android.R.attr.textColorPrimary),
                toHtmlColor(context, android.R.attr.textColorLink),
                toHtmlPx(context, R.dimen.activity_horizontal_margin));
    }

    @IdRes
    public static int getIdRes(Context context, @AttrRes int attrRes) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attrRes});
        int resId = ta.getResourceId(0, 0);
        ta.recycle();
        return resId;
    }

    private static String toHtmlColor(Context context, @AttrRes int colorAttr) {
        return String.format(FORMAT_HTML_COLOR, 0xFFFFFF &
                ContextCompat.getColor(context, getIdRes(context, colorAttr)));
    }

    private static float toHtmlPx(Context context, @DimenRes int dimenAttr) {
        return context.getResources().getDimension(dimenAttr) /
                context.getResources().getDisplayMetrics().density;
    }
}
