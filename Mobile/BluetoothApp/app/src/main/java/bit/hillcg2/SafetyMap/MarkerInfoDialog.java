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


public class MarkerInfoDialog extends DialogFragment {

    private Button btnDelete;
    private Button btnClose;
    private TextView distanceBox;
    private TextView timeBox;
    private TextView dateBox;

    private Incident currIncident;


    public MarkerInfoDialog(){

    }

    public void setCurrIncident(Incident newCurrIncident){
        currIncident = newCurrIncident;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.marker_info_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        btnDelete = (Button)v.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new deleteHandler());
        btnClose = (Button)v.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new closeHandler());

        distanceBox = (TextView)v.findViewById(R.id.distanceBox);
        distanceBox.setText(String.valueOf(currIncident.getDistance()) + "cm");
        timeBox = (TextView)v.findViewById(R.id.timeBox);
        timeBox.setText(currIncident.getTime());
        dateBox = (TextView)v.findViewById(R.id.dateBox);
        dateBox.setText(currIncident.getDate());

        return v;
    }

    public class deleteHandler implements OnClickListener{

        @Override
        public void onClick(View v) {
            //ask mapActivity to delete incident
            int incidentID = currIncident.getIncidentID();
            MapActivity mapActivity = (MapActivity)getActivity();
            mapActivity.deleteIncident(incidentID);
            mapActivity.closeDialog();

        }
    }

    public class closeHandler implements OnClickListener{

        @Override
        public void onClick(View v) {
            //ask mapActivity to close
            MapActivity mapActivity = (MapActivity)getActivity();
            mapActivity.closeDialog();
        }
    }
}
