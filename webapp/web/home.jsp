<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="../../favicon.ico">

    <title>Logius document converter</title>

    <!-- Bootstrap core CSS -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->

    <!-- Just for debugging purposes. Don't actually copy these 2 lines! -->
    <script src="js/ie-emulation-modes-warning.js"></script>

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>
<body>

<div id="wrap" class="container">

    <div id="main">
        <div>
            <h3 style="font-weight: bold;">Office to PDF/A-1A converter</h3>
            <p>
                <span style="font-weight: bold; color: rgba(0, 0, 0, 0.5)">Convert MS Office or OpenOffice files (supported extensions: doc, docx, xls, xlsx, ppt, pptx, ods, odt, odp) into PDF/A-1 Level A document </span>
            </p>
        </div>

        <!-- Main component for a primary marketing message or call to action -->
        <div align='left' style="padding: 12px" class="jumbotron">
            <div class="form-group">
            <form id='file-form' enctype="multipart/form-data">
                <input id='file-select' class="filestyle" type="file" name="aFile" data-buttonText="Choose file"
                       data-buttonBefore="true">
                <br>
                <input class="btn btn-default" id='upload-button' type="submit" value="Convert file" />
            </form>
            </div>
        </div>

        <div id="loading_message"></div>
        <div id="converting_message"></div>
        <div id="validating_message"></div>
        <div id="information"></div>
    </div>

</div> <!-- /container -->

<!-- Bootstrap core JavaScript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<%--<script src="http://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>--%>
<script src="js/jquery.min.js"></script>
<%--<script>window.jQuery || document.write('<script src="js/jquery.min.js"><\/script>')</script>--%>
<script src="js/bootstrap.min.js"></script>
<script type="text/javascript" src="js/bootstrap-filestyle.min.js"> </script>
<!-- IE10 viewport hack for Surface/desktop Windows 8 bug -->
<script src="js/ie10-viewport-bug-workaround.js"></script>

<script>
    window.onload = function() {
        document.getElementById('file-form').action = sendDocument();
    };
    $(":file").filestyle({buttonBefore: true, 'icon': false});
</script>

<script>
    function sendDocument() {
        var form = document.getElementById('file-form');
        var fileSelect = document.getElementById('file-select');

        form.onsubmit = function(event) {
            event.preventDefault();

            // Update button text.
            document.getElementById("loading_message").innerHTML = 'Uploading...';
            document.getElementById("converting_message").innerHTML = '';
            document.getElementById("validating_message").innerHTML = '';
            document.getElementById("information").innerHTML = '';

            // Get the selected files from the input.
            var files = fileSelect.files;

            // Create a new FormData object.
            var formData = new FormData();

            // Loop through each of the selected files.
            for (var i = 0; i < files.length; i++) {
                var file = files[i];

                // Add the file to the request.
                formData.append(file.name, file);
            }

            // Set up the request.
            var xhr = new XMLHttpRequest();

            // Open the connection.
            xhr.open('POST', './uploadDocument', true);

            xhr.onload = function () {
                if (xhr.status === 200) {
                    // File(s) uploaded.
                    $.post("./viewInformation", function (data) {
                        document.getElementById("information").innerHTML = data;
                        if (data == "") {
                            document.getElementById("loading_message").innerHTML = "Document was uploaded.";
                            document.getElementById("converting_message").innerHTML = "Converting...";
                            $.post("./convert", function () {
                                $.post("./viewInformation", function (data) {
                                    document.getElementById("converting_message").innerHTML =
                                        "Converting was finished.";
                                    document.getElementById("information").innerHTML = data;
                                    if (data == "") {
                                        document.getElementById("converting_message").innerHTML +=
                                            '<form method="get" action="./download"> <button class="btn btn-primary" type="submit">Download file</button></form>';
                                        document.getElementById("validating_message").innerHTML = "Validating...";
                                        $.post("./validate", function (data) {
                                            document.getElementById("validating_message").innerHTML = "<pre>" + data + "</pre>";
                                            $.post("./viewInformation", function (data) {
                                                document.getElementById("information").innerHTML = data;
                                            })
                                        });
                                    }
                                })
                            });
                        } else {
                            document.getElementById("loading_message").innerHTML = "";
                        }
                    })
                } else {
                    document.getElementById("loading_message").innerHTML = "Upload failed!";
                }
            };

            // Send the Data.
            xhr.send(formData);
        }
    }
</script>

</body>
</html>
