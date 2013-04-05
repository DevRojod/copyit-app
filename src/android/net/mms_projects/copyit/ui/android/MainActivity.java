package net.mms_projects.copyit.ui.android;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.mms_projects.copyit.FileStreamBuilder;
import net.mms_projects.copyit.R;
import net.mms_projects.copyit.Settings;
import net.mms_projects.copyit.api.ServerApi;
import net.mms_projects.copyit.api.endpoints.ClipboardContentEndpoint;
import net.mms_projects.copyit.app.AndroidApplication;
import net.mms_projects.copyit.app.CopyItAndroid;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	private CopyItAndroid app;
	private Settings settings;

	public void setup(Settings settings, ServerApi api) {
		this.settings = settings;
		AndroidApplication.getInstance().setApi(api);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.app = new CopyItAndroid();
		this.app.run(this, new StreamBuilder(this));

		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void copyIt(View view) {
		try {
			new ClipboardContentEndpoint(AndroidApplication.getInstance()
					.getApi()).update(this.getClipboard());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void pasteIt(View view) {
		try {
			this.setClipboard(new ClipboardContentEndpoint(AndroidApplication
					.getInstance().getApi()).get());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void doLogin(View view) {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

	@SuppressWarnings("deprecation")
	protected void setClipboard(String text) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(text);
		} else {
			this.setClipboardHoneycomb(text);
		}
	}

	@SuppressWarnings("deprecation")
	protected String getClipboard() {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			return clipboard.getText().toString();
		} else {
			return this.getClipboardHoneycomb();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void setClipboardHoneycomb(String text) {
		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		android.content.ClipData clip = android.content.ClipData.newPlainText(
				"text label", text);
		clipboard.setPrimaryClip(clip);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected String getClipboardHoneycomb() {
		android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		android.content.ClipData clip = clipboard.getPrimaryClip();
		return clip.getItemAt(0).getText().toString();
	}

	class StreamBuilder extends FileStreamBuilder {

		private Activity activity;

		public StreamBuilder(Activity activity) {
			this.activity = activity;
		}

		@Override
		public FileInputStream getInputStream() throws IOException {
			return this.activity.openFileInput("settings");
		}

		@Override
		public FileOutputStream getOutputStream() throws IOException {
			return this.activity.openFileOutput("settings",
					Context.MODE_PRIVATE);
		}

	}
}