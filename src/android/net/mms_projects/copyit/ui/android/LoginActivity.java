package net.mms_projects.copyit.ui.android;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import net.mms_projects.copyit.LoginResponse;
import net.mms_projects.copyit.R;
import net.mms_projects.copyit.api.ServerApi;
import net.mms_projects.copyit.api.endpoints.DeviceEndpoint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends Activity {

	private static final int ACTIVITY_LOGIN = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// Show the Up button in the action bar.
		setupActionBar();

		Intent intent = new Intent(this, BrowserLoginActivity.class);
		startActivityForResult(intent, LoginActivity.ACTIVITY_LOGIN);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void openBrowserLogin(View view) {
		Intent intent = new Intent(this, BrowserLoginActivity.class);
		startActivityForResult(intent, LoginActivity.ACTIVITY_LOGIN);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case LoginActivity.ACTIVITY_LOGIN:
			if (resultCode == RESULT_OK) {
				LoginResponse response = new LoginResponse();
				response.deviceId = UUID.fromString(data
						.getStringExtra("device_id"));
				response.devicePassword = data
						.getStringExtra("device_password");

				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(this);

				try {
					Editor preferenceEditor = preferences.edit();
					preferenceEditor.putString("device.id",
							response.deviceId.toString());
					preferenceEditor.putString("device.password",
							response.devicePassword);
					preferenceEditor.commit();
				} catch (Exception event) {
					// TODO Auto-generated catch block
					event.printStackTrace();
				}

				ServerApi api = new ServerApi();
				api.deviceId = response.deviceId;
				api.devicePassword = response.devicePassword;
				api.apiUrl = preferences.getString("server.baseurl", this
						.getResources().getString(R.string.default_baseurl));

				LoginTask task = new LoginTask(api);
				task.execute();
				break;
			}
		}
	}

	private class LoginTask extends ServerApiTask<Void, Void, Boolean> {

		private Exception exception;
		private ProgressDialog progress;

		public LoginTask(ServerApi api) {
			super(api);
		}

		@Override
		protected void onPreExecute() {
			this.progress = ProgressDialog.show(
					LoginActivity.this,
					LoginActivity.this.getResources().getString(
							R.string.dialog_title_busy),
					LoginActivity.this.getResources().getString(
							R.string.text_logging_in), true);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				InetAddress addr = InetAddress.getLocalHost();
				String hostname = addr.getHostName();

				return new DeviceEndpoint(api).create(hostname);
			} catch (UnknownHostException event) {
				// TODO Auto-generated catch block
				event.printStackTrace();
			} catch (Exception event) {
				// TODO Auto-generated catch block
				event.printStackTrace();
			}

			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!result) {
				AlertDialog alertDialog = new AlertDialog.Builder(
						LoginActivity.this).create();
				alertDialog.setTitle(LoginActivity.this.getResources()
						.getString(R.string.dialog_title_error));
				alertDialog.setMessage(LoginActivity.this.getResources()
						.getString(R.string.error_device_setup_failed,
								this.exception.getMessage()));
				alertDialog.setButton(
						DialogInterface.BUTTON_POSITIVE,
						LoginActivity.this.getResources().getString(
								R.string.dialog_button_okay),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
				alertDialog.setIcon(R.drawable.ic_launcher);
				alertDialog.show();
				return;
			}
			Toast.makeText(
					LoginActivity.this,
					LoginActivity.this.getResources().getString(
							R.string.text_login_successful), Toast.LENGTH_SHORT)
					.show();

			this.progress.dismiss();

			LoginActivity.this.finish();
		}
	}

}
