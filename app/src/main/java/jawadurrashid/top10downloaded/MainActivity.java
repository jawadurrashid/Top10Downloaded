package jawadurrashid.top10downloaded;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int feedLimit = 10;
    private String feedCachedUrl = "INVALID";  //Invalid URL in case we want to force a download to happen
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";


    //String format method takes string containing special format codes and number of values to replace the  format code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listApplications = (ListView) findViewById(R.id.xmlListView);

        if (savedInstanceState != null) {      //Bundle passed to onCreate will be non-null if the activity is restarted (changing orientation)
            feedUrl = savedInstanceState.getString(STATE_URL);
            feedLimit = savedInstanceState.getInt(STATE_LIMIT);

        }


        /* while (true){                  // Loop prevents onCreate method from finishing; will make app freeze
            int x = 5;
        }*/

        // Downloading data over internet can unreliable, therefore it is good to run download on a separate thread

        downloadUrl(String.format(feedUrl, feedLimit));   //Parsing web link and feed limit (set to 10) and will replace %d

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);    //Activity is a context
        if (feedLimit == 10) {
            menu.findItem(R.id.mnu10).setChecked(true);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        return true;                                           // Tells Android that menu has been inflated
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
                break;

            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;

            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;

            case R.id.mnu10:

            case R.id.mnu25:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    feedLimit = 35 - feedLimit;
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " setting Feed limit to " + feedLimit);

                } else {
                    Log.d(TAG, "onOptionsItemSelected: " + item.getTitle() + " feedLimit unchanged");
                }
                break;

            case R.id.mnuRefresh:
                feedCachedUrl = "INVALIDATED";
                break;

            default:
                return super.onOptionsItemSelected(item);

        }

        downloadUrl(String.format(feedUrl, feedLimit));
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, feedLimit);
        super.onSaveInstanceState(outState);
    }

    private void downloadUrl(String feedUrl) {

        if (!feedUrl.equalsIgnoreCase(feedCachedUrl)) {     //Will check value against stored value; if they are the same then the data will not be downloaded
            Log.d(TAG, "downloadUrl: starting Asynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedUrl);
            feedCachedUrl = feedUrl;
            Log.d(TAG, "downloadUrl: done");
        } else {
            Log.d(TAG, "downloadUrl: URL not changed");
        }
    }

    private class DownloadData extends AsyncTask<String, Void, String> {        //Subclassing AsyncTask; built in Android task that takes care of multi-threading complexities

        // Allows user to run code in background thread so that app is not blocked or waiting for a long running process such as downloading data
        // Allows you to perform background operations and publish results on the UI thread without having to manipulate threads and/or handlers.
        // Parameters; (String) containing URL and Address of the RSS feed, (Void) progress bar not necessary for short downloads, (String) containing XML after being downloaded

        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {          //Runs on main UI thread once the background process is completed and returns value
            super.onPostExecute(s);
//            Log.d(TAG, "onPostExecute: paramater is " + s);  //Print out parameter passed in

            ParseApplication parseApplication = new ParseApplication(); //Creating new ParseApplication object
            parseApplication.parse(s);  //Recall "s" is the XML data that Android framework has sent after downloading in the doInBackground method

//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplication.getApplications());  //Create array adapter object, parameters (context (instance of main activity), resource containing the textView that the array adapter will use to put the data into, list of objects to display)
//            listApplications.setAdapter(arrayAdapter);  //Tell adapter to get data

            FeedAdapter feedAdaptor = new FeedAdapter(MainActivity.this, R.layout.list_record, parseApplication.getApplications());
            listApplications.setAdapter(feedAdaptor);

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

