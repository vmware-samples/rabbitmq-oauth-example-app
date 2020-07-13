# client credentials app
This app uses OAuth to bind to RabbitMQ SI

# Prerequisite
    * SSO tile installed with plan and single instance named `identity`
    * RabbitMQ tile with single instance created called `rabbit`

# Deploy

* Find your space guid
* In `manifest.yml` and `binding.json` update `p-rabbitmq_<guid>`references with your space guid 
* Deploy the application without starting

```
$ ./gradlew build && cf push --no-start
```
* Bind application to the identity service using `binding.json`
```
$ cf bind-service client-credentials-sample identity -c binding.json
```
* Uncomment the following from the `manifest.yml`
```
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_SCOPE
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_AUTHORIZATIONGRANTTYPE
```

* Deploy the app

```
$ cf push
```

* Grant authorities in the SSO dashboard using cody's username and password