package com.example.cat;
 
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;


public class MainActivity extends Activity {
	private HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private Button button1;
    private EditText editText1;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    static TextView tap_on_mic;
    static String addr;
	private static final String TAG = "Voice::MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dictionary.put("Включи свет", 10);
        dictionary.put("Выключи свет", 0);
        setContentView(R.layout.activity_main);
        tap_on_mic = (TextView)findViewById(R.id.tap_on_mic);
    
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        button1 = (Button) findViewById(R.id.button1);
        editText1 = (EditText) findViewById(R.id.editText1);
        // hide the action bar
        getActionBar().hide();
        
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	 promptSpeechInput();
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			addr = editText1.getText().toString();
				
			}
		});
        getApplicationContext();
        getResources();
    }
    

 
    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    
 
    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
         case REQ_CODE_SPEECH_INPUT: {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String recVariant = result.get(0);
                txtSpeechInput.setText(recVariant);
                Integer ligthLevel = dictionary.get(recVariant);
                if(ligthLevel != null){
                    try {
                        new DataSender().execute();
                        tap_on_mic.setText("Команда распознана " + result.get(0));
                    } catch (Exception e) {
                        Log.e(TAG, "Could not send the data: " + e.toString());
                    }

                } else{
                    tap_on_mic.setText("Команда не распознана");
                }
               
            }
            break;
         }
 
        }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private class DataSender extends AsyncTask<String, Void, Void>
	{

		private static final String TAG = "Voice::DataSender";

		@Override
		protected Void doInBackground(String... params) {
			
				try
				{
			        	TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
			                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			                    return null;
			                }
			                public void checkClientTrusted(X509Certificate[] certs, String authType) {
			                }
			                public void checkServerTrusted(X509Certificate[] certs, String authType) {
			                }
			            }
			        };
			 
			        // Install the all-trusting trust manager
			        SSLContext sc = SSLContext.getInstance("SSL");
			        sc.init(null, trustAllCerts, new java.security.SecureRandom());
			        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			 
			        // Create all-trusting host name verifier
			        HostnameVerifier allHostsValid = new HostnameVerifier() {
			            public boolean verify(String hostname, SSLSession session) {
			                return true;
			            }
			        };
			 
			        // Install the all-trusting host verifier
			        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

				    String data = com.budka.network.Utils.Pack("LIGHT","1");
					
				    URL url = new URL(addr);
					
			        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
					
			        conn.setDoOutput(true);
			        conn.setInstanceFollowRedirects(false);
			        try {
						conn.setRequestMethod("POST");
					} catch (ProtocolException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
			        conn.setRequestProperty("Content-Type", "application/json");
			        conn.setRequestProperty("charset", "utf-8");
			        conn.setRequestProperty("Content-Length", ((Integer) data.length()).toString());
			        conn.setUseCaches(false);
			                    try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
			                        wr.write(data.getBytes());
			                        wr.flush();
			                        wr.close();
			                    } catch (IOException e) {
			                        Log.e(TAG, "Could not send the data: " + e.toString());
			                    }
}
		 catch (Exception ex)
	        {
	        	Log.e(TAG, "Failed to establish SSL connection to server: " + ex.toString());
	            
	        }
			return null;
		}
		
	}
}

 

		