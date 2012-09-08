/*******************************************************************************
 * Copyright (c) 2012 Rob B.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Rob B - initial API and implementation
 ******************************************************************************/
package com.theultimatelabs.abcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.theultimatelabs.nfcblanket.R;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class NfcAbcsActivity extends Activity implements OnInitListener {

	public final static String TAG = NfcAbcsActivity.class.getName();
	private TextToSpeech mTts;
	private Character mLetter = null;
	private String mWord = null;
	final private int WordResources[] = { R.raw.a, R.raw.b, R.raw.c, R.raw.d };

	@Override
	public void onNewIntent(Intent intent) {
		Log.v(TAG, "New Intent:" + intent.getAction());
	}

	private void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getWord(Character letter) {
		Resources res = getResources();
		// TypedArray words = res.obtainTypedArray(R.array.a);
		/*
		 * Log.v(TAG,String.format("%s",letter.toString()));
		 * Log.v(TAG,String.format("%d",res.getIdentifier(letter.toString(),
		 * "array", this.getPackageName())));
		 * Log.v(TAG,String.format("%d",res.getIdentifier(letter.toString(),
		 * "arrays", this.getPackageName())));
		 * Log.v(TAG,String.format("%d",res.getIdentifier(letter.toString(),
		 * "id", this.getPackageName())));
		 * Log.v(TAG,String.format("%d",res.getIdentifier(letter.toString(),
		 * "values", this.getPackageName())));
		 */

		TypedArray words = res.obtainTypedArray(res.getIdentifier(
				letter.toString(), "array", this.getPackageName()));
		return words.getString(new Random().nextInt(words.length()));
		/*
		 * InputStream ins =
		 * getResources().openRawResource(WordResources[mLetter
		 * .charValue()-'A']); BufferedReader reader = new BufferedReader(new
		 * InputStreamReader(ins)); try { String [] words =
		 * reader.readLine().split(","); return words; } catch (IOException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); }
		 */

	}

	int[] wordLetterIds = { R.id.wordLetter0, R.id.wordLetter1,
			R.id.wordLetter2, R.id.wordLetter3, R.id.wordLetter4,
			R.id.wordLetter5, R.id.wordLetter6, R.id.wordLetter7,
			R.id.wordLetter8 };

	/*
	 * TextView [] mWordLetters = { (TextView)findViewById(R.id.wordLetter0),
	 * (TextView)findViewById(R.id.wordLetter1),
	 * (TextView)findViewById(R.id.wordLetter2),
	 * (TextView)findViewById(R.id.wordLetter3),
	 * (TextView)findViewById(R.id.wordLetter4),
	 * (TextView)findViewById(R.id.wordLetter5),
	 * (TextView)findViewById(R.id.wordLetter6),
	 * (TextView)findViewById(R.id.wordLetter7),
	 * (TextView)findViewById(R.id.wordLetter8)};
	 */

	private void clearWord() {
		Log.v(TAG, String.format("imageview id: %d", R.id.wordImageView));
		for (int id : wordLetterIds) {
			Log.v(TAG, findViewById(id).getClass().getName());
			Log.v(TAG, new Integer(id).toString());
			((TextView) findViewById(id)).setText("");
		}
	}

	private void showWord(String word) {
		Log.v(TAG, word);
		clearWord();
		char[] cWord = word.toCharArray();
		for (int i = 0; i < word.length(); i++) {
			((TextView) findViewById(wordLetterIds[4 - word.length() / 2 + i]))
					.setText(word.substring(i, i + 1));
		}
	}

	private void highlightLetter(int off, int on) {
		((TextView) findViewById(wordLetterIds[4 - mWord.length() / 2 + off]))
				.setTextColor(Color.BLACK);
		((TextView) findViewById(wordLetterIds[4 - mWord.length() / 2 + on]))
				.setTextColor(Color.RED);
	}

	private void presentLetter() {

		Log.v(TAG, "Present letter");
		mWord = getWord(mLetter);

		((TextView) (this.findViewById(R.id.bigLetter))).setText(mLetter
				.toString().toUpperCase());
		((TextView) (this.findViewById(R.id.bigLetter)))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						mTts.speak("big " + mLetter.toString(),
								TextToSpeech.QUEUE_FLUSH, null);
					}
				});
		((TextView) (this.findViewById(R.id.littleLetter))).setText(mLetter
				.toString().toLowerCase());
		((TextView) (this.findViewById(R.id.littleLetter)))
				.setOnClickListener(new OnClickListener() {
					public void onClick(View arg0) {
						mTts.speak("little " + mLetter.toString(),
								TextToSpeech.QUEUE_FLUSH, null);
					}
				});
		showWord(mWord);

		/*
		 * ((TextView)(this.findViewById(R.id.wordTextView))).setOnClickListener(
		 * new OnClickListener() { public void onClick(View arg0) {
		 * mTts.speak(String.format("%s is spelled",mWord),
		 * TextToSpeech.QUEUE_FLUSH, null); for (int i=0; i<mWord.length(); i++)
		 * { mTts.speak(mWord.substring(i, i+1), TextToSpeech.QUEUE_ADD, null);
		 * } } });
		 */

		new RetriveImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				mWord);

		mTts.speak(String.format("big %c little %c, you found the letter %c",
				mLetter, mLetter, mLetter, mLetter), TextToSpeech.QUEUE_FLUSH,
				null);
		mTts.playSilence(100, TextToSpeech.QUEUE_ADD, null);
		mTts.speak(String.format("What starts with %c?", mLetter),
				TextToSpeech.QUEUE_ADD, null);
		mTts.playSilence(400, TextToSpeech.QUEUE_ADD, null);
		mTts.speak(String.format("%s starts with %c!", mWord, mLetter),
				TextToSpeech.QUEUE_ADD, null);
		mTts.speak(String.format("%s is spelled", mWord),
				TextToSpeech.QUEUE_ADD, null);
		new SpellWord().execute(mWord);
		// new
		// SpellWord().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mWord);
		//

	}

	private class SpellWord extends AsyncTask<String, Integer, Void> {

		@Override
		protected Void doInBackground(String... words) {
			String word = words[0];
			while (mTts.isSpeaking())
				;
			for (int i = 0; i < mWord.length(); i++) {
				this.publishProgress(i, Color.RED);
				mTts.speak(mWord.substring(i, i + 1), TextToSpeech.QUEUE_ADD,
						null);
				mTts.playSilence(400, TextToSpeech.QUEUE_ADD, null);
				while (mTts.isSpeaking())
					;
				this.publishProgress(i, Color.BLACK);
			}
			return null;
		}

		protected void onProgressUpdate(Integer... args) {
			int index = args[0];
			int color = args[1];
			((TextView) findViewById(wordLetterIds[4 - mWord.length() / 2
					+ index])).setTextColor(color);
		}

		protected void onPostExecute(Void v) {
			new VoiceCapture().execute(mWord);
			mTts.speak(String.format("Logan can you say %s", mWord),
					TextToSpeech.QUEUE_ADD, null);
		}

	}

	private class VoiceCapture extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... words) {
			String word = words[0];
			Intent recognizeIntent = getRecognizeIntent(word);
			while (mTts.isSpeaking())
				;
			startActivityForResult(recognizeIntent, mLetter);
			return null;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.v(TAG, this.getIntent().getAction());
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {

			Tag tag = (Tag) getIntent().getExtras().get(NfcAdapter.EXTRA_TAG);
			Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(
					NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				NdefRecord[] records = ((NdefMessage) rawMsgs[0]).getRecords();
				Log.v(TAG, new String(records[0].getPayload()));
				if (records != null && records[0].getPayload() != null) {
					mLetter = new Character((char) records[0].getPayload()[0]);
					Log.v(TAG, mLetter.toString());
					mTts = new TextToSpeech(this, this);
					// mTts.setPitch(1.0f);
				} else {
					Log.e(TAG, "ERROR: missing record");
				}
			} else {
				Log.e(TAG, "ERROR: missing ndef messages");
			}
		} else {
			mLetter = 'B';
			Log.v(TAG, mLetter.toString());
			mTts = new TextToSpeech(this, this);
		}

	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			int result = mTts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.w(TAG, "Language is not available.");
				// Lanuage data is missing or the language is not supported,
				// fallback to US english
				mTts.setLanguage(Locale.US);

			}

			Log.v(TAG, "TTS ready");

		} else {
			// Initialization failed.
			Log.e(TAG, "ERROR: Could not initialize TextToSpeech!");
		}

		presentLetter();

	}

	public Intent getRecognizeIntent(String word)// , int maxResultsToReturn)
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		// intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,
		// maxResultsToReturn);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				String.format("Say %s", word));
		intent.putExtra("word", word);
		return intent;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "GOT SPEECH RESULT " + resultCode + " req: " + requestCode);

		if (resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			boolean matchFound = false;
			for (String match : matches) {
				if (match.contains(mWord)) {
					mTts.speak(
							String.format("Good Job Logan! You said %s", mWord),
							TextToSpeech.QUEUE_ADD, null);
					new RetriveYoutube().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, mWord);
					mTts.speak(String.format("Here's a video about %s", mWord),
							TextToSpeech.QUEUE_ADD, null);
					matchFound = true;
					break;
				}
			}
			if (!matchFound) {
				mTts.speak(String.format(
						"I heard you say %s, you were supposed to say %s",
						matches.get(0), mWord), TextToSpeech.QUEUE_ADD, null);
				// new
				// RetriveYoutube().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mWord);
			}

			for (String match : matches) {
				Log.d(TAG, match);
			}
			Log.v(TAG, "word:" + data.getStringExtra("word"));
		}

		// startActivity(new Intent(Intent.ACTION_VIEW,
		// Uri.parse("http://www.youtube.com/watch?v=2qBgMmRMpOo")));
	}

	class DescribeLetter extends AsyncTask<String, Void, Void> {
		protected Void doInBackground(String... words) {
			Log.v(TAG, "descibe letter");
			String word = words[0];
			mTts.speak("hello", TextToSpeech.QUEUE_FLUSH, null);
			mTts.speak(mLetter.toString(), TextToSpeech.QUEUE_FLUSH, null);
			mTts.speak(String.format("%s starts with %c", word, mLetter),
					TextToSpeech.QUEUE_ADD, null);
			return null;
		}
	}

	class RetriveImage extends AsyncTask<String, Void, Bitmap> {

		final static String TAG = "RetriveImage";

		protected Bitmap doInBackground(String... words) {

			String word = words[0];

			Log.v(TAG, String.format("getting image for %s", word));

			URL url = null;
			try {
				url = new URL(
						String.format(
								"https://ajax.googleapis.com/ajax/services/search/images?v=1.0&safe=active&imgtype=clipart&q=%s",
								word));

			} catch (MalformedURLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			URLConnection connection = null;
			try {
				connection = url.openConnection();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			connection.addRequestProperty("Referer", "theultimatelabs.com");

			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			JSONObject json = null;
			try {
				json = new JSONObject(builder.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			JSONArray results = null;
			JSONObject result = null;
			try {
				results = json.getJSONObject("responseData").getJSONArray(
						"results");
				result = results.getJSONObject(new Random().nextInt(Math.min(5,
						results.length())));
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String imageUrl = result.optString("url");

			try {

				InputStream imageStream = (InputStream) new URL(imageUrl)
						.getContent();
				if (imageStream != null) {
					Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
					return bitmap;
				}

			} catch (MalformedURLException e) {
				Log.e(TAG, "Malformed URL");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "IO Exception");
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(Bitmap bitmap) {
			ImageView wordImage = (ImageView) findViewById(R.id.wordImageView);
			wordImage.setImageBitmap(bitmap);
			wordImage.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					presentLetter();
				}
			});
		}
	}

	class RetriveYoutube extends AsyncTask<String, Void, String> {

		final static String TAG = "RetriveYoutube";

		protected String doInBackground(String... words) {

			String word = words[0];

			Log.v(TAG, String.format("getting video for %s", word));

			URL url = null;
			try {
				// test:
				// https://gdata.youtube.com/feeds/api/videos?v=2&q=hot&safeSearch=strict
				url = new URL(
						String.format(
								"https://gdata.youtube.com/feeds/api/videos?v=2&alt=json&safeSearch=strict&category=kids&q=%s",
								word));
			} catch (MalformedURLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			URLConnection connection = null;
			try {
				connection = url.openConnection();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			connection.addRequestProperty("Referer", "theultimatelabs.com");

			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			JSONObject json = null;
			try {
				json = new JSONObject(builder.toString());
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String videoUrl = null;

			try {

				JSONObject feed = json.getJSONObject("feed");

				JSONArray entries = feed.getJSONArray("entry");

				JSONObject entry = entries.optJSONObject(new Random()
						.nextInt(Math.min(3, entries.length())));

				/*
				 * JSONObject jsonId = entry.getJSONObject("id");
				 * Log.v(TAG,"jsonId:"+jsonId.toString());
				 * 
				 * String id = jsonId.getString("$t"); Log.v(TAG,"id:"+id);
				 */

				JSONObject content = entry.getJSONObject("content");
				// Log.v(TAG,"content:"+content.toString());

				String src = content.getString("src");
				// Log.v(TAG,"src:"+src.toString());
				videoUrl = src;

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Log.i(TAG, videoUrl);

			return videoUrl;

		}

		protected void onPostExecute(String videoUrl) {
			Log.v(TAG, String.format("watching %s", videoUrl));
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)));
		}
	}

}
