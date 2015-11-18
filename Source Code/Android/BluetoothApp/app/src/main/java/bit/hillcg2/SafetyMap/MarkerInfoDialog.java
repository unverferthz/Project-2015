package bit.hillcg2.SafetyMap;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnClickListener;
import bit.hillcg2.SafetyMap.Models.Incident;


//Class for a custom Dialog pop up box
public class MarkerInfoDialog extends DialogFragment {

    private Button btnDelete;
    private Button btnClose;
    private TextView distanceBox;
    private TextView timeBox;
    private TextView dateBox;

    private Incident currIncident;


    public MarkerInfoDialog(){
        currIncident = null;
    }

    //Pre-condition: Gets called before onCreateView
    //Post-condition: Set the incident to be displayed in the pop up
    public void setCurrIncident(Incident newCurrIncident){
        currIncident = newCurrIncident;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Inflate layout
        View v = inflater.inflate(R.layout.marker_info_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Set up delete button
        btnDelete = (Button)v.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new deleteHandler());

        //Disable button if there is no incident
        if(currIncident == null)
        {
            btnDelete.setEnabled(false);
        }

        //Set up close button
        btnClose = (Button)v.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new closeHandler());

        //Display all of the incidents information
        if(currIncident != null)
        {
            distanceBox = (TextView) v.findViewById(R.id.distanceBox);
            distanceBox.setText(String.valueOf(currIncident.getDistance()) + "cm");
            timeBox = (TextView) v.findViewById(R.id.timeBox);
            timeBox.setText(currIncident.getTime());
            dateBox = (TextView) v.findViewById(R.id.dateBox);
            dateBox.setText(currIncident.getDate());
        }

        return v;
    }

    //Handler to delete incidents
    public class deleteHandler implements OnClickListener{
        @Override
        public void onClick(View v) {
            //ask mapActivity to delete incident
            int incidentID = currIncident.getIncidentID();

            //Get instance of map activity
            MapActivity mapActivity = (MapActivity)getActivity();

            //ask map to delete incident
            mapActivity.deleteIncident(incidentID);
            mapActivity.closeDialog();

        }
    }

    //Button handler to close the dialog
    public class closeHandler implements OnClickListener{
        @Override
        public void onClick(View v) {
            //ask mapActivity to close
            MapActivity mapActivity = (MapActivity)getActivity();
            mapActivity.closeDialog();
        }
    }
}
