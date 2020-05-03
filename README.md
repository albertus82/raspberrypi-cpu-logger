Raspberry Pi CPU Logger
=======================

[![Build status](https://github.com/albertus82/raspberrypi-cpu-logger/workflows/build/badge.svg)](https://github.com/albertus82/raspberrypi-cpu-logger/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=it.albertus%3Araspberrypi-cpu-logger&metric=alert_status)](https://sonarcloud.io/dashboard?id=it.albertus%3Araspberrypi-cpu-logger)

Log [Raspberry Pi](https://www.raspberrypi.org) CPU frequency and temperature to [ThingSpeak](https://thingspeak.com).

## Requirements

### Mandatory

* Java SE Development Kit 11 (e.g.: [BellSoft Liberica](https://bell-sw.com) JDK 11)
* [ThingSpeak](https://thingspeak.com) account (available for free)

### Recommended

* [Apache Maven](https://maven.apache.org) 3.3.x

## Download

`git clone https://github.com/albertus82/raspberrypi-cpu-logger.git`

## Build

### Using Maven

`mvn clean test`

### Without Maven

`javac -d target/classes src/main/java/RaspberryPiCpuLogger.java`

## Usage

`java -cp target/classes RaspberryPiCpuLogger WRITE_API_KEY`

* `WRITE_API_KEY`: the **Write API Key** associated with your ThingSpeak channel (e.g. `1234567890ABCDEF`).

## Example

:warning: **Don't forget to replace `WRITE_API_KEY` with your actual ThingSpeak Write API Key!**

```sh
git clone https://github.com/albertus82/raspberrypi-cpu-logger.git
cd raspberrypi-cpu-logger
javac -d target/classes src/main/java/RaspberryPiCpuLogger.java
cd target/classes
java -Xms8m -Xmx32m RaspberryPiCpuLogger WRITE_API_KEY
```

## Install as a service

:warning: **Don't forget to replace `WRITE_API_KEY` with your actual ThingSpeak Write API Key!**

```sh
cd /opt
sudo git clone https://github.com/albertus82/raspberrypi-cpu-logger.git
cd raspberrypi-cpu-logger
sudo mvn clean test
printf '#!/bin/sh\njava -Xms8m -Xmx32m -cp target/classes RaspberryPiCpuLogger WRITE_API_KEY\n' | sudo tee runsvc.sh
sudo chmod +x runsvc.sh
printf '[Unit]\nDescription=CPU Logger\nAfter=network.target\n\n[Service]\nExecStart=/opt/raspberrypi-cpu-logger/runsvc.sh\nUser=pi\nWorkingDirectory=/opt/raspberrypi-cpu-logger/\nKillMode=control-group\nKillSignal=SIGTERM\nTimeoutStopSec=5min\n\n[Install]\nWantedBy=multi-user.target\n' | sudo tee /etc/systemd/system/raspberrypi-cpu-logger.service
sudo systemctl enable raspberrypi-cpu-logger
sudo service raspberrypi-cpu-logger restart
```
