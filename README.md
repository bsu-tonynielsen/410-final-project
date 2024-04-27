#CS 410/510 Final Project
@authors: Ander Barbot and Tony Nielson

## Description
Allows the creation and maintenance of a school management system, which is implemented using an SQL database. Supports classes, students and assignments, as well as supporting entities and relationships.

## Installation
- clone this repo into your onyx system
- ensure there is an active SQL database on onyx
	- local SQL connection can be established via tunneling a local port to BSU's system via `ssh -L port:127.0.0.1:port username@onyx.boisestate.edu`
	- remote SQL connection
		- ssh into BSU's system
		- create database via `deploydb.sh`
		- `cd /home/$USER/sandboxes/msb_8_0_30`
		- start

## Usage 
- change environment variables as specified in database.java
- compile and run schoolManagement.java
- run `test connection` while in School Management Shell
- use help

## Video
The video debrief for this project can be found [here](https://youtu.be/v0MlIYPERy0)