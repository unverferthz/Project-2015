//Global variables
var mapObject;
var markersArray = [];
var ajaxRequest = new XMLHttpRequest();
var currIncidents = [];
var loadNewIncidents;
 
//Model for holding incidents
function incident(lat, lng, distance, date, time)
{
  this.lat = lat;
  this.lng = lng;
  this.distance = distance;
  this.date = date;
  this.time = time;
}

/*Pre-condition: None */
/*Post-condition: Called from html's dropdown. Updates the first day dropdown box */
function month1Changed(selectedMonth){
  var currentYear = new Date().getFullYear()  
  var numDays = daysInMonth(selectedMonth,currentYear);

  var daySelector = document.getElementById("day1Selector");

  var numOptions = daySelector.length;

  //Remove all of the children from the daySelector
  for(var i=0; i < numOptions; i++)
  {
    daySelector.remove(0);
  }

  //Loop over all the number of days filling up the list
  for(var i=0; i < numDays; i++)
  {
    var dayNumber = i+1;

    var newOption = document.createElement("option");
    newOption.value = dayNumber;
    var textNode = document.createTextNode(dayNumber);
    newOption.appendChild(textNode);

    daySelector.appendChild(newOption);
  }
  
  loadNewIncidents = true;
}


/*Pre-condition: None */
/*Post-condition: Called from html's dropdown. Updates the second day dropdown box */
function month2Changed(selectedMonth){
  var currentYear = new Date().getFullYear();
  var numDays = daysInMonth(selectedMonth,currentYear);

  var daySelector = document.getElementById("day2Selector");

  var numOptions = daySelector.length;

  //Remove all of the children from the daySelector
  for(var i=0; i < numOptions; i++)
  {
    daySelector.remove(0);
  }

  //Loop over all the number of days filling up the day dropdown
  for(var i=0; i < numDays; i++)
  {
    var dayNumber = i+1;

    var newOption = document.createElement("option");
    newOption.value = dayNumber;
    var textNode = document.createTextNode(dayNumber);
    newOption.appendChild(textNode);

    daySelector.appendChild(newOption);
  }

  daySelector.value = numDays;
  loadNewIncidents = true;
}

/*Pre-condition: None */
/*Post-condition: Called from html day dropdown. Sets flag to load new incidents from webpage */
function dayChanged() {
  loadNewIncidents = true;
}

/*Pre-condition: None */
/*Post-condition: Gets selected times from dropdown. Checks against current loaded incidents to see if they should be added
to the map*/
//TODO rename
function timeCheck(){
  clearMarkers();

  //Get times selected by user
  var time1Element = document.getElementById('time1');
  var time1AmPmElement = document.getElementById('time1AmPm');
  var time2Element = document.getElementById('time2');
  var time2AmPmElement = document.getElementById('time2AmPm');

  var time1 = parseInt(time1Element.value);
  var time1AmPm =  time1AmPmElement.value;
  var time2 = parseInt(time2Element.value);
  var time2AmPm = time2AmPmElement.value;
  
  //Check if times were PM and add 12 hours onto it to compensate
  if (time1AmPm == "PM"){
    time1 += 12
  }
  if (time2AmPm == "PM") {
    time2 += 12;
  }
  
  //Create date objects to compare against
  var time1DateObject = new Date(2015, 1, 1,time1, 0, 0, 0); //Date(year, month, day, hours, minutes, seconds, milliseconds);
  var time2DateObject = new Date(2015, 1, 1,time2, 0, 0, 0);
  
  //Check if 2nd date object needs adjusted. Two different dates for when time1 is greater than time2 so it properly
  //checks between the right days
  //e.g time1: 7pm, time2: 3am, this checks between 7pm-3am rather than 3am-7pm
  if (time1DateObject > time2DateObject)
  {
    var newDay = 2;
    time2DateObject = new Date(2015, 1, newDay, time2, 0, 0, 0);
  }
  
  //Loop over all incidents
  for(var i=0; i < currIncidents.length; i++)
  {
    var currIncident = currIncidents[i];
    
    //Get current incidents information
    var incidentTime = currIncident.time;
    var splitTimeAndAmPm = incidentTime.split(" ");

    //Split time
    var incidentAmPm = splitTimeAndAmPm[1];
    var timeSplit = splitTimeAndAmPm[0].split(":");
    var incidentHour = parseInt(timeSplit[0]);
    
    //Split date
    var dateSplit = currIncident.date.split("-");
    var year = dateSplit[0];
    var month = dateSplit[1];
    var day = dateSplit[2];
    
    //Date format that's easier for users in NZ to read
    var newDateFormat = day + "-" + month + "-" + year;
    
    //If time for incident is PM, add 12 hours to compensate
    if (incidentAmPm == "PM")
    {
      incidentHour += 12;
    }
    else
    {
      if (incidentHour == 12) {
        incidentHour = 0;
      }
    }
    
    var incidentDateObject;
    
    //Make incident date object. Two different dates for when time1 is greater than time2 so it properly checks between the right days
    //e.g time1: 7pm, time2: 3am, this checks between 7pm-3am rather than 3am-7pm
    if (incidentHour < time1 && time1DateObject > time2DateObject) {
      incidentDateObject = new Date(2015, 1, 2, incidentHour, 0, 0, 0);//(year, month, day, hours, minutes, seconds, milliseconds)
    }
    else{
      incidentDateObject = new Date(2015, 1, 1, incidentHour, 0, 0, 0);
    }
    
    var incidentLocation = new google.maps.LatLng(currIncident.lat, currIncident.lng);
    
    //Showing all, using time instead of the date object because it wasn't returning true for same dates
    if (time1 == time2) {
      //Check to see if the incident is between the users selected distances
      if(distanceCheck(currIncident.distance))
      {
        addMarker(incidentLocation, "Distance: " + currIncident.distance + "cm" + "  Time: " + currIncident.time + "  Date: " + newDateFormat);
      }
    }
    //Check to make sure the incident falls into the correct time
    else if (incidentDateObject >= time1DateObject && incidentDateObject < time2DateObject) {
      //Check to see if the incident is between the users selected distances
      if(distanceCheck(currIncident.distance))
      {
        addMarker(incidentLocation, "Distance: " + currIncident.distance + "cm" + "  Time: " + currIncident.time + "  Date: " + newDateFormat);
      }
    }
  }
}

