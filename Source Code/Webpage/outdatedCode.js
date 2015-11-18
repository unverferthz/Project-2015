
//Updates the second day dropdown box
//Removes unneeded months from second month dropdown
function updateMonth2(selectedMonth){
  var month2Selector = document.getElementById("month2Selector");
  
  /*var numOptions = month2Selector.length;

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
  }*/

  month2Changed(selectedMonth, false);
}


/***************           Still need to add in incident AM/PM use          ****************/
    if(time1AmPm == time2AmPm)
    {
      //check time
      if(time1 == time2)
      {
        //show everything
        addMarker(incidentLocation, "Distance: " + currIncident.distance + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
      }
      else if(time1 > time2)
      {
        if(time2 == 12)
        {
          time2 = 0;
        }

        //check if incident time is smaller than time1 and higher than time2
        if(incidentHour <= time1 && incidentHour >= time2)
        {
          //show marker
          addMarker(incidentLocation, "Distance: " + currIncident.distance + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
        }
      }
      else
      {
        if(time1 == 12)
        {
          time1 = 0;
        }

        if(incidentHour >= time1 && incidentHour <= time2)
        {
          //show marker
          addMarker(incidentLocation, "Distance: " + currIncident.distance + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
        }
      }
    }
    //time1 is greater
    else if(time1AmPm > time2AmPm)
    {
      if(time2 == 12)
        {
          time2 = 0;
        }

      if(incidentHour <= time1 && incidentHour >= time2)
      {
        //show marker
        addMarker(incidentLocation, "Distance: " + currIncident.distance + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
      }
    }
    else
    {
      if(time1 == 12)
      {
        time1 = 0;
      }

      if(incidentHour >= time1 && incidentHour <= time2)
      {
        //show marker
        addMarker(incidentLocation, "Distance: " + currIncident.distance + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
      }
    }
    
    //new
    if (time1AmPm == "PM")
  {
    time1 += 12;
  }
  else
  {
    if (time1 == 12) {
      time1 = 0;
    }
  }
  if (time2AmPm == "PM")
  {
    time2 += 12;
  }
  else
  {
    if (time2 == 12) {
      time2 = 0;
    }
  }
    
  var time1DateObject = new Date(2015, 1, 1,time1, 0, 0, 0);
  var time2DateObject = new Date(2015, 1, 1,time2, 0, 0, 0);
  
  for(var i=0; i < currIncidents.length; i++)
  {
    var currIncident = currIncidents[i];
    var incidentTime = currIncident.time;

    var splitTimeAndAmPm = incidentTime.split(" ");

    var incidentAmPm = splitTimeAndAmPm[1];
    var timeSplit = splitTimeAndAmPm[0].split(":");
    var incidentHour = parseInt(timeSplit[0]);
    
    //new Date(year, month, day, hours, minutes, seconds, milliseconds);
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
    var incidentDateObject = new Date(2015, 1, 1, incidentHour, parseInt(timeSplit[1]), 0, 0);
    
    var incidentLocation = new google.maps.LatLng(currIncident.lat, currIncident.lng);
    
    //If times are the same, show everything
    if (time1DateObject == time2DateObject)
    {
      addMarker(incidentLocation, "Distance: " + currIncident.distance + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
    }
    //Check which time user entered has the smaller value to know which order to compare against
    else if (time1DateObject > time2DateObject)
    {
      //Check if incident time is between two selected times
      if (incidentDateObject < time1DateObject && incidentDateObject >= time2DateObject)
      {
        addMarker(incidentLocation, "Distance: " + currIncident.distance + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
      }
    }
    else
    {
      var test1 = false
      var test2 = false;
      
      if (incidentDateObject => time1DateObject) {
        test1 = true;
      }
      
      if (incidentDateObject < time2DateObject) {
        test2 = true;
      }
      
      //Check if incident time is between two selected times
      if (test1 && test2)
      {
        addMarker(incidentLocation, "Distance: " + currIncident.distance + "  Time: " + currIncident.time + "  Date: " + currIncident.date);
      }
    }
  }
    
    