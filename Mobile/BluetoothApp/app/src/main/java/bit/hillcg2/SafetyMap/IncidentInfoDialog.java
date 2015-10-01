package bit.hillcg2.SafetyMap;


import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Button;
import android.view.View.OnClickListener;

import bit.hillcg2.SafetyMap.Models.Incident;

public class IncidentInfoDialog extends DialogFragment{

    private Incident currIncident;
    private TextView boxPassingDist;
    private TextView boxTime;
    private TextView boxDate;
    private Button btnClose;
    private Button btnDelete;

    public IncidentInfoDialog(){

    }

    public void setIncident(Incident incident){
        currIncident = incident;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.marker_info, container, false);

        boxPassingDist = (TextView)v.findViewById(R.id.boxPassingDist);
        boxTime = (TextView)v.findViewById(R.id.boxTime);
        boxDate = (TextView)v.findViewById(R.id.boxDate);
        btnClose = (Button)v.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new closeHandler());
        btnDelete = (Button)v.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new deleteHandler());

        boxPassingDist.setText(String.valueOf(currIncident.getDistance()) + "cm");
        boxTime.setText(currIncident.getTime());
        boxDate.setText(currIncident.getDate());

        return v;
    }

    public class closeHandler implements OnClickListener{

        @Override
        public void onClick(View v) {
            MapActivity m = (MapActivity)getActivity();
            m.closeFragment();
        }
    }

    public class deleteHandler implements OnClickListener{

        @Override
        public void onClick(View v) {
            MapActivity m = (MapActivity)getActivity();
            m.deleteIncident(currIncident);
        }
    }
}
