
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