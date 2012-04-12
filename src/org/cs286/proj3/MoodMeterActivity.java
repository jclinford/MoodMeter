package org.cs286.proj3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class MoodMeterActivity extends ListActivity {
	private String defVlaue1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		thisActivity = this;
	}

	public int clickT1(View v) {
		setContentView(R.layout.viewmood);

		progressLabel = (TextView) findViewById(R.id.volume);
		topicEdit = (EditText) findViewById(R.id.topicField);
		rate = (SeekBar) findViewById(R.id.seekRate);

		rate.setMax(5);

		// rate listener
		rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// set text for progress label
				progressLabel.setText("" + progress);

				// update server
				try
				{
					writer.write("Mood:" + progress + ":" + name + "\n");
					writer.flush();

					// should read okay
					String input = reader.readLine().trim();
					Log.i("MoodMeter", input);
					
					update();

					Log.i("MoodMeter", "Update Mood sucess");
				}
				catch (Exception e)
				{
					Log.i("MoodMeter", "Update Mood failed");
					e.printStackTrace();
				}

				Log.i("MoodMeter", "Rating Changed");
			}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		// set adapter
		adapter = new ArrayAdapter<String>(thisActivity,
				android.R.layout.simple_list_item_1, moods);
		setListAdapter(adapter);

		// start the updating of messages every 60 sec
		myTimer = new Timer();
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// tell the handler to update
				Message msg = new Message();
				msg.obj = "update";
				mHandler.sendMessage(msg);
			}
		}, 0, 60000);

		return 0;
	}

	public void clickT2(View v) {
		setContentView(R.layout.main);
		
		// stop updates
		myTimer.cancel();

		serverTextBox = (EditText) findViewById(R.id.ServerField);
		nameTextBox = (EditText) findViewById(R.id.NameField);

		SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);
		String sName = myPref.getString("Server",defVlaue1).toString();
		String nPerson = myPref.getString("Name",defVlaue1).toString();
		Log.i("MoodMeter", nPerson+"  edit push");

		serverTextBox.setText(sName);
		nameTextBox.setText(nPerson);
	}

	public void applyClicked(View v) {
		Log.i("MoodMeter", "ApplyClicked");
		serverTextBox = (EditText) findViewById(R.id.ServerField);
		nameTextBox = (EditText) findViewById(R.id.NameField);

		server = serverTextBox.getText().toString();
		name = nameTextBox.getText().toString();

		SharedPreferences myPref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor prefEditor = myPref.edit();
		prefEditor.putString("Server", server);
		prefEditor.putString("Name", name);
		prefEditor.commit();
		Log.i("MoodMeter", server + " ApplyClicked " + name);

		// set up connection
		try {
			// socket = new Socket(IP, Integer.parseInt(PORT));
			socket = new Socket(server, Integer.parseInt(PORT));
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			String connection = reader.readLine();
			connection.trim();

			// the string mood server will be replied if we are connected correctly
			if (connection.equals("Mood Server")) {
				writer.write("Register:" + name + "\n");
				writer.flush();

				// should read okay
				String input = reader.readLine().trim();
				Log.i("MoodMeter", input);
			}

			Log.i("MoodMeter", "Connection established");
		} catch (Exception e) {
			Log.i("MoodMeter", "Unable to establish connection");
			e.printStackTrace();
		}

	}

	public void setClicked(View v) {
		topicEdit = (EditText) findViewById(R.id.topicField);
		topic = topicEdit.getText().toString().trim();
		rate.setProgress(0);

		if (socket != null) {
			try {
				writer.write("Topic:" + topic + "\n");
				writer.flush();
				Log.i("MoodMeter", "Topic updated: " + topic);

				// should read okay
				String input = reader.readLine().trim();
				Log.i("MoodMeter", input);

				update();
			} catch (Exception e) {
				Log.i("MoodMeter", "Topic update failed");
			}
		}
	}

	public void update() {
		try
		{
			Log.i("MoodMeter", "Updating");

			String name = "";
			moods = new ArrayList<String>();

			// ask server for status
			writer.write("Status\n");
			writer.flush();

			// the first line will be the topic
			//reader.readLine();
			topic = reader.readLine();
			topicEdit.setText(topic.substring(6));

			// continue to read lines until we get success returned
			name = reader.readLine();
			while (name.trim().length() > 5) {
				//				// if no topic don't write anything
				//				if (topic.contains("No Topic"))
				//					break;

				// else add the names to the list
				moods.add(name.substring(5));
				Log.i("MoodMeter", "Updated Name: " + name);

				name = reader.readLine();
			}

			adapter = new ArrayAdapter<String>(thisActivity,
					android.R.layout.simple_list_item_1, moods);
			setListAdapter(adapter);
			Log.i("MoodMeter", "ListView updated");
		} catch (Exception e) {
			Log.i("MoodMeter", "Unable to update");
			e.printStackTrace();
		}
	}
	
	// handles updates in main ui thread
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
				Log.i("MoodMeter", "Handler updating ui");
				update();
		}
	};

	private SeekBar rate;
	private TextView progressLabel;
	private EditText topicEdit;
	private ListActivity thisActivity;
	EditText serverTextBox;
	EditText nameTextBox;
	String Sname;
	String Uname;

	// List view stuff
	private static String topic = "";
	private ArrayAdapter<String> adapter;
	private ArrayList<String> moods = new ArrayList<String>();

	// Server stuff
	private Timer myTimer;
	private BufferedReader reader;
	private BufferedWriter writer;
	private Socket socket;
	private String server;
	private String name;
	private static final String PORT = "7890";
	private static final String IP = "";

}