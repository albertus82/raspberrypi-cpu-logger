Raspberry Pi CPU Logger
=======================

[![Build status](https://github.com/albertus82/raspberrypi-cpu-logger/workflows/build/badge.svg)](https://github.com/albertus82/raspberrypi-cpu-logger/actions)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=it.albertus%3Araspberrypi-cpu-logger&metric=alert_status)](https://sonarcloud.io/dashboard?id=it.albertus%3Araspberrypi-cpu-logger)
[![Known Vulnerabilities](https://snyk.io/test/github/albertus82/raspberrypi-cpu-logger/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/albertus82/raspberrypi-cpu-logger?targetFile=pom.xml)

Log [Raspberry Pi](https://www.raspberrypi.org) CPU frequency and temperature to [ThingSpeak](https://thingspeak.com).

## Requirements

* Java Runtime Environment 11 (either OpenJDK 11 included in Raspbian Buster or [BellSoft Liberica](https://bell-sw.com) JRE 11 is good)
* [ThingSpeak](https://thingspeak.com) account (available for free)

## Download

`git clone https://github.com/albertus82/raspberrypi-cpu-logger.git`

## Configuration

Put the **Write API Key** associated with your ThingSpeak channel (e.g. `1234567890ABCDEF`) into a text file named `api.key` in the `conf` directory.

## Usage

Launch the program via the shell script `start.sh` and use CTRL+C to terminate, or [install the program as a service](#install-as-a-service).

## Example

:warning: **Don't forget to replace `WRITE_API_KEY` with your actual ThingSpeak Write API Key!**

```sh
git clone https://github.com/albertus82/raspberrypi-cpu-logger.git
cd raspberrypi-cpu-logger
 echo WRITE_API_KEY > conf/api.key
./start.sh
```

## Install as a service

:warning: **Don't forget to replace `WRITE_API_KEY` with your actual ThingSpeak Write API Key!**

```sh
cd /opt
sudo git clone https://github.com/albertus82/raspberrypi-cpu-logger.git
cd raspberrypi-cpu-logger
 echo WRITE_API_KEY | sudo tee conf/api.key
sudo chmod 640 conf/api.key
printf '[Unit]\nDescription=CPU Logger\nAfter=network.target\n\n[Service]\nExecStart=/opt/raspberrypi-cpu-logger/start.sh\nUser=root\nTimeoutStopSec=5min\n\n[Install]\nWantedBy=multi-user.target\n' | sudo tee /etc/systemd/system/raspberrypi-cpu-logger.service
sudo systemctl enable raspberrypi-cpu-logger
sudo service raspberrypi-cpu-logger restart
```
