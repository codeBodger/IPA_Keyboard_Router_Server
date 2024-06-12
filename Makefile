all: compile

compile: IPA_Keyboard_Router_Server.class sClient.class sServer.class

IPA_Keyboard_Router_Server.class: IPA_Keyboard_Router_Server.java
	javac IPA_Keyboard_Router_Server.java
sClient.class: sClient.java
	javac sClient.java
sServer.class: sServer.java
	javac sServer.java

run: pull all
	-kill `ps -e -o pid,args | grep -v grep | grep "java IPA_Keyboard_Router_Server" | grep -o "^\S\+"`
	sleep 2
	java IPA_Keyboard_Router_Server &>> /home/phonetics/java-log &

pull:
	git pull