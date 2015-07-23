package bit.hillcg2.SafetyMap;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class ViewIncidents extends ActionBarActivity {
    private ArrayAdapter<String> listAdapter;
    private DBManager dbManager;
    private Button btnSendData;
    private Spinner spinMonth;
    private Spinner spinDay;
    private String months[];
    private String days[];
    private ArrayAdapter monthAdapter;
    private ArrayAdapter dayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_incidents);

        init();
    }

    //Initialize everything
    public void init(){
        //Get instances of views from layout and set them up
        Button btnBack = (Button)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new goBack());

        Button btnReset = (Button)findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new resetDB());

        btnSendData = (Button)findViewById(R.id.btnSendData);
        btnSendData.setOnClickListener(new sendData());

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ListView incidentList = (ListView)findViewById(R.id.incidentList);
        incidentList.setAdapter(listAdapter);

        spinMonth = (Spinner)findViewById(R.id.spinIncidentMonth);
        spinDay = (Spinner)findViewById(R.id.spinIncidentDay);

        Calendar cal = Calendar.getInstance();

        //Get the current month and day to set initial values into spinners
        int currMonth = cal.get(Calendar.MONTH);
        int today = cal.get(Calendar.DAY_OF_MONTH) - 1;

        //Find out how many days the current month has
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        days = new String[daysInMonth];

        //Set up array to have all the days of the current month to use for spinner
        for(int i=0; i < daysInMonth; i++)
        {
            String currDay = String.valueOf(i + 1);
            days[i] = currDay;
        }

        //Array holding all months to use for spinner
        months = new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        //Set up adapters for the spinners
        monthAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months);
        dayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, days);

        //Set spinners adapters
        spinMonth.setAdapter(monthAdapter);
        spinDay.setAdapter(dayAdapter);

        //Set up spinners with their initial values
        spinMonth.setSelection(currMonth, false);
        spinDay.setSelection(today, false);

        spinDay.setOnItemSelectedListener(new daySelected());
        spinMonth.setOnItemSelectedListener(new monthSelected());

        //Get instance of database
        dbManager = new DBManager(getBaseContext());

        //Populate listview with existing incidents
        populateList();
    }

    //Reads in incidents from database and puts them into listview
    public void populateList(){

        listAdapter.clear();

        //TODO remove the 0
        //Get the date values from spinners
        String selectedMonth = "0" + String.valueOf(spinMonth.getSelectedItemPosition() + 1);
        String selectedDay = spinDay.getSelectedItem().toString();

        //Ask database for all of the incidents
        ArrayList<Incident> incidentArray = dbManager.getAllIncidents();

        int counter = 0;

        //Loop over all of the incidents
        for(Incident currIncident : incidentArray){
            //Get the date value to check if it's the same as spinner values
            String date = currIncident.getDate();

            //Split it apart for comparison
            String[] splitDate = date.split("/");
            String currMonth = splitDate[1];
            String currDay = splitDate[0];

            //Check if month and day are the same for incident and spinner values
            if(currMonth.equals(selectedMonth) && currDay.equals(selectedDay))
            {
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

    //Handler to start sending file up to the server
    public class sendData implements OnClickListener{

        @Override
        public void onClick(View view) {
            //Disable button so it can't be spammed
            btnSendData.setEnabled(false);

            DBManager dbManager = new DBManager(getBaseContext());
            FTPManager ftpManager = new FTPManager(getBaseContext(), dbManager, ViewIncidents.this);

            ftpManager.sendFile();
        }
    }

    //Informs user if the upload was successful
    public void finishedUpload(boolean success){
        btnSendData.setEnabled(true);

        if(success)
            Toast.makeText(getBaseContext(), "Uploaded data", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getBaseContext(), "No new data", Toast.LENGTH_LONG).show();
    }

    private class monthSelected implements OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //The number of the month that was selected
            int clickedMonth = position;

            //Set up and calculate how many days the selected month has
            Calendar myCal = new GregorianCalendar(2015, clickedMonth, Integer.valueOf(spinDay.getSelectedItem().toString()));
            int daysInMonth = myCal.getActualMaximum(Calendar.DAY_OF_MONTH);

            //Set up a new array for days
            days = new String[daysInMonth];

            //Add the values into the days array
            for (int i = 0; i < daysInMonth; i++)
            {
                String currDay = String.valueOf(i + 1);
                days[i] = currDay;
            }

            //TODO this is ugly, fix it
            //Update the spinner with the right amount of days for the month
            dayAdapter = new ArrayAdapter(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, days);
            spinDay.setAdapter(dayAdapter);
            dayAdapter.notifyDataSetChanged();

            populateList();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class daySelected implements OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
           populateList();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
