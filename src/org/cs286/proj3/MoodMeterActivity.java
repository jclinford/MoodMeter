package org.cs286.proj3;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MoodMeterActivity extends ListActivity 
{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		//dbStart = new DBHelp(this);
		//dbStart.open();

		setContentView(R.layout.viewmood);
		progressLabel = (TextView) findViewById(R.id.volume);
		topicEdit = (EditText) findViewById(R.id.topicField);
		rate = (SeekBar) findViewById(R.id.seekRate);
		rate.setMax(5);

		// rate listener
		rate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				progressLabel.setText("" + progress);
				System.out.println("Changed");
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});

		// set adapter
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, moods);
		setListAdapter(adapter);

		setContentView(R.layout.main);

	}

	public void clickT1(View v) 
	{
		setContentView(R.layout.viewmood);

		// start the updating of messages
		callBack.run();
		handler.sendEmptyMessage(0);
	}

	public void clickT2(View v) 
	{		
		setContentView(R.layout.main);
		
		// stop updating messages
		handler.removeMessages(0);
		//dbStart.readPersistence();	
	}

	public void applyClicked(View v) 
	{
		EditText serverTextBox;
		EditText nameTextBox;

		serverTextBox = (EditText) findViewById(R.id.ServerField);
		nameTextBox = (EditText) findViewById(R.id.NameField);

		server = serverTextBox.getText().toString();
		name = nameTextBox.getText().toString();

		// set up connection
		try
		{
			//socket = new Socket(IP, Integer.parseInt(PORT));
			socket = new Socket(server, Integer.parseInt(PORT));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			String connection = reader.readLine();

			// the string mood server will be replied if we are connected correctly
			if (connection.equals("Mood Server"))
			{
				writer.write("Register:" + name + "\n");
				writer.flush();
			}

			Log.i("MoodMeter", "Connection established");
		}
		catch(Exception e)
		{
			Log.i("MoodMeter", "Unable to establish connection");
			e.printStackTrace();
		}
		//dbStart.updateDb(serverName, namePerson);

		//topic.topic(serverName);
		//topic.topic(namePerson, 2);

	}


	public void setClicked(View v)
	{
		String topic;

		topic = topicEdit.getText().toString();

		if (socket != null)
		{
			try
			{
				writer.write("Topic:" + topic + "\n");
				writer.flush();
				Log.i("MoodMeter", "Topic updated");
			}
			catch (Exception e)
			{
				Log.i("MoodMeter", "Topic update failed");
			}
		}
	}

	// Runnable object so we can call back to it to update our adapter
	Runnable callBack = new Runnable() {
		@Override
		public void run() 
		{
			try
			{
				String topic;
				String name;

				// ask server for status
				writer.write("Status");

				// the first line will be the topic
				topic = reader.readLine();
				topicEdit.setText(topic.substring(5));

				// continue to read lines until no more
				while ( (name = reader.readLine() ) != null)
				{
					moods = new ArrayList<String>();

					moods.add(name.substring(4));
				}

				adapter = new ArrayAdapter<String>(null, android.R.layout.simple_list_item_1, moods);
				setListAdapter(adapter);
			}
			catch (Exception e)
			{
				Log.i("MoodMeter", "Unable to update");
			}

		}
	};

	// Have the handler call callBack every 60 seconds
	private Handler handler = new Handler() {
		public void handleMessage(Message msg)
		{
			if (msg.what == 0)
			{
				callBack.run();
				this.sendEmptyMessageDelayed(0, 60 * 1000);
			}
		}
	};

	private SeekBar rate;
	private TextView progressLabel;
	private DBHelp dbStart;
	private EditText topicEdit;

	// List view stuff
	private ArrayAdapter<String> adapter;
	private ArrayList<String> moods = new ArrayList<String>();

	// Server stuff
	private BufferedReader reader;
	private BufferedWriter writer;
	private Socket socket;
	private String server;
	private String name;
	private static final String PORT = "7890";
	private static final String IP = "";

}