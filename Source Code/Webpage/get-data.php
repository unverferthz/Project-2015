<?php

//insert into tblTesting(testDate) values(STR_TO_DATE('19/04/2015', '%d/%m/%Y'));

$firstMonth = strip_tags($_GET['firstMonth']);
$firstDay = strip_tags($_GET['firstDay']);

$secondMonth = strip_tags($_GET['secondMonth']);
$secondDay = strip_tags($_GET['secondDay']);

$firstYear = strip_tags($_GET['firstYear']);
$secondYear = strip_tags($_GET['secondYear']);


$host = "localhost";
$userMS = "root";
$passwordMS = "2015projectcz";

$connection = mysql_connect($host,$userMS,$passwordMS) or die("Couldn't connect:".mysql_error());

$database = "cyclingIncidents";
$db = mysql_select_db($database,$connection) or die("Couldn't select database");

//Pull all data out of incident table
//$getIncidentQuery = "SELECT * FROM tblIncidents";
//SELECT * FROM tblTesting WHERE testDate BETWEEN '2015-4-13' AND '2015-4-17';
$getIncidentQuery = "SELECT * FROM tblIncidents WHERE date BETWEEN '$firstYear-$firstMonth-$firstDay' AND '$secondYear-$secondMonth-$secondDay'";
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