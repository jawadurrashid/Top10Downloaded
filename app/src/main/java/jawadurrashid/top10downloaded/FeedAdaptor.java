package jawadurrashid.top10downloaded;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by User on 2017-03-19.
 */

public class FeedAdaptor extends ArrayAdapter {

    private static final String TAG = "FeedAdaptor";   //Store layout resource that will be given in constructor and list that contains our data
    private final int layoutResource;
    private final LayoutInflater layoutInflater;  //Final field cannot be changed, needs value when declared
    private List<FeedEntry> applications;

    public FeedAdaptor(@NonNull Context context, @LayoutRes int resource, List<FeedEntry> applications) {
        super(context, resource);
        this.layoutResource = resource;  //"this" current instance of class, specifies field opposed to parameter passed into the class
        this.layoutInflater = LayoutInflater.from(context); //Context holds state of activity while it is running, contains info that system needs to manage it and allows access to various classes needed as well
        this.applications = applications;
    }
}


//Inflate XML resource to create the view (producing actual widgets from XML)