package com.wizarpos.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.cloudpos.pinpad.newui.R;


/**
 * Created by pengli on 17-2-17.
 */

public class AlertDialogUtils {

	public static void showWarningDialog(Activity mActivity, String title, String content, DialogInterface.OnClickListener listener) {
		showDialog(mActivity, android.R.drawable.stat_notify_error, title, content, listener);
	}

	public static void showDialog(Activity mActivity, int iconID, String title, String content, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(title).setIcon(iconID)
				.setMessage(content)
				.setPositiveButton(R.string.confirm, listener);
		builder.create().show();
	}

	public static void showSuccessDialog(final Activity mActivity, String title, String content) {
		showDialog(mActivity, android.R.drawable.btn_star, title, content, new DialogInterface.OnClickListener() {// 积极

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
	}


	public static void showWarningDialog(Activity mActivity, String title, String content) {
		showWarningDialog(mActivity, title, content, new DialogInterface.OnClickListener() {// 积极

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}

	public static void showWarningDialog(Activity mActivity, String warningMsg) {
		String title = mActivity.getString(R.string.warning_title);
		showWarningDialog(mActivity, title, warningMsg);

	}

	public static void showWarningDialog(Activity mActivity, String warningMsg, DialogInterface.OnClickListener listener) {
		String title = mActivity.getString(R.string.warning_title);
		showWarningDialog(mActivity, title, warningMsg, listener);

	}

}
