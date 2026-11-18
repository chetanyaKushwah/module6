
FROM mcr.microsoft.com/azure-functions/java:4-java21

COPY target/azure-functions/orderitemsreserver /home/site/wwwroot

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

EXPOSE 80
