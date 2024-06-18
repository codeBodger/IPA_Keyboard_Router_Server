all: compile

compile: IPA_Keyboard_Router_Server.class sClient.class sServer.class

IPA_Keyboard_Router_Server.class: IPA_Keyboard_Router_Server.java
	javac IPA_Keyboard_Router_Server.java
sClient.class: sClient.java
	javac sClient.java
sServer.class: sServer.java
	javac sServer.java

run: all
	kill `ps -e -o pid,args | grep -v grep | grep "/usr/bin/java -cp /home/phonetics/IPA_Keyboard_Router_Server IPA_Keyboard_Router_Server" | grep -o "^\ \+\S\+"`

pull:
	git pull

checkout:
	git checkout *.java Makefile

prun: pull run
crun: checkout run
