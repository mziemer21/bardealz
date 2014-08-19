package activities;

import navigation.NavDrawer;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bardealz.ParseApplication;
import com.bardealz.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.parse.ParseUser;

/***
 * empty main page that loads the nav drawer and home fragment
 * 
 * @author zieme_000
 * 
 */
public class FeedbackActivity extends NavDrawer {

	private Button send;
	private EditText subject, message;
	private String subjectS, messageS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_feedback);
		super.onCreate(savedInstanceState);
		
		// Get tracker.
		((ParseApplication) getApplication()).getTracker(ParseApplication.TrackerName.APP_TRACKER);

		send = (Button) findViewById(R.id.send_button);

		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				subject = (EditText) findViewById(R.id.email_subject);
				message = (EditText) findViewById(R.id.email_message);

				subjectS = subject.getText().toString();
				messageS = message.getText().toString();
				sendEmail();
			}
		});

	}

	protected void sendEmail() {

		String[] TO = { "contact.the.bar.app@gmail.com" };
		String[] CC = {};
		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setData(Uri.parse("mailto:"));
		emailIntent.setType("text/plain");

		emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
		emailIntent.putExtra(Intent.EXTRA_CC, CC);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subjectS + " " + ParseUser.getCurrentUser().getObjectId());
		emailIntent.putExtra(Intent.EXTRA_TEXT, messageS);

		try {
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			finish();
			Log.i("Finished sending email...", "");
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(FeedbackActivity.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
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
