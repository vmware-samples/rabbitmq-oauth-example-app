# SSO RabbitMQ example
This app uses OAuth to bind to RabbitMQ SI

# Prerequisites
    * SSO tile installed with plan and single instance named `identity`
    * RabbitMQ tile with single instance created called `rabbit`

# Deploy

* Find your space guid
* In `application.yml` and `binding.json` update `p-rabbitmq_<guid>`references with your space guid 
* Run `./deploy.sh`
* Check granted authorities in the SSO dashboard using cody's username and password

# Notes

* the name of the queue/exchange/routingKey is specifed in `application.yml`
* CFEnv AMQP processor is disabled so it does not automatically create `spring.rabbitmq.*` properties. This might cause two connections to be created to RabbitMQ since the binding key still contains username and password in the url.