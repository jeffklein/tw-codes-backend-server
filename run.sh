#!/bin/bash

mvn -Dmaven.repo.local=/home/jklein/.m2/repository -Djava.security.egd=file:/dev/./urandom clean spring-boot:run

