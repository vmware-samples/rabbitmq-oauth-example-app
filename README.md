# RabbitMQ OAuth Example App
This is an example app which uses OAuth 2.0 to bind to a VMware Tanzu® RabbitMQ™ for VMs
On-Demand service instance.

## Prerequisites
    * VMware Tanzu® Application Service for VMs
    * VMware Tanzu® Single Sign-On Service tile installed with access to the system plan enabled for an org and single service instance named `identity` within that org.
    * Tanzu RabbitMQ tile with an On-Demand service instance created in the same space, called `rabbit`.

## Overview
Apps authenticate with Tanzu RabbitMQ using the OAuth2 client flow as follows:
1. The app queries UAA for a JWT token with relevant scopes, using the client credentials granted through binding with the Single Sign-On instance.
1. The app then uses this JWT token, in place of a password, to authenticate with the RabbitMQ service instance.

### Create UAA Groups for a Space
You must create UAA groups for the space in Tanzu Application Service that contains the Tanzu RabbitMQ service instance. These groups correspond to RabbitMQ resources, and can be granted as authorities to clients which interact with RabbitMQ.

To create a UAA group for a space:
1. Display the space GUID by running:
    ```
    cf space SPACE-NAME --guid
    ```
    where SPACE-NAME is the name of the space
1. Record the space GUID
1. Retrieve the UAA admin client credentials from Ops Manager
    ```
    om credentials --product-name cf \
        --credential-reference .uaa.admin_client_credentials
    ```
1. Authenticate with UAA
    ```
    uaac target https://uaa.SYSTEM-DOMAIN
    uaac token client get CLIENT -s SECRET
    ```
    Where:
        a. SYSTEM-DOMAIN is the system domain for your TAS foundation
        a. CLIENT and SECRET are the credentials for the uaa admin client.
1. Create UAA groups using the space GUID and RabbitMQ resources by running:
    ```
    uaac group add p-rabbitmq_SPACE-GUID.read:*/*
    uaac group add p-rabbitmq_SPACE-GUID.write:*/*
    uaac group add p-rabbitmq_SPACE-GUID.configure:*/*
    ```
    where SPACE-GUID was recorded in step 2 above.

### Assign an Identity to the App
You must associate any applications needing to interact with a Tanzu RabbitMQ instance with a UAA client. You must grant this client appropriate authorities corresponding to the Tanzu RabbitMQ resources it will interact with.

To assign an indentity to the example application using the Tanzu Single Sign-On Service tile:
1. Enable the UAA/System plan for the org containing the Tanzu RabbitMQ service instance by running the following CF CLI command:
    ```
    cf enable-service-access p-identity -p uaa -o ORG-NAME
    ```
    where ORG-NAME is the name of the cf org with the Tanzu RabbitMQ service instance.
1. Target the space containing the Tanzu RabbitMQ service instance and create a Single Sign-On service instance with the following CF CLI command:
    ```
    cf create-service p-identity uaa instance
    ```
1. Create a binding parameters JSON file named `binding.json`. SSO uses this file to configure the OAuth client when binding the application to the SSO service instance. This should include any RabbitMQ scopes which should be granted to the app, corresponding ot the UAA groups created above.
    ```
    {
        "grant_types": ["client_credentials],
        "authorities": [
            "uaa.resource",
            "openid",
            "roles",
            "user_attributes",
            "p-rabbitmq_SPACE-GUID.read:*/*",
            "p-rabbitmq_SPACE-GUID.write:*/*",
            "p-rabbitmq_SPACE-GUID.configure:*/*"
        ]
    }
    ```
    where SPACE-GUID was recorded above.
1. Edit `src/main/resources/application.yml` to update the `p-rabbitmq_SPACE-GUID` references with the value of SPACE-GUID recorded above.
1. Run `./deploy.sh` to build and deploy the application and bind to the Tanzu Single Sign-On service instance and the Tanzu RabbitMQ service instance.

### Grant Authorities to the App
1. Determine the neame of the UAA client associated with the application. To do this, look up the application in the Tanzu Single Sign-On service instance dashboard, or run the following UAA CLI command:
    ```
    uaac clients | grep -B 10 "name: rabbitmq-oauth-example-app"
    ```
1. From the output of the above command, record the CLIENT-NAME.
1. Verify this is the correct UAA client by examining the output of the command:
    ```
    uaac client get CLIENT-NAME
    ```
1. Grant the UAA client the relevant authorities using the UAA CLI by running:
    ```
    uaac client update CLIENT-NAME \
        --authorities 'uaa.resource openid roles user_attributes p-rabbitmq_SPACE-GUID.read:*/* p-rabbitmq_SPACE-GUID.write:*/* p-rabbitmq_SPACE-GUID.configure:*/*'
    ```
    where CLIENT-NAME and SPACE-GUID were recorded above.

### Visit the web interface
Visit the web interface of the app and verify that a token has been granted and a message has been sent.


## Notes

* the name of the queue/exchange/routingKey is specifed in `application.yml`
* CFEnv AMQP processor is disabled so it does not automatically create `spring.rabbitmq.*` properties. This might cause two connections to be created to RabbitMQ since the binding key still contains username and password in the url.