all: compile

compile: IPA_Keyboard_Router_Server.class sClient.class sServer.class

IPA_Keyboard_Router_Server.class: IPA_Keyboard_Router_Server.java
	javac IPA_Keyboard_Router_Server.java
sClient.class: sClient.java
	javac sClient.java
sServer.class: sServer.java
	javac sServer.java

run: all
	java IPA_Keyboard_Router_Server >> /home/phonetics/java-log &
