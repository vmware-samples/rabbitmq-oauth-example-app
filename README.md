# client credentials app
This app uses OAuth to bind to RabbitMQ SI

# Prerequisites
    * SSO tile installed with plan and single instance named `identity`
    * RabbitMQ tile with single instance created called `rabbit`

# Deploy

* Find your space guid
* In `application.yml` and `binding.json` update `p-rabbitmq_<guid>`references with your space guid 
* Deploy the application without starting

```
$ ./gradlew build && cf push --no-start
```

Note: If you try to start the application at this point it will likely fail because spring is not able to initialize SSO beans.
 
* Bind application to the identity service using `binding.json`

```
$ cf bind-service client-credentials-sample identity -c binding.json
```

* Deploy the app

```
$ cf start client-credentials-sample
```

* Check granted authorities in the SSO dashboard using cody's username and password