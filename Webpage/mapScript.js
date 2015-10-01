//Global variables
var mapObject;
var markersArray = [];
var ajaxRequest = new XMLHttpRequest();
var currIncidents = [];
 

function incident(lat, lng, distance, date, time)
{
  this.lat = lat;
  this.lng = lng;
  this.distance = distance;
  this.date = date;
  this.time = time;
}

//Function called from html when dropdown is changed
//Updates the first day dropdown box
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

  updateMapWithNewValues();
}


//Update the number of days in 2nd day dropdown
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

  daySelector.value = numDays;

  updateMapWithNewValues();
}

function dayChanged(){
  updateMapWithNewValues();
}

function timeChanged(){
  
  var sortStatusElement = document.getElementById("sortStatus");

  clearMarkers();

  var time1Element = document.getElementById('time1');
  var time1AmPmElement = document.getElementById('time1AmPm');
  var time2Element = document.getElementById('time2');
  var time2AmPmElement = document.getElementById('time2AmPm');

  var time1 = parseInt(time1Element.value);
  var time1AmPm =  time1AmPmElement.value;
  var time2 = parseInt(time2Element.value);
  var time2AmPm = time2AmPmElement.value;
  
  if (time1 == 0) {
    sortStatusElement.innerHTML = "Showing incidents from " + 12 + " " + time1AmPm + " - " + time2.toString() + " " + time2AmPm;
  }
  else if (time2 == 0) {
    sortStatusElement.innerHTML = "Showing incidents from " + time1.toString() + " " + time1AmPm + " - " + 12 + " " + time2AmPm;
  }
  else{
    sortStatusElement.innerHTML = "Showing incidents from " + time1.toString() + " " + time1AmPm + " - " + time2.toString() + " " + time2AmPm;
  }

  if (time1AmPm == "PM"){
    time1 += 12
  }
  if (time2AmPm == "PM") {
    time2 += 12;
  }
  
  //new Date(year, month, day, hours, minutes, seconds, milliseconds);
  var time1DateObject = new Date(2015, 1, 1,time1, 0, 0, 0);
  var time2DateObject = new Date(2015, 1, 1,time2, 0, 0, 0);
  
  if (time1DateObject > time2DateObject) {
    var newDay = 2;
    time2DateObject = new Date(2015, 1, newDay, time2, 0, 0, 0);
  }
  
  if (time1 == time2) {
    sortStatusElement.innerHTML = "Showing all incidents";
  }
  
  for(var i=0; i < currIncidents.length; i++)
  {
    var currIncident = currIncidents[i];
    var incidentTime = currIncident.time;
    
    var splitTimeAndAmPm = incidentTime.split(" ");

    var incidentAmPm = splitTimeAndAmPm[1];
    var timeSplit = splitTimeAndAmPm[0].split(":");
    var incidentHour = parseInt(timeSplit[0]);
    
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
    
    //Make incident date object
    if (incidentHour < time1 && time1DateObject > time2DateObject) {
      //new Date(year, month, day, hours, minutes, seconds, milliseconds);
      incidentDateObject = new Date(2015, 1, 2, incidentHour, 0, 0, 0);
    }
    else{
      incidentDateObject = new Date(2015, 1, 1, incidentHour, 0, 0, 0);
    }
    
    var incidentLocation = new google.maps.LatLng(currIncident.lat, currIncident.lng);
    
    //Showing all, using time instead of the date object because it wasn't returning true for same dates
    if (time1 == time2) {
      addMarker(incidentLocation, "Distance: " + currIncident.distance + "cm" + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
    }
    else if (incidentDateObject >= time1DateObject && incidentDateObject < time2DateObject) {
      addMarker(incidentLocation, "Distance: " + currIncident.distance + "cm" + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
    }
  }
}

function updateMapWithNewValues(){
  var month1Selector = document.getElementById('month1Selector');
  var month2Selector = document.getElementById('month2Selector');
  var day1Selector = document.getElementById('day1Selector');
  var day2Selector = document.getElementById('day2Selector');

  var month1 = parseInt(month1Selector.value);
  var month2 = parseInt(month2Selector.value);
  var day1 = day1Selector.value;
  var day2 = day2Selector.value;

  var firstDateFirst = true;

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

  if(firstDateFirst)
  {
    loadNewMarkers(month1, day1, month2, day2, currentYear, currentYear);
  }
  else
  {
    loadNewMarkers(month2, day2, month1, day1, currentYear, currentYear);
  }
}

//Set month dropdown to current month
function setCurrentMonth(){
  var currentMonth = new Date().getMonth() + 1;

  var month1Selector = document.getElementById('month1Selector');
  month1Selector.value = currentMonth;

  var month2Selector = document.getElementById('month2Selector');
  month2Selector.value = currentMonth;

  month1Changed(currentMonth);
  month2Changed(currentMonth);
}

//Returns number of days in the month. Janurary = 1
function daysInMonth(month,year) {
    return new Date(year, month, 0).getDate();
}


//Function that runs when the ajax request returns
 ajaxRequest.onload = function(){
    //alert(this.responseText);

    var jsonResponse = JSON.parse(this.responseText);
    var incidents = jsonResponse["Incidents"];

    for(var i=0; i < incidents.length; i++)
    {
      var currLat = incidents[i]["lat"];
      var currLng = incidents[i]["lng"];
      var incidentLocation = new google.maps.LatLng(currLat, currLng);

       
      var currDistance = incidents[i]["distance"];
      var currTime = incidents[i]["time"];
      var currDate = incidents[i]["date"];

      if(currLat != "null" && currLng != "null")
      {
        currIncidents.push(new incident(currLat, currLng, currDistance, currDate, currTime));
        addMarker(incidentLocation, "Distance: " + currDistance + "cm" + "  Time: " + currTime + "  Date: " + currDate);
      }
    }
 }

//Add marker onto map
function addMarker(latLng, incidentInfo) {

  //var distanceInMeters = distance / 100;

  var infowindow = new google.maps.InfoWindow({
    content: incidentInfo
  });

  var markerOption = {
      position: latLng,
      title: incidentInfo,
      size: new google.maps.Size(1, 1)
      };
  var markerObject = new google.maps.Marker(markerOption);

  google.maps.event.addListener(markerObject, 'click', function() {
      infowindow.open(mapObject,markerObject);
    });

  /*var circle = new google.maps.Circle({
  map: mapObject,
  radius: distanceInMeters,
  fillColor: '#AA0000'
  });
  circle.bindTo('center', markerObject, 'position');*/

  markerObject.setMap(mapObject);

  markersArray.push(markerObject);
}


//Removes existing markers from the map
function clearMarkers(){
  //Remove markers from map
  for(var i=0; i < markersArray.length; i++)
  {
    markersArray[i].setMap(null);
  }

  //Reset marker array
  markersArray = [];
}

function loadNewMarkers(firstMonth, firstDay, secondMonth, secondDay, firstYear, secondYear)
{
  clearMarkers();

  ajaxRequest.open("get", "get-data.php?firstMonth=" + firstMonth + "&firstDay=" + firstDay + "&secondMonth=" + secondMonth +
    "&secondDay=" + secondDay + "&firstYear=" + firstYear + "&secondYear=" + secondYear, true);

  //ajaxRequest.open("get", "get-data.php", true);
  ajaxRequest.send();
}

function init() {
  var mapCanvas = document.getElementById("mapArea");
                                      //Lat&Long for Dunedin
  var mapCentre = new google.maps.LatLng(-45.874036, 170.503566);
  var mapType = google.maps.MapTypeId.ROADMAP;
  var mapOptions = {  center: mapCentre,
                      zoom: 14,
                      mapTypeId: mapType};
                      
  mapObject = new google.maps.Map(mapCanvas, mapOptions);

  setCurrentMonth();

  var time1 = document.getElementById('time1');
  time1.value = 0;

  var time2 = document.getElementById('time2');
  time2.value = 0;

  var currentMonth = new Date().getMonth() + 1;
  var firstDay = "1";
  var currentYear = new Date().getFullYear();
  var secondDay = daysInMonth(currentMonth,currentYear);

  loadNewMarkers(currentMonth, firstDay, currentMonth, secondDay, currentYear, currentYear);

  updateMapWithNewValues();
}

window.onload = init;