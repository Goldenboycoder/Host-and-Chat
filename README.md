# Host-and-Chat
Host and Chat is a side project of mine , it gives the user the ability to host his own chat server , then use the client to text chat and voice chat with there circle as if they were in the same room .
## Prerequisites
#### To be able to use the server you will need to :
* Install [java jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html) on your machine 
* Know how to change the directory in your appropriate OS to the one the server is in
* Setup port forwarding on your router so that the `ChatServer` may  be used in a WAN environment.
* Create an exception in your firewall for these ports

**Choose two `consecutive` ports _eg: port1=4444 , port2=4445_**

Example for windows : 
C:\\>`cd projects\host and chat\`
#### To be able to use the client you will need to :
* Install [java jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html) or [java jre](https://www.java.com/en/download/win10.jsp) on your machine 

***If you want to use the `ChatClient.exe` make sure that the firewall let's it connect to your network***

## How to start the ChatServer
If you are in the directory of the `ChatServer` and you already have java jdk installed correctly on your machine then :

##### To compile ChatServer
```
javac ChatServer.java
```
##### To Run ChatServer
```
java ChatServer [port]
```
`[port]` is the first port that you chose to forward on your router. eg : port = 4444 `java ChatServer 4444`

**Then you will be asked to enter an `IP Address` you should enter your `Local IP` here , the one that the router assignes to you .If you're going to leave the server running for a long time you should assign your Pc a static ip address.**

## How to use the ChatClient
You can either compile it and run or use the ChatClient.exe . *the .exe file will probably not be updated ,so do compile it*

#### Interface

[interface](screenshots/int1)

### Client to server connection
1. Enter the `hostname` which is the public IP of the machine hosted by the server *the host must share his public ip with the group he wants to give accesss to*
2. Enter the port on which the server is listening *the host must share this too*
3. Enter a username

**And finally click on `Connect`**

#### Once connected 
* A voice channel will be created that enables the users to speak to each others
* A text box is available near the send button , write in it whatever you feel like texting and click send to broadcast it to all the users connected

## Fully built with java
## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments
[Inspiration](https://github.com/adolfintel/voicechat)
