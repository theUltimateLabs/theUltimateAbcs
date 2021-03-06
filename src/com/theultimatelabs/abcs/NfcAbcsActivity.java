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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.theultimatelabs.abcs.R;

public class NfcAbcsActivity extends Activity implements OnInitListener {

	public final static String TAG = NfcAbcsActivity.class.getName();
	protected static final String CHILD_NAME_ENTRY = "CHILD_NAME";
	protected static final String APP_PREFS = "APP_PREFS";
	private TextToSpeech mTts;
	private Character mLetter = null;
	private String mWord = null;
	int[] wordLetterIds = { R.id.wordLetter0, R.id.wordLetter1,
			R.id.wordLetter2, R.id.wordLetter3, R.id.wordLetter4,
			R.id.wordLetter5, R.id.wordLetter6, R.id.wordLetter7,
			R.id.wordLetter8 };
	private NfcAdapter mAdapter;
	private PendingIntent mPendingIntent;
	private IntentFilter[] mFilters;
	private String[][] mTechLists;
	private String mChildName = "Logan";
	

	@Override
	public void onNewIntent(Intent intent) {
		Log.v(TAG, "New Intent:" + intent.getAction());
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			Tag tag = (Tag) intent.getExtras().get(NfcAdapter.EXTRA_TAG);
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
					NfcAdapter.EXTRA_NDEF_MESSAGES);
			Log.v(TAG,"Decoding TAG");
			if (rawMsgs != null) {
				NdefRecord[] records = ((NdefMessage) rawMsgs[0]).getRecords();
				Log.v(TAG, new String(records[0].getPayload()));
				if (records != null && records[0].getPayload() != null) {
					//TODO make sure it has my package name also
					mLetter = new Character((char) records[0].getPayload()[0]);
					Log.v(TAG, mLetter.toString());
					presentLetter(true);					
				} else {
					Log.e(TAG, "ERROR: missing record");
				}
			} else {
				Log.e(TAG, "ERROR: missing ndef messages");
			}
		}
		else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
			Log.v(TAG,"offer to write tag here");
			
		}
		else {
			Log.v(TAG,String.format("Unknown TAG type, expecting %s",NfcAdapter.ACTION_NDEF_DISCOVERED));
		}
	}

    public void writeTag(Tag tag, String message) {
    	
    	Log.v(TAG,"Writing message to card");
    	NdefMessage ndefMessage = null;
    	NdefRecord mimeRecord = new NdefRecord(
    		    NdefRecord.TNF_MIME_MEDIA ,
    		    "application/com.theultimatelabs.nfcblanket".getBytes(Charset.forName("US-ASCII")),
    		    new byte[0], message.getBytes(Charset.forName("US-ASCII")));
		NdefRecord[] records = {mimeRecord};
		ndefMessage = new NdefMessage(records);		
    	
    	Ndef ndef = Ndef.get(tag);
    	if (ndef != null) {
    	  try {
			ndef.connect();
			ndef.writeNdefMessage(ndefMessage);
    	  } catch (IOException e) {
			 // TODO Auto-generated catch block
    		 e.printStackTrace();
    	  } catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	} else {
    	  NdefFormatable format = NdefFormatable.get(tag);
    	  if (format != null) {
    	    try {
				format.connect();
				format.format(ndefMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
    	  	} catch (FormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
    	  }           
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

	private void clearWord() {
		Log.v(TAG, String.format("imageview id: %d", R.id.wordImageView));
		for (int id : wordLetterIds) {
			Log.v(TAG, findViewById(id).getClass().getName());
			Log.v(TAG, new Integer(id).toString());
			((TextView) findViewById(id)).setText("");
			((TextView) findViewById(id)).setTextColor(Color.BLACK);
			((TextView) findViewById(id)).setClickable(false);
			//((TextView) findViewById(id)).setVisibility(View.INVISIBLE);
		}
	}

	private void showWord(final String word) {
		Log.v(TAG, word);
		clearWord();
		char[] cWord = word.toCharArray();
		for (int i = 0; i < word.length(); i++) {
			TextView letterView = ((TextView) findViewById(wordLetterIds[4 - word.length() / 2 + i]));
			letterView.setText(word.substring(i, i + 1));
			//letterView.setVisibility(View.VISIBLE);
			letterView.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					mTts.speak(word,TextToSpeech.QUEUE_FLUSH, null);
				}
				
			});
		}
	}

	private void presentLetter(boolean newLetter) {

		Log.v(TAG, "Present letter");
		
		mWord = getWord(mLetter);
		
		((ImageView) findViewById(R.id.wordImageView)).setClickable(false);
		new RetriveImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mWord);
			if(newLetter) {
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
			new SayLetter().execute(mLetter);
		}
		new SpellWord().execute(mWord);
	}
	
	private class SayLetter extends AsyncTask<Character, Integer, Void> {

		@Override
		protected Void doInBackground(Character... letters) {
			Character letter = letters[0];
			
			((TextView) findViewById(R.id.bigLetter)).setTextColor(Color.BLACK);
			((TextView) findViewById(R.id.littleLetter)).setTextColor(Color.BLACK);
			
			mTts.playSilence(1000, TextToSpeech.QUEUE_ADD, null);
			while (mTts.isSpeaking());
			
			publishProgress(R.id.bigLetter,0);
			mTts.speak(String.format("Big %c!", letter), TextToSpeech.QUEUE_FLUSH, null);
			while (mTts.isSpeaking());
			//publishProgress(R.id.bigLetter,Color.BLACK);
			
			publishProgress(R.id.littleLetter,0);
			mTts.speak(String.format("Little %c!", letter), TextToSpeech.QUEUE_FLUSH, null);
			while (mTts.isSpeaking());
			//publishProgress(R.id.littleLetter,Color.BLACK);
			
			//publishProgress(R.id.littleLetter,0);
			//publishProgress(R.id.bigLetter,0);
			mTts.speak(String.format("You found the letter %c!", letter), TextToSpeech.QUEUE_FLUSH, null);
			while (mTts.isSpeaking());
			//publishProgress(R.id.littleLetter,Color.BLACK);
			//publishProgress(R.id.bigLetter,Color.BLACK);
			
			mTts.playSilence(333, TextToSpeech.QUEUE_ADD, null);
			mTts.speak(String.format("What starts with %c?", letter),
					TextToSpeech.QUEUE_ADD, null);
			mTts.playSilence(333, TextToSpeech.QUEUE_ADD, null);
			
			return null;
		}

		protected void onProgressUpdate(Integer... args) {
			int view = args[0];
			int color = args[1];
			if (color == 0) {
				color = 0xff000000 | new Random().nextInt();
			}
			((TextView) findViewById(view)).setTextColor(color);
		}

		protected void onPostExecute(Void v) {
			
		}

	}

	private class SpellWord extends AsyncTask<String, Integer, Void> {
		
		@Override
		protected void onPreExecute() {
			showWord(mWord);
		}

		@Override
		protected Void doInBackground(String... words) {
			String word = words[0];
			Character c = word.toCharArray()[0];
			
			mTts.playSilence(333, TextToSpeech.QUEUE_ADD, null);
			mTts.speak(String.format("%s starts with %c!", word, c),
					TextToSpeech.QUEUE_ADD, null);
			mTts.playSilence(333, TextToSpeech.QUEUE_ADD, null);
			mTts.speak(String.format("%s is spelled", word),
					TextToSpeech.QUEUE_ADD, null);
		
			while (mTts.isSpeaking());
			mTts.playSilence(333, TextToSpeech.QUEUE_ADD, null);
			for (int i = 0; i < word.length(); i++) {
				this.publishProgress(i, 0);
				mTts.speak(word.substring(i, i + 1), TextToSpeech.QUEUE_ADD,
						null);
				mTts.playSilence(333, TextToSpeech.QUEUE_ADD, null);
				while (mTts.isSpeaking())
					;
				//publishProgress(i, Color.BLACK);
			}
			return null;
		}

		protected void onProgressUpdate(Integer... args) {
			int index = args[0];
			int color = args[1];
			if (color == 0) {
				color = 0xff000000 | new Random().nextInt();
			}
			((TextView) findViewById(wordLetterIds[4 - mWord.length() / 2
					+ index])).setTextColor(color);
		}

		protected void onPostExecute(Void v) {
			new VoiceCapture().execute(mWord);
			mTts.speak(String.format("%s can you say %s?",mChildName,mWord),
					TextToSpeech.QUEUE_ADD, null);
			((ImageView) findViewById(R.id.wordImageView)).setClickable(true);
		}

	}

	private class VoiceCapture extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... words) {
			String word = words[0];
			Intent recognizeIntent = buildRecognizeIntent(word);
			while (mTts.isSpeaking())
				;
			startActivityForResult(recognizeIntent, mLetter);
			return null;
		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		//https://help.github.com/articles/dealing-with-non-fast-forward-errors
//			myView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		mChildName = getSharedPreferences(APP_PREFS, 0).getString(CHILD_NAME_ENTRY,mChildName);
		ActionBar bar = getActionBar();
		bar.setTitle(mChildName);
		
		/*int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
		TextView titleView = (TextView)findViewById(titleId);
		titleView.setText("hello");*/
				
		mAdapter = NfcAdapter.getDefaultAdapter(this);

        // Create a generic PendingIntent that will be deliver to this activity. The NFC stack
        // will fill in the intent with the details of the discovered tag before delivering to
        // this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Setup an intent filter for all MIME based dispatches
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        
        mFilters = new IntentFilter[] {
                ndef,tech
        };

        // Setup a tech list for all NfcF tags
        mTechLists = new String[][] { new String[] { NdefFormatable.class.getName() } };

        
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
					
				} else {
					Log.e(TAG, "ERROR: missing record");
				}
			} else {
				Log.e(TAG, "ERROR: missing ndef messages");
			}
		} else {
			mLetter = 'A';
			Log.v(TAG, mLetter.toString());
		}
		
		mTts = new TextToSpeech(this, this);
        mTts.setPitch(1.5f);

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

		if (mLetter != null) presentLetter(true);

	}
  
	@Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters,
                mTechLists);
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	return true;
        // Handle item selection
        /*switch (item.getItemId()) {
            case R.id.menu_write:
            	Log.v(TAG,"Start Write Mode");
            	View x = (View) findViewById(R.layout.activity_main);
            	x.setBackgroundColor(Color.RED);
                return true;
            case R.id.menu_name:
                Log.v(TAG,"Setting child's name");
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Child's Name");
                ///alert.setMessage(String.format("Currently set to %s",mChildName));
                
                // Set an EditText view to get user input 
                final EditText input = new EditText(this);
                input.setText(mChildName);
                input.selectAll();
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                  mChildName= input.getText().toString();
                  getSharedPreferences(APP_PREFS, 0).edit().putString(CHILD_NAME_ENTRY,mChildName).commit();
                  ActionBar bar = getActionBar();
          		  bar.setTitle(mChildName);
                  }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                  }
                });

                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
    }
    
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.show();
    }
    
	public Intent buildRecognizeIntent(String word)// , int maxResultsToReturn)
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
							String.format("Good Job %s! You said %s",mChildName, mWord),
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
					presentLetter(false);
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
