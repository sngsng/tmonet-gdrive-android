package kr.co.tmonet.gdrive.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import kr.co.tmonet.gdrive.R;

/**
 * Created by Jessehj on 05/06/2017.
 */

public class NotoButton extends Button {

    private int mFontStyle = 2;

    public NotoButton(Context context) {
        super(context);
        setFont(context);
    }

    public NotoButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NotoButton, 0, 0);

        try {
            mFontStyle = a.getInteger(R.styleable.NotoButton_btnFontStyle, 2);
        } finally {
            a.recycle();
        }
        setFont(context);
    }

    public NotoButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NotoButton, 0, 0);

        try {
            mFontStyle = a.getInteger(R.styleable.NotoButton_btnFontStyle, 2);
        } finally {
            a.recycle();
        }
        setFont(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NotoButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
