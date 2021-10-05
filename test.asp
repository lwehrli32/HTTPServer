<html>
	<head>
		<title>Main Page</title>
		<link rel="stylesheet" type="text/css" href="style.css">
	</head>
	
	<body>
		<h1 id="header">Welcome to my Server</h1>
		<input type="button" value="Send Email" onclick="sendEmail()">
	</body>
	<script>
		function sendEmail(){
		
			var xmlhttp = new XMLHttpRequest();
                xmlhttp.onreadystatechange = function() {
                    if (this.readyState == 4 && this.status == 200) {
                        console.log(this.responseText);
                    }
                };
                xmlhttp.open("POST", "test.html", true);
                xmlhttp.send();
		}
	</script>
</html>