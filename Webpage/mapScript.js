 var mapObject;

 var ajaxRequest = new XMLHttpRequest();
 

//Function called from html when dropdown is changed
function monthChanged(selectedMonth){
  var currentYear = new Date().getFullYear()  
  var numDays = daysInMonth(selectedMonth,currentYear);


  var daySelector = document.getElementById("daySelector");

  var numOptions = daySelector.length;

  //Remove all of the children from the daySelector
  for(var i=0; i < numOptions; i++)
  {
    daySelector.remove(0);
  }
  

  var newAllOption = document.createElement("option");
  newAllOption.value = "All";
  var allTextNode = document.createTextNode("All");
  newAllOption.appendChild(allTextNode);
  daySelector.appendChild(newAllOption);

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
  
}

function setCurrentMonth(){
  var currentMonth = new Date().getMonth() + 1;

  var element = document.getElementById('monthSelector');
  element.value = currentMonth;

  monthChanged(currentMonth);
}

//Month is 1 based
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
       var lat = incidents[i]["lat"];
       var lng = incidents[i]["lng"];
       var incidentLocation = new google.maps.LatLng(lat, lng);

       
       var distance = incidents[i]["distance"];
       var time = incidents[i]["time"];
       var date = incidents[i]["date"];

       addMarker(incidentLocation, "  Distance: " + distance + "  Time: " + time + "  Date: " + date);
    }
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
    time1.value = 12;

    var time2 = document.getElementById('time2');
    time2.value = 12;

    //ajaxRequest.open("get", "get-data.php", true);
    //ajaxRequest.send();
}

function addMarker(latLng, incidentInfo) {

    var infowindow = new google.maps.InfoWindow({
      content: incidentInfo
    });

    var markerOption = {
        position: latLng,
        title: incidentInfo
        };
    var markerObject = new google.maps.Marker(markerOption);

     google.maps.event.addListener(markerObject, 'click', function() {
          infowindow.open(mapObject,markerObject);
        });

    markerObject.setMap(mapObject);
}


window.onload = init;