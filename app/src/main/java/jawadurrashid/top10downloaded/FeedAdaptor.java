package jawadurrashid.top10downloaded;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

    @Override
    public int getCount() {               //Returning number  of items on our list
        return applications.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {  //Called by list view every time it wants another item to display

        //When getView method is called, it tells it the position of the items it needs to display position parameters (retrieves objects at that position from applications list)

        View view = layoutInflater.inflate(layoutResource, parent, false); //Creating a view by inflating the layout resource
        TextView tvName = (TextView) view.findViewById(R.id.tvName);     //Finds the three textView widgets; inflating view relates to layout resouce which comes from list_record (constraint layout)
        TextView tvArtist = (TextView) view.findViewById(R.id.tvArtist); //Find id part of this view and set these fields to the corresponding textViews in the layout
        TextView tvSummary = (TextView) view.findViewById(R.id.tvSummary);  //Android framework sending back parent to us (list_record)

        FeedEntry currentApp = applications.get(position);

        tvName.setText(currentApp.getName());
        tvArtist.setText(currentApp.getArtist());
        tvSummary.setText(currentApp.getSummary());

        return view;
    }
}


//Inflate XML resource to create the view (producing actual widgets from XML)