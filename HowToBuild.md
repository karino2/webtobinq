# HowToBuild #

  * setup eclipse, maven, HG
  * check-out code.
  * download antlr3.2-gwt-module.jar from http://code.google.com/p/antlrgwt/downloads/list
  * download gchart.jar from http://code.google.com/p/clientsidegchart/downloads/list
  * download maven from http://maven.apache.org/
  * from cmd, go to maven bin.
  * set JAVA\_HOME (ex: set JAVA\_HOME=C:\Program Files\Java\jdk1.6.0\_27)
  * call command below (fix -Dfile= section)
```
mvn.bat install:install-file -DgroupId=org.antlr.AntlrGWT -DartifactId=antlrgwt -Dversion=1.0 -Dpackaging=jar -Dfile=..\local_jars\antlr3.2-gwt-module.jar
mvn.bat install:install-file -DgroupId=com.googlecode.gchart.GChart -DartifactId=gchart -Dversion=1.0 -Dpackaging=jar -Dfile=..\local_jars\gchart.jar
```
  * ADD JDK from Windows->Preferences->Installed JRE->Search->(select JDK path under Program Files/Java)
  * Run as->maven build...->goal gae:unpack
  * Run as->maven test

Give credential:

  * open cmd.exe
  * goto C:\Users\{yourname}\.m2\repository\com\google\appengine\appengine-java-sdk\1.5.2\appengine-java-sdk-1.5.2\bin
  * appcfg.cmd update {yourworkspace}\WebTobinQ\target\WebTonbinQ-0.0.1-SNAPSHOT
  * type email and password.


After setting up finished, you can run:


  * UnitTest: Run->JUnit test->Use configuration specific settings->Eclipse JUnit launcher
  * Deploy: Run as->Maven build...->goal gae:deploy