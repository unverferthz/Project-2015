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
            <img id="logo" src="/pictures/Roximal_logo.png" width=90px height=35px />
          </a>
        </div>
        <div id="navbar" class="collapse navbar-collapse">
          <ul class="nav navbar-nav">
            <li><a href="index.php">Home</a></li>
	    <li class="active"><a href="">Map</a></li>
	    <li><a href="howItWorks.php">How it works</a></li>
	    <li><a href="youCanHelp.php">You can help -Volunteering</a>
	    <li><a href="contact.php">Contact us</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </nav>

    <div class="mainContainer">
      <div class="textPadding">
        <h1>Map</h1>
        <br/>
        <p>This map will show all of the incidents that have been collected and submitted. 
        The sidebar will allow you to sort the data on date, time, and distance. 
        The time sorting works by showing all the incidents between the first and second time dropdown. If the times are the same it will
        show all incidents. 
        The maximum distance an incident can be is 150cm and the lowest is 30cm. The system has been in use since August 6th 2015, so you
        will be able to find data from then onwards.</p>
      </div>

    <!-- Google map and sidebar container-->
    <div>
      <div id="mapSideBar">
        <div>
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

            <select id='day1Selector' name='daySelector' onchange="dayChanged()">
            </select>
          </p>
          <p class='alignRight'>
              and

            <select id='month2Selector' name='monthSelector' onchange="month2Changed(this.value);">
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

            <select id='day2Selector' name='daySelector' onchange="dayChanged()">
            </select>
          </p>
        </div>
        <br/>
        <div>
          <p class='alignRight'>
            Time between:
            <select id='time1' name='time1'>
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
              <option value='0'>12</option>
            </select>

            <select id='time1AmPm' name='time1AmPm'>
              <option value='AM'>am</option>
              <option value='PM'>pm</option>
            </select>
          </p>
          <p class='alignRight'>
            and

            <select id='time2' name='time2'>
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
              <option value='0'>12</option>
            </select>

            <select id='time2AmPm' name='time2AmPm'>
              <option value='AM'>am</option>
              <option value='PM'>pm</option>
            </select>
          </p>
        </div>
        <br/>
	 <div>
	  <p class='alignRight'>
          Distance one:<input type="text" id="llimit" size="3" >cm<br/>
          Distance two:<input type="text" id="ulimit" size="3" >cm<br/>
          <br/>
	  <input type="button" id="checkDistance" value="Update incidents">
	  </p>
        </div>
      </div><!-- End map side bar-->
      <div id="mapArea"></div>
    </div>
    <br class="clear"/>

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
