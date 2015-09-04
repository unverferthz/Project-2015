<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="../../favicon.ico">

    <title>Home</title>

    <!-- Bootstrap core CSS -->
    <link href="dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="starter-template.css" rel="stylesheet">
    
    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
  </head>

  <body>

    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">Roximal</a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li class="active"><a href="">Home</a></li>
            <li><a href="map.php">Map</a></li>
            <li><a href="graphs.php">Graphs</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </nav>

    <div class="container">
        
        <div class="mainContainer">
          <div class="textPadding">
            <h1>Welcome to Roximal</h1>
            <p>Roximal works by having cyclists wear a device while our cycling. The device will measure the distance between
              the cyclist and vehicle. If the vehicle is within 1.5 meters then the incident is recorded and the data
              can be seen here.</p>
          </div>
        </div>

        <!--
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
            while($row = mysql_fetch_row($result))
            {
              $id = $row[0];
              $distance = $row[1];
              $time = $row[2];
              $date = $row[3];
              $lat = $row[4];
              $lng = $row[5];

              echo("ID: $id");echo("<br/>");
              echo("Distance: $distance");echo("<br/>");
              echo("Time: $time");echo("<br/>");
              echo("Date: $date");echo("<br/>");
              echo("Latitude: $lat");echo("<br/>");
              echo("Longitude: $lng");echo("<br/>");

            }
        }
        mysql_free_result($result);
        ?>-->

    </div><!-- /.container -->


    <!-- Bootstrap core JavaScript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
    <script src="dist/js/bootstrap.min.js"></script>
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
    <script src="assets/js/ie10-viewport-bug-workaround.js"></script>
  </body>
</html>
