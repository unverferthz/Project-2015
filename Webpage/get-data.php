<?php
$host = "localhost";
$userMS = "root";
$passwordMS = "2015projectcz";

$connection = mysql_connect($host,$userMS,$passwordMS) or die("Couldn't connect:".mysql_error());

$database = "cyclingIncidents";
$db = mysql_select_db($database,$connection) or die("Couldn't select database");

//Pull all data out of incident table
$getIncidentQuery = "SELECT * FROM tblIncidents";
$result = mysql_query($getIncidentQuery);

//Check if there is any data
if(mysql_num_rows($result) > 0)
{
	$count = 0;
	echo("{\"Incidents\":[");
    while($row = mysql_fetch_row($result))
    {
      //$id = $row[0];
      if($count != 0)
      {
      	echo(",");
      }
      echo("{");

      $distance = $row[1];
      echo("\"distance\":\"$distance\"");
      echo(",");

      $time = $row[2];
      echo("\"time\":\"$time\"");
      echo(",");

      $date = $row[3];
      echo("\"date\":\"$date\"");
      echo(",");

      $lat = $row[4];
      echo("\"lat\":\"$lat\"");
      echo(",");

      $lng = $row[5];
      echo("\"lng\":\"$lng\"");

      echo("}");
      $count++;
    }
    echo("]}");
}
mysql_free_result($result);

?>