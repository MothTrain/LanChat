# LanChat
A java aplication that allows you to message on the same LAN network. It works
on a peer-to-peer basis, where one node connects to a listening node with a 
connection key created by the listening node

## Some information
- This is a personal project of mine
- This is not a fully stable project
- It has no Denial of Service protection
- It has no Encryption
- Because this is meant to be part of a portfolio (It is a demonstration of my
  own abilites) so I will ignore or decline any pull requests made (sorry)
- It operates with a GUI (yay!)
- It only compiles to .jar (on top of the regular build) using maven
- The pom.xml requires at least java 19

## Install
```
mvn clean package
```
A jar will appear in `target\jars`

## How to Run
Just double press the jar file and the program should start!