/*Pre-condition: Only works if user entered a distance that is a number */
/*Post-condition: Gets distances entered by user and checks to see whether distance passed in is between user
 entered info, returns true if it is and false if not*/
function distanceCheck(distance){
  //Get the distances the user entered
  var distanceNum = parseInt(distance);
  var limit1Element = document.getElementById("llimit",10);
  var limit1 = parseInt(limit1Element.value);
  var limit2Element = document.getElementById("ulimit",10);
  var limit2 = parseInt(limit2Element.value);
  
  //Check which order the distances need to be arranged in so the lower number goes in the correct position for the comparison
  if(limit1 > limit2)
  {
    var ulimit = limit1;
    var llimit = limit2;
  }
  if(limit1 < limit2)
  {
    var llimit = limit1;
    var ulimit = limit2;
  }	
  if(limit1 == limit2)
  {
    var llimit = limit1;
    var ulimit = limit2;
  }
  
  //Check if the distance is between the users entered distance and return true or false if it is or isn't
  if(distanceNum >= llimit && distanceNum <= ulimit)
  {
    return true;
  }
  else if(isNaN(limit1) || isNaN(limit2))
  {
    return false;
  }
  else
  {
    return false;
  }
}

/*Pre-condition: None */
/*Post-condition: Set up and execute function to load in new markers from ajax */
function updateMapWithNewValues(){
  //Get the user selected months and days
  var month1Selector = document.getElementById('month1Selector');
  var month2Selector = document.getElementById('month2Selector');
  var day1Selector = document.getElementById('day1Selector');
  var day2Selector = document.getElementById('day2Selector');

  var month1 = parseInt(month1Selector.value);
  var month2 = parseInt(month2Selector.value);
  var day1 = day1Selector.value;
  var day2 = day2Selector.value;

  var firstDateFirst = true;

  //Check which date needs to go first in the comparison
  if(month1 == month2)
  {
    if(day1 > day2)
    {
      firstDateFirst = false;
    }
  }
  else if(month1 > month2)
  {
    firstDateFirst = false;
  }

  var currentYear = new Date().getFullYear();

  //Execute function to load new markers through ajax
  if(firstDateFirst)
  {
    loadNewMarkers(month1, day1, month2, day2, currentYear, currentYear);
  }
  else
  {
    loadNewMarkers(month2, day2, month1, day1, currentYear, currentYear);
  }
}

/*Pre-condition: None */
/*Post-condition: Function that runs when the ajax request returns. Looks through the json and creates incidents from them
to store in global array*/
 ajaxRequest.onload = function(){
  //Read in the JSON from the PHP file
  var jsonResponse = JSON.parse(this.responseText);
  var incidents = jsonResponse["Incidents"];

  for(var i=0; i < incidents.length; i++)
  {
    //Parse through the JSON to get all the information of the incidents
    var currLat = incidents[i]["lat"];
    var currLng = incidents[i]["lng"];
    var incidentLocation = new google.maps.LatLng(currLat, currLng);

    var currDistance = incidents[i]["distance"];
    var currTime = incidents[i]["time"];
    var currDate = incidents[i]["date"];

    //If the location wasn't null, put all the information together to create an incident and store it into the array
    if(currLat != "null" && currLng != "null")
    {
      currIncidents.push(new incident(currLat, currLng, currDistance, currDate, currTime));
    }
  }
    //time check
    //TODO rename timeCheck
    timeCheck();
 }
 
