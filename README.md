# pdfa-converter
REST web service wrapping of the Open Office PDF/A convertor

# Steps to run web application
 - Install _Apache Tomcat 8.5_
 - Install _Java 8 (OpenJDK)_
 - Install _LibreOffice 5.4.3.2_
 - Create _logius.properties_ file with properties:
    - dir.temp=path to temporary folder
    - officeDir=path to LibreOffice executive file
 - Create _LOGIUS_PROPERTIES_ environment variable and set it to the path of logius.properties file
 - Copy a _logius.war_ file into subdirectory _webapps_ in Tomcat directory
 - Start Tomcat server