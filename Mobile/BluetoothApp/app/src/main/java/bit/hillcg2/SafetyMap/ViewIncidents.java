package bit.hillcg2.SafetyMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
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
import bit.hillcg2.SafetyMap.Managers.DBManager;
import bit.hillcg2.SafetyMap.Managers.FTPManager;
import bit.hillcg2.SafetyMap.Models.Incident;


public class ViewIncidents extends Activity {
    //Globals
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

    //Pre-condition: None
    //Post-condition: Initialize everything
    public void init(){
        //Get instances of views from layout and set them up
        Button btnBack = (Button)findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new goBack());

        btnSendData = (Button)findViewById(R.id.btnSendData);
        btnSendData.setOnClickListener(new sendData());

        listAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
        ListView incidentList = (ListView)findViewById(R.id.incidentList);
        incidentList.setAdapter(listAdapter);

        spinMonth = (Spinner)findViewById(R.id.spinIncidentMonth);
        spinDay = (Spinner)findViewById(R.id.spinIncidentDay);

        Calendar cal = Calendar.getInstance();

        //Get the current month and day to set initial values into spinners
        int currMonth = cal.get(Calendar.MONTH);

        //Find out how many days the current month has
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysArraySize = daysInMonth + 1;
        days = new String[daysArraySize];
        days[0] = "All";

        //Set up array to have all the days of the current month to use for spinner
        for(int i=0; i < daysInMonth; i++)
        {
            String currDay = String.valueOf(i + 1);
            days[i + 1] = currDay;
        }

        //Array holding all months to use for spinner
        months = new String[]{"All", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        //Set up adapters for the spinners
        monthAdapter = new ArrayAdapter(this, R.layout.spinner_item, months);
        dayAdapter = new ArrayAdapter(this, R.layout.spinner_item, days);

        //Set spinners adapters
        spinMonth.setAdapter(monthAdapter);
        spinDay.setAdapter(dayAdapter);

        //Set up spinners with their initial values
        spinMonth.setSelection(currMonth + 1, false);
        spinDay.setSelection(0, false);

        spinDay.setOnItemSelectedListener(new daySelected());
        spinMonth.setOnItemSelectedListener(new monthSelected());

        //Get instance of database
        dbManager = new DBManager(getBaseContext());
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        //Set custom height of listview depending on screen height
        incidentList.getLayoutParams().height = (int)Math.round(height*0.74);

        //Populate listview with existing incidents
        populateList();
    }

    //Pre-condition: None
    //Post-condition: Reads in incidents from database and puts them into listview
    public void populateList(){
        listAdapter.clear();

        //Get the date values from spinners
        String selectedMonth = String.valueOf(spinMonth.getSelectedItemPosition());
        String selectedDay = spinDay.getSelectedItem().toString();

        //Ask database for all of the incidents
        ArrayList<Incident> incidentArray = dbManager.getAllIncidents();

        int counter = 0;

        //Loop over all of the incidents
        for(Incident i : incidentArray){
            counter++;
            //Get the date value to check if it's the same as spinner values
            String date = i.getDate();

            //Split it apart for comparison
            String[] splitDate = date.split("/");
            String currMonth = splitDate[1];
            String currDay = splitDate[2];

            //Show all months
            if(selectedMonth.equals("0")) {
                addIncidentToList(i, counter);
            }
            //Do all days for a month
            else if(currMonth.equals(selectedMonth) && selectedDay.equals("All"))
            {
                addIncidentToList(i, counter);
            }
            //Check if month and day are the same for incident and spinner values
            else if(currMonth.equals(selectedMonth) && currDay.equals(selectedDay))
            {
                addIncidentToList(i, counter);
            }
        }
    }

    //Pre-condition: None
    //Post-condition: Adds a single incident into the listview
    public void addIncidentToList(Incident i, int counter){
        //Built up string to put into list to display incidents to use
        String textForList = "Incident " + counter + ":\n";

        textForList += "Distance: " + i.getDistance() + "cm\n";
        textForList += "Time: " + i.getTime() + "\n";
        textForList += "Date: " + i.getDate() + "\n";
        textForList += "Latitude: " + i.getLat() + "\n";
        textForList += "Longitude: " + i.getLng();

        //Add into adapter and update so it can be seen
        listAdapter.add(textForList);
        listAdapter.notifyDataSetChanged();
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

            Toast.makeText(getBaseContext(), "Sending...", Toast.LENGTH_LONG).show();

            DBManager dbManager = new DBManager(getBaseContext());
            FTPManager ftpManager = new FTPManager(getBaseContext(), dbManager, ViewIncidents.this);

            ftpManager.sendFile();
        }
    }

    //Pre-condition: None
    //Post-condition: Informs user if the upload was successful
    public void finishedUpload(String uploadMessage){
        btnSendData.setEnabled(true);
        Toast.makeText(getBaseContext(), uploadMessage, Toast.LENGTH_LONG).show();
    }

    //Handler for the month drop down box
    private class monthSelected implements OnItemSelectedListener {
        //Pre-condition: None
        //Post-condition: Displays incidents for the selected month
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //The number of the month that was selected
            int clickedMonth = position;

            //If month selected = 0, show all months
            if (clickedMonth != 0)
            {
                //Set up and calculate how many days the selected month has
                Calendar myCal = new GregorianCalendar(2015, clickedMonth, 1);
                int daysInMonth = myCal.getActualMaximum(Calendar.DAY_OF_MONTH);

                //Set up a new array for days
                int daysArrayLength = daysInMonth + 1;
                days = new String[daysArrayLength];
                days[0] = "All";

                //Add the values into the days array
                for (int i = 0; i < daysInMonth; i++)
                {
                    String currDay = String.valueOf(i + 1);
                    days[i + 1] = currDay;
                }
            }
            else
            {
                days = new String[1];
                days[0] = "All";
            }

            //Update the spinner with the right amount of days for the month
            dayAdapter = new ArrayAdapter(getBaseContext(), R.layout.spinner_item, days);
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