/*Pre-condition: None */
/*Post-condition: Create and send ajax request to load in new incidents*/
function loadNewMarkers(firstMonth, firstDay, secondMonth, secondDay, firstYear, secondYear)
{
  clearMarkers();

  //Put together the URL to get the information from the PHP file
  ajaxRequest.open("get", "get-data.php?firstMonth=" + firstMonth + "&firstDay=" + firstDay + "&secondMonth=" + secondMonth +
    "&secondDay=" + secondDay + "&firstYear=" + firstYear + "&secondYear=" + secondYear, true);

  //ajaxRequest.open("get", "get-data.php", true);
  
  //Send the request
  ajaxRequest.send();
}

/*Pre-condition: None */
/*Post-condition: Add marker onto map from passed in information*/
function addMarker(latLng, incidentInfo) {
  var infowindow = new google.maps.InfoWindow({
    content: incidentInfo
  });

  //Create marker
  var markerOption = {
      position: latLng,
      title: incidentInfo,
      size: new google.maps.Size(1, 1)
      };
  
  var markerObject = new google.maps.Marker(markerOption);

  //Add info window onto marker to display the information
  google.maps.event.addListener(markerObject, 'click', function() {
      infowindow.open(mapObject,markerObject);
    });

  //Add marker onto map and array
  markerObject.setMap(mapObject);
  markersArray.push(markerObject);
}

/*Pre-condition: None */
/*Post-condition: Resets markers to be ready for new ones by clearing map and emptying array*/
function clearMarkers(){
  //Remove markers from map
  for(var i=0; i < markersArray.length; i++)
  {
    markersArray[i].setMap(null);
  }

  //Reset marker array
  markersArray = [];
}

/*Pre-condition: None */
/*Post-condition: Set month dropdown to current month */
function setCurrentMonth(){
  var currentMonth = new Date().getMonth() + 1;

  var month1Selector = document.getElementById('month1Selector');
  month1Selector.value = currentMonth;

  var month2Selector = document.getElementById('month2Selector');
  month2Selector.value = currentMonth;

  //Have months change their related day dropdowns to have the correct number of days
  month1Changed(currentMonth);
  month2Changed(currentMonth);
}


/*Pre-condition: Month is 0 based January = 0 */
/*Post-condition: Returns number of days in the month. Janurary = 1*/
function daysInMonth(month,year) {
  return new Date(year, month, 0).getDate();
}

/*Pre-condition: None */
/*Post-condition: Button handler. Loads new incidents if needed, and gets them added to the map*/
function buttonUpdateValues(){
  //Clear old markers
  clearMarkers();
  
  //Load new markers if needed
  if (loadNewIncidents) {
    currIncidents = [];
    updateMapWithNewValues();
  }
  //Else, go over current markers and add the right ones to the map
  else
  {
    timeCheck();
  }
  
  loadNewIncidents = false;
}

/*Pre-condition: None */
/*Post-condition: Initialize everything needed*/
function init() {
  loadNewIncidents = false;
  
  //Load google map
  var mapCanvas = document.getElementById("mapArea");
                                      //Lat&Long for Dunedin
  var mapCentre = new google.maps.LatLng(-45.874036, 170.503566);
  var mapType = google.maps.MapTypeId.ROADMAP;
  var mapOptions = {  center: mapCentre,
                      zoom: 14,
                      mapTypeId: mapType};
                      
  mapObject = new google.maps.Map(mapCanvas, mapOptions);

  //Set month dropdowns to current month
  setCurrentMonth();
  
  //Set default values for controls
  var time1 = document.getElementById('time1');
  time1.value = 0;

  var time2 = document.getElementById('time2');
  time2.value = 0;
  
  var dis1Box = document.getElementById('llimit');
  dis1Box.value = "30";
  var dis2Box = document.getElementById('ulimit');
  dis2Box.value = "150";

  var currentMonth = new Date().getMonth() + 1;
  var firstDay = "1";
  var currentYear = new Date().getFullYear();
  var secondDay = daysInMonth(currentMonth,currentYear);
  
  //Set up button
  var buttonClick = document.getElementById("btnCheckDistance");
  buttonClick.onclick = buttonUpdateValues;
  
  loadNewMarkers(currentMonth, firstDay, currentMonth, secondDay, currentYear, currentYear);

  updateMapWithNewValues();
}

//Get everything initialized on page load
window.onload = init;
