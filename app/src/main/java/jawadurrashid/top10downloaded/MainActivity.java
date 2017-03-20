package jawadurrashid.top10downloaded;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView listApplications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listApplications = (ListView) findViewById(R.id.xmlListView);


        /* while (true){                  // Loop prevents onCreate method from finishing; will make app freeze
            int x = 5;
        }*/

        // Downloading data over internet can unreliable, therefore it is good to run download on a separate thread

        Log.d(TAG, "onCreate: starting Asynctask");     //To start background task, we must create instance of DownloadData class and call excecute method
        DownloadData downloadData = new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
        Log.d(TAG, "onCreate: done");

    }

    private class DownloadData extends AsyncTask<String, Void, String> {        //Subclassing AsyncTask; built in Android task that takes care of multi-threading complexities

        // Allows user to run code in background thread so that app is not blocked or waiting for a long running process such as downloading data
        // Allows you to perform background operations and publish results on the UI thread without having to manipulate threads and/or handlers.
        // Parameters; (String) containing URL and Address of the RSS feed, (Void) progress bar not necessary for short downloads, (String) containing XML after being downloaded

        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {          //Runs on main UI thread once the background process is completed and returns value
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: paramater is " + s);  //Print out parameter passed in

            ParseApplication parseApplication = new ParseApplication(); //Creating new ParseApplication object
            parseApplication.parse(s);  //Recall "s" is the XML data that Android framework has sent after downloading in the doInBackground method

            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplication.getApplications());  //Create array adapter object, parameters (context (instance of main activity), resource containing the textView that the array adapter will use to put the data into, list of objects to display)
            listApplications.setAdapter(arrayAdapter);  //Tell adapter to get data


        }

        @Override
        protected String doInBackground(String... params) {   //Main method that does processing on other thread, elipses allows user to pass in multiple values/parameters (URL's in this case) into the class
            Log.d(TAG, "doInBackground: starts with " + params[0]);  //Print out parameter that is passed
            String rssFeed = downloadXML(params[0]);   //downloadXML will download the feed and return a string containing the XML
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");  //Log.d entries don't appear in the log cat when productive version of app is released, we want this message to remain therefore log.e is used
            }
            return rssFeed; //Elipses parameter gets passed into class as an array, wil return first element in array (params[0])
        }

        //Open http connection will access stream of data coming from the internet from the URL, connection provides input stringtherefore an input string reader will be used to read data
        //Buffered reader - buffers data coming in from stream, read in to the buffer in memory and program could read from the buffer

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder(); //Will be appending to string a lot as we read characters from the input string

            try {     //Dealing with data from external source; not in computer's memory therefore data not entirely accountable
                //Rap up a section of code and catch any exceptions that occur while it is executing
                URL url = new URL(urlPath);   //Checked exceptions must be handled in order for compiler to work; run time exceptions don't interfere with compiling
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //iO exception can be thrown, there may be a problem with the internet connection or URl can refer to a server that does not exit

                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was " + response);             //Input stream is created from open http connection

               /* InputStream inputStream = connection.getInputStream();                    //Creates input stream, uses that to create input stream reader which is used to create a buffer reader
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream); //InputStreamReader uses inputStream object as a source of its data
                BufferedReader reader = new BufferedReader(inputStreamReader);     //Buffered reader will be used to read the XML*/

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charactersRead;
                char[] inputBuffer = new char[1000];      //InputBuffer character array (stores 1000 characters)
                while (true) {                              //Will continue until input stream is finished
                    charactersRead = reader.read(inputBuffer);
                    if (charactersRead < 0) {              //Signals end of stream of data, break out of loop, link terminates and closes the buffered reader
                        break;
                    }
                    if (charactersRead > 0) {              //charactersRead variable will hold and count number of characters read from stream
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charactersRead));
                    }
                }

                reader.close();  //Closes buffered reader, input stream reader and input stream
                return xmlResult.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL" + e.getMessage()); //Error, not just debugging information
            } catch (IOException e) {                                  //"e" is type of error, and getMessage gives us information about error
                Log.e(TAG, "downloadXML: IO Exception reading data: " + e.getMessage()); //Order of catching exceptions matters; MalformedURLException is a sublass of the IOException
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security exception. Needs permission? " + e.getMessage());
//                 e.printStackTrace(); //Get access to stack trace having caught the actual exception that caused the initial stack trace
            }

            return null;
        }

    }

    }

