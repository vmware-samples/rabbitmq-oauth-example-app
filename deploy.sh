#!/bin/bash

APP_NAME=client-credentials-sample

./gradlew build && cf push --no-start
cf bind-service $APP_NAME identity -c binding.json
cf start $APP_NAME
