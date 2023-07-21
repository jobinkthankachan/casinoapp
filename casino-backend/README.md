# Read Me

# Spring boot App and H2

## Prerequisites

Maven , Java 11

## Installation

Clone the repo -> https://github.com/jobinkthankachan/casinoapp.git
navigate to the local directory where you had cloned repo , go to casino-backend/ dir

## Start the Program

```bash
mvn spring-boot:run
```

### Note:

There are 2 Test Classes :

1) CasinoControllerTest - tests all controller scenarios
2) CasinoConcurrencyTest - tests concurrency scenario of program specially updatePlayerBalance method, when
   multiple threads try to update the players balance simultaneously.

## Run Unit Test

navigate to the local directory where you had cloned repo , go to casino-backend/ dir and execute the below command

```bash
mvn test
```



