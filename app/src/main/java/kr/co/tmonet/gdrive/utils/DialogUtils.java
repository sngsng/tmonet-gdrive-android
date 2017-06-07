package kr.co.tmonet.gdrive.utils;

import android.content.Context;
import android.view.View;

import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import kr.co.tmonet.gdrive.R;

/**
 * Created by Jessehj on 07/06/2017.
 */

public class DialogUtils {

    public static final int CHARGE_LIST_DIALOG_MARGIN = 36;

    public static void showDialog(Context context, String msg, final View.OnClickListener actionCallback) {
        showDialog(context, msg, context.getString(R.string.title_retry), false, actionCallback);
    }

    public static void showDialog(Context context, String msg) {
        showDialog(context, msg, context.getString(R.string.title_submit), false, null);
    }

    public static void showDialog(Context context, String msg, String btnText, boolean needCancelButton, final View.OnClickListener positionCallback) {
        final LovelyStandardDialog dialog = new LovelyStandardDialog(context)
                .setTopColorRes(R.color.colorCharcoalGrey)
                .setButtonsColorRes(R.color.colorCharcoalGrey)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(msg);

        if (positionCallback != null) {
            dialog.setPositiveButton(btnText, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    positionCallback.onClick(null);
                }
            });
        }

        if (needCancelButton) {
            dialog.setNegativeButton(R.string.title_cancel, null);
        } else {
            dialog.setCancelable(false);
        }

        dialog.show();
    }

}
