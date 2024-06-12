all: compile

compile: IPA_Keyboard_Router_Server.class sClient.class sServer.class

IPA_Keyboard_Router_Server.class: IPA_Keyboard_Router_Server.java
	javac IPA_Keyboard_Router_Server.java
sClient.class: sClient.java
	javac sClient.java
sServer.class: sServer.java
	javac sServer.java

run: pull all
	-kill `ps -e -o pid,args | grep "java IPA_Keyboard_Router_Server" | grep -v grep | grep -o "^\S\+"`
	java IPA_Keyboard_Router_Server >> /home/phonetics/java-log &

pull:
	git pull