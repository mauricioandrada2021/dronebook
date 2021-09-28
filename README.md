# UAV Hub - client and server software

## Introduction

The binaries and source code in this repository are a companion to the book [***Principles of Drone Design***](https://payhip.com/dronedesign).

It is a simple client/server implementation allowing QGroundControl to communicate with an UAV over the Internet.

It is composed of a server proxy, responsible for relaying messages from QGroundControl to an UAV's on-board computer and vice-versa, and a client application running in the RPi 4 on-board computer, responsible for relaying MAVLINK messages between the server and the flight controller.

The code provided here is very simple and shall not be used for commercial applications; it has the following limitations:

* All communication is in clear text so privacy is not guaranteed
* There's no authentication so anyone can interact with the server
* The server only supports one single UAV and QGroundControle connection
* The code supports only RPi 4, Pixhawk running PX4 and QGroundControl

Users are free to download and modify the code and remove these limitations.

## Prerequisites

* Access to the Internet (WiFi or mobile hostspot preferred)
* Laptop/desktop with QGroundControl installed and configured per the instructions in the book
^ Raspberry Pi 4 with Raspberry OS installed
* Pixhawk with PX4 software installed
* Serial cable connecting the Raspberry Pi 4 to the Pixhawk, per instructions in the book
* Web service account (AWS, OpenShift, etc.)
* Java Runtime Environment installed in both RPi 4 and web service account (usually installed by default)


## Usage
1. Use *git clone* or link to the ZIP file and download the content of the repository

## Server proxy

1. Upload the service JAR file `<git clone folder>/build/px4server.jar` to your web service account
   1. The server will bind to hardcoded ports 54321 and 51001 so make sure these ports are open for TCP external access
1. Run the server proxy:

`java -jar serverproxy.jar [obc|qgc]

The **obc** option will display logs with data received from the on-board computer; the **qgc** option will show data from QGroundControle.

## Client application

1. Upload the client app JAR file `<git clone folder>/build/px4client.jar` to the RPi 4
1. Run the client app:

`java -jar clientapp.jar [-v 1|2|3] -h <hostname> -i obc`

**hostname** is the URL or IP address for the server proxy.

**-v** enables verbose mode and the client will display messages on screen, useful for debugging.
* 1: minimalistic logs
* 2: logs from option 1 + data read from the serial port
* 3: logs from 1 + data received from the server

The book provides instructions n how to setup the RPi 4 to start the client application automatically on power up.
