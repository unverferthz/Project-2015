<?php
$host = "localhost";
$userMS = "root";
$passwordMS = "2015projectcz";
$database = "cyclingIncidents";
$connection = mysqli_connect($host,$userMS,$passwordMS,$database);

if(!$connection){
	die("Connection failed: " . mysqli_connect_error());
}

$sql = "CREATE TABLE IF NOT EXISTS tblIncidents (
incidentID INT AUTO_INCREMENT PRIMARY KEY,
distance INT NOT NULL,
time TEXT NOT NULL, 
date DATE NOT NULL,
latitude TEXT NOT NULL,
longitude TEXT NOT NULL
)";
echo("creating table");

if(mysqli_query($connection,$sql)) {
	echo "TAble Myguests created successfully";
} else {
	echo "Error creating table: " . mysqli_error($connection);
}

mysqli_close($connection);
?>
