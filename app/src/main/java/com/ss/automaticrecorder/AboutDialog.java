package com.ss.automaticrecorder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.method.LinkMovementMethod;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;




public class AboutDialog {
	public static void show(final Context context) {

		ContextThemeWrapper wrapper = new ContextThemeWrapper(context, R.style.DialogBaseTheme);
		View view = LayoutInflater.from(wrapper).inflate(R.layout.dialog_about, null);

		TextView tv = (TextView) view.findViewById(R.id.textview1);
		tv.setText(HtmlTagHandler.fromHtml(context.getResources().getString(R.string.about_app)));
		tv.setMovementMethod(LinkMovementMethod.getInstance());

		tv.setOnClickListener(new OnClickListener() {
			AtomicInteger easteregg_count = new AtomicInteger(0);

			@Override
			public void onClick(View v) {
				if(easteregg_count.get()==-1)return; // already shown...
				if (easteregg_count.getAndIncrement() > 6) {
					easteregg_count.set(-1);
					ContextThemeWrapper wrapper = new ContextThemeWrapper(context, R.style.DialogBaseTheme);
					View view = LayoutInflater.from(wrapper).inflate(R.layout.dialog_about_me, null);

					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("About Me");
					builder.setIcon(android.R.drawable.ic_dialog_info);
					builder.setView(view);

					builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}

					});

					builder.create().show();
				}

			}

		});

		tv = (TextView) view.findViewById(R.id.textview2);
		String str = context.getResources().getString(R.string.about_version);

		tv.setText(str + " " + PackageUtils.getVersionName(context) + " - " + PackageUtils.getVersionNumber(context));

		str = context.getResources().getString(R.string.about_model);
		if (!str.contentEquals("OVERRIDE_FOR_MODEL")) {
			tv = (TextView) view.findViewById(R.id.textview3);
			String str2 = android.os.Build.MANUFACTURER + "-" + android.os.Build.MODEL;
			tv.setText(str + str2.toUpperCase());
		}
		str = context.getResources().getString(R.string.about_deviceid);
		if (!str.contentEquals("OVERRIDE_FOR_DEVICE_ID")) {
			String androidDeviceId = android.provider.Settings.Secure.getString(context.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
			tv = (TextView) view.findViewById(R.id.textview4);
			tv.setText(str + "android-" + androidDeviceId);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);
		builder.setTitle(context.getResources().getString(R.string.about_title));
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setView(view);
		builder.setCancelable(true);
		
		String close = context.getString(android.R.string.ok);
		
		builder.setPositiveButton(close, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}

		});		
		AlertDialog dlg = builder.create();
		dlg.setCanceledOnTouchOutside(true);
		dlg.show();
	}
}
