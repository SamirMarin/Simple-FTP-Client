all: CSftp.jar
CSftp.jar: CSftp.java
	javac CSftp.java
	javac UserCommands.java
	javac FTPPanel.java
	jar cvfe CSftp.jar CSftp *.class


run: CSftp.jar
	java -jar CSftp.jar

clean:
	rm -f *.class
	rm -f CSftp.jar
