package jawadurrashid.top10downloaded;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by User on 2017-03-18.
 */

public class ParseApplication {
    private static final String TAG = "ParseApplication";

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
        boolean inEntry = false;
        String textValue = "";

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xmlData));
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                String tagName = xpp.getName();

                //For example; <updated> 2016-07-21T19 </updated>
                // <updated> is the entry or start tag, (2016-07-21) is the text value and </updated> is the end tag

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if ("entry".equalsIgnoreCase(tagName)) {
                            inEntry = true;
                            currentRecord = new FeedEntry();
                        }

                        break;

                    case XmlPullParser.TEXT:
                        textValue = xpp.getText();
                        break;

                    case XmlPullParser.END_TAG:
                        if (inEntry) {
                            if ("entry".equalsIgnoreCase(tagName)) {
                                applications.add(currentRecord);
                                inEntry = false;
                            } else if ("name".equalsIgnoreCase(tagName)) {
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

                    default:
                }

                eventType = xpp.next();

            }


        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        }

        return status;

    }

}


//For ListView to function, an adaptor must be placed between the ListView and data
//Whenever ListView needs to display more data, it asks the adaptor for a view that it can display
//The adaptor is responsible for placing the values of the data into the correct widgets in the view, then returns the view to the ListView for display
