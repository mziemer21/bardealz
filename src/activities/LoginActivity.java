package activities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.bardealz.ParseApplication;
import com.bardealz.R;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

	private Dialog loginProgressDialog;
	private String fName, lName, email, birthday, relationship;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Check if there is a currently logged in user
		// and they are linked to a Facebook account.
		ParseUser currentUser = ParseUser.getCurrentUser();
		if ((currentUser != null) && ParseFacebookUtils.isLinked(currentUser)) {
			// Go to the user info activity
			showNextActivity();
		}
		
		// Get tracker.
		((ParseApplication) getApplication()).getTracker(ParseApplication.TrackerName.APP_TRACKER);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Add code to print out the key hash
		try {
			Log.d("Hash start", "Checking signs");
			PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("Hash reslut", Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.d("Hash error", e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Log.d("Hash error", e.getMessage());
		}
	}

	public void onButtonClick(View v) {

		switch (v.getId()) {

		case R.id.btnLogin:
			onLoginButtonClicked();
			break;

		case R.id.btnSkip:
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(i);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
	}

	@SuppressWarnings("deprecation")
	private void onLoginButtonClicked() {
		if (loginProgressDialog != null) {
			loginProgressDialog.dismiss();
			loginProgressDialog = null;
		}

		loginProgressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...", true);
		List<String> permissions = Arrays.asList("public_profile", "user_about_me", "user_relationships", "user_birthday", "user_location", "email");
		ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
			@Override
			public void done(ParseUser pUser, ParseException err) {

				if (email != null) {
					pUser.put("email", email);
				}
				if (fName != null) {
					pUser.put("firstName", fName);
				}
				if (lName != null) {
					pUser.put("lastName", lName);
				}
				if (birthday != null) {
					pUser.put("birthday", birthday);
				}
				if (relationship != null) {
					pUser.put("relationship", relationship);
				}
				
				pUser.saveInBackground();

				if (loginProgressDialog != null) {
					loginProgressDialog.dismiss();
					loginProgressDialog = null;
				}

				if (pUser == null) {
					Log.d("The Bar App", "Uh oh. The user cancelled the Facebook login.");
				} else if (pUser.isNew()) {
					Log.d("The Bar App", "User signed up and logged in through Facebook!");
					showNextActivity();
				} else {
					Log.d("The Bar App", "User logged in through Facebook!");
					showNextActivity();
				}
				
				com.facebook.Request.executeMeRequestAsync(ParseFacebookUtils.getSession(), new com.facebook.Request.GraphUserCallback() {

					@Override
					public void onCompleted(GraphUser user, Response response) {
						email = user.getProperty("email").toString();
						fName = user.getFirstName();
						lName = user.getLastName();
						birthday = user.getBirthday();
						relationship = user.getProperty("relationship_status").toString();
						
					
					}
				});
			}
		});
	}

	private void showNextActivity() {
		if (isTaskRoot()) {
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(i);
		}
		finish();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (loginProgressDialog != null) {
			loginProgressDialog.dismiss();
			loginProgressDialog = null;
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
}