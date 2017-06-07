package kr.co.tmonet.gdrive.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import kr.co.tmonet.gdrive.R;

/**
 * Created by Jessehj on 05/06/2017.
 */

public class NotoTextView extends TextView {

    private int mFontStyle = 2;

    public NotoTextView(Context context) {
        super(context);
        setFont(context);
    }

    public NotoTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NotoTextView, 0, 0);

        try {
            mFontStyle = a.getInteger(R.styleable.NotoTextView_fontStyle, 2);
        } finally {
            a.recycle();
        }
        setFont(context);
    }

    public NotoTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NotoTextView, 0, 0);

        try {
            mFontStyle = a.getInteger(R.styleable.NotoTextView_fontStyle, 2);
        } finally {
            a.recycle();
        }
        setFont(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NotoTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void setFont(Context context, int fontStyle) {
        String fontName;
        switch (mFontStyle) {
            case 0:
                fontName = context.getString(R.string.noto_demilight);
                break;
            case 1:
                fontName = context.getString(R.string.noto_light);
                break;
            case 2:
                fontName = context.getString(R.string.noto_regular);
                break;
            case 3:
                fontName = context.getString(R.string.noto_medium);
                break;
            case 4:
                fontName = context.getString(R.string.noto_bold);
                break;
            case 5:
                fontName = context.getString(R.string.noto_black);
                break;
            default:
                fontName = context.getString(R.string.noto_regular);
                break;
        }

        setTypeface(Typeface.createFromAsset(context.getAssets(), fontName));
    }

    private void setFont(Context context) {
        setFont(context, mFontStyle);
    }
}

