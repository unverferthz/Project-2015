package bit.hillcg2.SafetyMap;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class ViewIncidents extends ActionBarActivity {
    ArrayAdapter<String> listAdapter;
    DBManager dbManager;
    Button btnSendData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_incidents);

        init();
    }

    //Initialize everything
    public void init(){

        //Set up buttons
        Button btnBack = (Button)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new goBack());

        Button btnReset = (Button)findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new resetDB());

        btnSendData = (Button)findViewById(R.id.btnSendData);
        btnSendData.setOnClickListener(new sendData());

        //Set up listview
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ListView incidentList = (ListView)findViewById(R.id.incidentList);
        incidentList.setAdapter(listAdapter);

        //Get instance of database
        dbManager = new DBManager(getBaseContext());

        //Populate listview with existing incidents
        populateList();
    }

    //Reads in incidents from database and puts them into listview
    public void populateList(){

        //Ask database for all of the incidents
        ArrayList<Incident> incidentArray = dbManager.getAllIncidents();

        int counter = 0;

        //Loop over all of the incidents
        for(Incident currIncident : incidentArray){
            counter++;

            //Built up string to put into list to display incidents to use
            String textForList = "Incident " + counter + ":\n";

            textForList += "Distance: " + currIncident.getDistance() + "cm\n";
            textForList += "Time: " + currIncident.getTime() + "\n";
            textForList += "Date: " + currIncident.getDate() + "\n";
            textForList += "Latitude: " + currIncident.getLat() + "\n";
            textForList += "Longitude: " + currIncident.getLng();

            //Add into adapter and update so it can be seen
            listAdapter.add(textForList);
            listAdapter.notifyDataSetChanged();
        }
    }

    //Button handler to go back to main screen
    public class goBack implements OnClickListener{
        @Override
        public void onClick(View view) {
            Intent goBackIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(goBackIntent);
            finish();
        }
    }

    //Button handler to reset the database
    public class resetDB implements OnClickListener{
        @Override
        public void onClick(View view) {
            dbManager.resetDatabase();
            listAdapter.clear();
            listAdapter.notifyDataSetChanged();
        }
    }

    public class sendData implements OnClickListener{

        @Override
        public void onClick(View view) {
            btnSendData.setEnabled(false);
            DBManager dbManager = new DBManager(getBaseContext());
            FTPManager ftpManager = new FTPManager(getBaseContext(), dbManager, ViewIncidents.this);

            ftpManager.sendFile();
        }
    }

    public void finishedUpload(boolean success){
        btnSendData.setEnabled(true);

        if(success)
            Toast.makeText(getBaseContext(), "Uploaded data", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getBaseContext(), "No new data", Toast.LENGTH_LONG).show();
    }
}
