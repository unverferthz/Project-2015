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

    <title>Map</title>

    <!-- Bootstrap core CSS -->
    <link href="dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="styleSheet.css" rel="stylesheet">

    <script src="http://maps.googleapis.com/maps/api/js?key=AIzaSyBWLnRuuzxeiVoTpRDVFXewSdHgVTbOHuY&sensor=false"></script>
    <script src="mapScript.js"></script>

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
          <a href="index.php">
            <img id="logo" src="../pictures/Roximal_logo.png" width=90px height=35px />
          </a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li><a href="index.php">Home</a></li>
            <li class="active"><a href="">Map</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </nav>

    <div class="mainContainer">
      <div class="textPadding">
        <h1>Map</h1>
        <br>
        <form action='map.php' method='POST'>
        <div id='leftSortingBox'>
          <p class='alignRight'>Date between:
            <select id='month1Selector' name='monthSelector' onchange="month1Changed(this.value);">
              <!--Change to select current month automatically-->
              <option value='1'>January</option>
              <option value='2'>February</option>
              <option value='3'>March</option>
              <option value='4'>April</option>
              <option value='5'>May</option>
              <option value='6'>June</option>
              <option value='7'>July</option>
              <option value='8'>August</option>
              <option value='9'>September</option>
              <option value='10'>October</option>
              <option value='11'>November</option>
              <option value='12'>December</option>
            </select>

            <select id='daySelector1' name='daySelector' onchange="dayChanged(this.value);">
            </select>
          </p>
          <p class='alignRight'>
              and

            <select id='month2Selector' name='monthSelector' onchange="month2Changed(this.value, true);">
              <!--Change to select current month automatically-->
              <option value='1'>January</option>
              <option value='2'>February</option>
              <option value='3'>March</option>
              <option value='4'>April</option>
              <option value='5'>May</option>
              <option value='6'>June</option>
              <option value='7'>July</option>
              <option value='8'>August</option>
              <option value='9'>September</option>
              <option value='10'>October</option>
              <option value='11'>November</option>
              <option value='12'>December</option>
            </select>

            <select id='daySelector2' name='daySelector' onchange="dayChanged(this.value);">
            </select>
          </p>
        </div>
        <div id='rightSortingBox'>
          <p class='alignRight'>
            Time between:
            <select id='time1' name='time1' onchange="timeChanged();">
              <option value='1'>1</option>
              <option value='2'>2</option>
              <option value='3'>3</option>
              <option value='4'>4</option>
              <option value='5'>5</option>
              <option value='6'>6</option>
              <option value='7'>7</option>
              <option value='8'>8</option>
              <option value='9'>9</option>
              <option value='10'>10</option>
              <option value='11'>11</option>
              <option value='12'>12</option>
            </select>

            <select id='time1AmPm' name='time1AmPm' onchange="timeChanged();">
              <option value='am'>am</option>
              <option value='pm'>pm</option>
            </select>
          </p>
          <p class='alignRight'>
            and

            <select id='time2' name='time2' onchange="timeChanged();">
             <option value='1'>1</option>
              <option value='2'>2</option>
              <option value='3'>3</option>
              <option value='4'>4</option>
              <option value='5'>5</option>
              <option value='6'>6</option>
              <option value='7'>7</option>
              <option value='8'>8</option>
              <option value='9'>9</option>
              <option value='10'>10</option>
              <option value='11'>11</option>
              <option value='12'>12</option>
            </select>

            <select id='time2AmPm' name='time2AmPm' onchange="timeChanged();">
              <option value='am'>am</option>
              <option value='pm'>pm</option>
            </select>

          </p>
        </div>
        </form>
      </div>

    <!-- Insert google map somewhere here-->
    <div id="mapArea"></div>

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
