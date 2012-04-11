import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class SimpleServer extends Thread
{
	private static final int PORT = 7890;
	private static final int NUM_CONNECT = 1;
	private static final String fail = "Sorry\n";
	private static final String success = "Okay\n";

	private String curTopic = "No Topic";
	private HashMap<String, String> hashMap;
	private PrintWriter writer;
	private BufferedReader reader;

	private SimpleServer(){}

	public static void main(String args[])
	{
		SimpleServer myServer = new SimpleServer();

		if(myServer !=null) 
		{
			myServer.start();
		}
	}

	public void run()
	{
		// Try to listen to port number and establish connection if possible
		try
		{
			ServerSocket server = new ServerSocket(PORT, NUM_CONNECT);
			Socket client = server.accept();

			System.out.println("Connection Established");

			// create HashMap and opening topic of nothing
			hashMap = new HashMap<String, String>();

			// reader for input
			reader = new BufferedReader(
					new InputStreamReader(client.getInputStream()));

			// write to output
			writer = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(client.getOutputStream())));

			// Write Mood Server to output Stream
			writer.write("Mood Server\n");
			writer.flush();

			String line;
			
			// sit in while loop and read any inputs
			while (client != null)
			{
				line = reader.readLine();
				System.out.println("Input read");
				// process the input
				process(line);
			}
		}
		catch(IOException ie)
		{
			ie.printStackTrace();
		}
	}

	public void process(String input)
	{
		if (input == null)
			return;
		
		System.out.println("Processing");

		// register the name to hashMap
		if (input.contains("Register"))
		{
			String name = null;

			// extract the name (happens directly after ':')
			for (int i = 0; i < input.length(); i++)
			{
				if (input.charAt(i) == ':')
				{
					name = input.substring(++i);
					break;
				}
			}

			// check to see if name is already in hashmap and return sorry
			if (hashMap.containsKey(name))
			{
				writer.println(fail);
				writer.flush();
				return;
			}

			// else, store the name in hashmap with initial mood 0
			hashMap.put(name, "0");

			// output okay
			writer.write(success);
			writer.flush();

			System.out.println("Registered user: " + name);
		}

		// set new topic
		else if (input.contains("Topic"))
		{
			String topic = null;

			for (int i = 0; i < input.length(); i++)
			{
				if (input.charAt(i) == ':')
				{
					topic = input.substring(++i);
					break;
				}
			}

			// update current topic
			curTopic = topic;

			// output okay
			writer.write(success);
			writer.flush();

			System.out.println("New topic set: " + curTopic);
		}

		// set mood
		else if (input.contains("Mood"))
		{
			String name = null;
			String num = null;

			// Expecting format "Mood:Num:Name" so get num first then name is rest of string
			for (int i = 0; i < input.length(); i++)
			{
				if (input.charAt(i) == ':')
				{
					num = input.substring(++i, ++i);
					name = input.substring(++i);
					break;
				}
			}

			// update the name w/ mood number
			hashMap.put(name, num);

			// output okay
			writer.write(success);
			writer.flush();

			System.out.println("" + name + "'s mood changed to: " + num);
		}

		// Write the hashmap to output
		else if (input.equals("Status"))
		{			
			System.out.println("Sending system status");
			
			// Get a set of the entries 
			Set<Entry<String, String>> set = hashMap.entrySet();

			// Get an iterator 
			Iterator<Entry<String, String>> i = set.iterator();

			// Display elements
			writer.write("Topic:" + curTopic + "\n");
			System.out.print("Sending-> Topic:" + curTopic + "\n");
			
			while(i.hasNext()) 
			{ 
				Map.Entry me = (Map.Entry)i.next(); 

				writer.write("Mood:" + me.getKey() + ":" + me.getValue() + "\n");
				System.out.print("Sending-> Mood:" + me.getKey() + ":" + me.getValue() + "\n");
			}

			writer.write(success);
			//writer.flush();

			System.out.println("Status update sent to device");
		}
	}
}