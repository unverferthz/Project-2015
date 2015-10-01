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

    <title>How it works</title>

    <!-- Bootstrap core CSS -->
    <link href="dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- Custom styles for this template -->
    <link href="styleSheet.css" rel="stylesheet">
    
    <script>
      (function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
      (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
      m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
      })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
    
      ga('create', 'UA-68255509-1', 'auto');
      ga('send', 'pageview');
    </script>

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
	    <li><a href="map.php">Map</a></li>
	    <li class="active"><a href="howItWorks.php">How it works</a></li>
            <li><a href="youCanHelp.php">You can help - Volunteering</a></li>
	    <li><a href="contact.php">Contact us</a></li>
          </ul>
        </div><!--/.nav-collapse -->
      </div>
    </nav>

    <div class="container">

        <div class="mainContainer">
	  <div class="textPadding">
	    <h1>How it works</h1>
 	    <h2>Arduino</h2>
	    <p>The arduino is a minature computer, that is equipped with a sonar sensor and bluetooth chip. The sonar
	    sensor works by sending out a sound wave and it times how long it takes for that sound wave to return
	    after being reflected off of an object. From the time it takes for the sound wave to come back it knows
	    how far away the object was.</p>
	    <p>Once the sonar has detected something within the threshold, set at 1.5m, it communicates through
	    bluetooth with an android application to store information about the incident.</p>
	    <br/>
	    <h2>Android</h2>
	    <p>The phones application will connect through bluetooth to the arduino automatically
	    once the application is opened. When it recieves a signal from the arduino it will log
	    the distance, time and location using GPS. This information can be viewed as raw data
	    or on a map. The user can then upload the data they've collected onto the server where it
	    can be stored onto the database to be viewed on this website.</p>
	    
	    <br/>
	    
	    <h3>Pictures of device</h3>
	    
	    <div class="widePictureBox">
	      <p><b>First prototype:</b> Attatches around the users waste, was reasonably bulky.</p>
	      <img class="picBorder" src="/pictures/first_prototype.jpg" alt="First prototype height="250" width="400"/>
	    </div>
	    
	    <div class="widePictureBox">
	      <p><b>Second prototype:</b> Attatches onto the users bike seat post. Much smaller than the original.</p>
	      <img class="picBorder" src="/pictures/second_prototype.jpg" alt="Second prototype height="250" width="400"/>
	    </div>
	    
	    <br class="clear"/>
	    <br/>
	    
	    <h3>Pictures of the app:</h3>
	    
	    <div class="leftFloat">
	    <p><b>Main screen:</b> Here the app tries to connect to the arduino. While connected it's waiting to recieve information from the
	    arduino.</p>
	    <img class="picBorder" src="/pictures/app_main_screen.png" alt="Main screen" height="300px" width="175px"/>
	    </div>
	    
	    <div class="leftFloat">
	    <p><b>Map screen:</b> Here you can view all of your close calls on a map inside the app. Each marker was something getting
	    close to the sensor. The markers can be tapped on for more information.</p>
	    <img class="picBorder" src="/pictures/app_map_screen.png" alt="Map screen" height="300px" width="175px"/>
	    </div>
	    
	    <div class="leftFloat">
	    <p><b>Incident screen:</b> Here you can view all of the raw data. There is also a button to upload the data to contribute
	    to the projects overall data.</p>
	    <img class="picBorder" src="/pictures/app_incidents_screen.png" alt="Incident screen" height="300px" width="175px"/>
	    </div>
	    
	    <br class="clear"/>
          </div>
        </div>
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

