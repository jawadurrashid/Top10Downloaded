package jawadurrashid.top10downloaded;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by User on 2017-03-18.
 */

public class ParseApplication {
    private static final String TAG = "ParseApplication";

    //Building list of feed entry objects in class, stores applications found in XML data
    // Will be created as we parse those entry tags in XML and will be stored in arraylist

    private ArrayList<FeedEntry> applications;

    public ParseApplication() {
        this.applications = new ArrayList<>();   //When XML is parsed, list will contain various feed entry objects (one for each entry in the XML)
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    //Will parse and manipulate XML data Strings and create list of applications that will be stored in the application array list

    public boolean parse(String xmlData) {
        boolean status = true;
        FeedEntry currentRecord = null;
        boolean inEntry = false;   //Return false if data could not be parsed for whatever reason, keeps track of tags whether in entry or not in order to avoid confusion
        String textValue = "";

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();  //Responsible for setting up the Java XML parser
            factory.setNamespaceAware(true);        //API provides factory that will produce pullParser object, factory classes are used if actual class is not known
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData)); //Tells object what to parse (StringReader) that uses the XML string data (XML data that is downloaded from iTunes website)
            int eventType = xpp.getEventType();  //Processes strings

            while (eventType != XmlPullParser.END_DOCUMENT) { //Check if parser is at the end of the document, will loop until it gets to the end of the document

                String tagName = xpp.getName();          //Checking for tags and extracting the data we need from the XML

                //For example; <updated> 2016-07-21T19 </updated>
                // <updated> is the entry or start tag, (2016-07-21) is the text value and </updated> is the end tag

                switch (eventType) {                     //Getting name of current tag
                    case XmlPullParser.START_TAG:
                        Log.d(TAG, "parse: Starting tag for  " + tagName);
                        if ("entry".equalsIgnoreCase(tagName)) {      //Only interested in an entry tag; only concerned with data in individual entries
                            inEntry = true;                          //If we have entry tag we create an instance of the feed entry class for data storage
                            currentRecord = new FeedEntry();
                        }

                        break;

                    case XmlPullParser.TEXT:       //Pull parser is telling us that data is available, therefore will store the data into the String variable
                        textValue = xpp.getText(); //Stores text when new text is available
                        break;

                    case XmlPullParser.END_TAG:
                        Log.d(TAG, "parse: Ending tag for " + tagName); //Will read current tag and extract the appropriate data from XML according to tag
                        if (inEntry) {                                   //Checks if pull parser is in entry tag, if it is we can test the tag name and assign the variable to the correct field of the current object
                            if ("entry".equalsIgnoreCase(tagName)) {     //String entry cannot be null
                                applications.add(currentRecord);        //If we reached the tagName, we have reached the end of the data for the current record (end tag for the entry)
                                inEntry = false;
                            } else if ("name".equalsIgnoreCase(tagName)) {  //Otherwise, the parser will store data for the various different fields
                                currentRecord.setName(textValue);
                            } else if ("artist".equalsIgnoreCase(tagName)) {
                                currentRecord.setArtist(textValue);
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                currentRecord.setReleaseDate(textValue);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                currentRecord.setSummary(textValue);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                currentRecord.setImageURL(textValue);
                            }
                        }

                        break;

                    default: //Nothing
                }

                eventType = xpp.next();   //Tells parser to continue working in XML until something interesting happens (value in tag, reaches end of document, etc)

            }

            //Loop through application list once the XMl has been processed and print out values of the fields

            for (FeedEntry app : applications) {
                Log.d(TAG, "parse: ******");
                Log.d(TAG, app.toString());
            }

        } catch (Exception e) {  //Catch all exceptions
            status = false;
            e.printStackTrace();
        }

        return status;

    }

}


//For ListView to function, an adaptor must be placed between the ListView and data
//Whenever ListView needs to display more data, it asks the adaptor for a view that it can display
//The adaptor is responsible for placing the values of the data into the correct widgets in the view, then returns the view to the ListView for display
