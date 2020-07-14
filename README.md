# client credentials app
This app uses OAuth to bind to RabbitMQ SI

# Prerequisites
    * SSO tile installed with plan and single instance named `identity`
    * RabbitMQ tile with single instance created called `rabbit`

# Deploy

* Find your space guid
* In `application.yml` and `binding.json` update `p-rabbitmq_<guid>`references with your space guid 
* Run `./deploy.sh`
* Check granted authorities in the SSO dashboard using cody's username and password