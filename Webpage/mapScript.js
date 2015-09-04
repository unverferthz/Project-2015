 var mapObject;

 var ajaxRequest = new XMLHttpRequest();
 

 ajaxRequest.onload = function(){
    //alert(this.responseText);

    var jsonResponse = JSON.parse(this.responseText);
    var incidents = jsonResponse["Incidents"];

    for(var i=0; i < incidents.length; i++)
    {
       // alert(incidents[i]["distance"]);
       var lat = incidents[i]["lat"];
       var lng = incidents[i]["lng"];
       var incidentLocation = new google.maps.LatLng(lat, lng);
       addMarker(incidentLocation, "Test");
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
    

    //ajaxRequest.open("get", "get-data.php", true);
    //ajaxRequest.send();
}

function addMarker(latLng, toolTipText) {

    var markerOption = {
        position: latLng,
        title: toolTipText
        };
    var markerObject = new google.maps.Marker(markerOption);
    markerObject.setMap(mapObject);
}


window.onload = init;