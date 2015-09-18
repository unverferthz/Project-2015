 var mapObject;

 var ajaxRequest = new XMLHttpRequest();
 

//Function called from html when dropdown is changed
function month1Changed(selectedMonth){
  var currentYear = new Date().getFullYear()  
  var numDays = daysInMonth(selectedMonth,currentYear);

  var daySelector = document.getElementById("daySelector1");

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


  updateMonth2(selectedMonth);
}

function updateMonth2(selectedMonth){
  var month2Selector = document.getElementById("month2Selector");
  var numOptions = month2Selector.length;

  //Remove all of the children from the daySelector
  for(var i=0; i < numOptions; i++)
  {
    month2Selector.remove(0);
  }

  if(selectedMonth != 12)
  {
    //Offset allows the dropdown to include the same month as what was selected in the top dropdown
    var selectedMonthOffset = selectedMonth - 1;

    var monthsArray = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    var monthsLeft = 12 - selectedMonthOffset;

    var optionsArray = new Array(monthsLeft);

    var optionsArrayIndex = monthsLeft - 1;

    for(var i=12; i > selectedMonthOffset; i--)
    {
      var newOption = document.createElement("option");
      newOption.value = i;
      var textNode = document.createTextNode(monthsArray[i - 1]);
      newOption.appendChild(textNode);

      optionsArray[optionsArrayIndex] = newOption;
      optionsArrayIndex--;
    }

    for(var i=0; i < optionsArray.length; i++)
    {
      month2Selector.appendChild(optionsArray[i]);
    }
  }
  else
  {
    var newOption = document.createElement("option");
    newOption.value = i;
    var textNode = document.createTextNode("December");
    newOption.appendChild(textNode);

    month2Selector.appendChild(newOption);
  }

  month2Changed(selectedMonth, false);
}

function month2Changed(selectedMonth, cameFromForm){
  var currentYear = new Date().getFullYear()  
  var numDays = daysInMonth(selectedMonth,currentYear);

  var daySelector = document.getElementById("daySelector2");

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

  if(!cameFromForm)
  {
    daySelector.value = numDays;
  }
}

function setCurrentMonth(){
  var currentMonth = new Date().getMonth() + 1;

  var element = document.getElementById('month1Selector');
  element.value = currentMonth;

  month1Changed(currentMonth);
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

    ajaxRequest.open("get", "get-data.php", true);
    ajaxRequest.send();
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