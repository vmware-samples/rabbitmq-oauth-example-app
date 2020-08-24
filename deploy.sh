#!/bin/bash

# RabbitMQ OAuth Example App
# Copyright 2020 VMware, Inc.
#
# SPDX-License-Identifier: Apache-2.0
#
# This product is licensed to you under the Apache 2.0 license
# (the "License").  You may not use this product except in compliance
# with the Apache 2.0 License.
#
# This product may include a number of subcomponents with separate
# copyright notices and license terms. Your use of these subcomponents
# is subject to the terms and conditions of the subcomponent's license,
# as noted in the LICENSE file.


APP_NAME=client-credentials-sample

./gradlew build && cf push --no-start
cf bind-service $APP_NAME identity -c binding.json
cf start $APP_NAME
