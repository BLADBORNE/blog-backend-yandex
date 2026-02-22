FROM tomcat:10.1-jdk21
COPY target/*.war /usr/local/tomcat/webapps/api.war
EXPOSE 8080
