FROM mcr.microsoft.com/azure-functions/java:4-java17

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

COPY target/azure-functions/azure-functions-usuarios-roles-*/ /home/site/wwwroot