# Module 6 – Azure Functions and Blob Storage

## Overview
PetStore integrates with a Java Azure Function that stores updated shopping-cart JSON in Azure Blob Storage.

## Project Structure
- petstore/: PetStore Spring Boot application and microservices.
- OrderItemsReserver/: Java 21 Azure HTTP-triggered Function.

## Workflow
1. User updates the PetStore cart.
2. PetStore sends order JSON to the Azure Function using HTTP POST.
3. Session ID is passed through X-Session-ID.
4. Function uploads JSON to the orders Blob Storage container.
5. The same session ID blob is overwritten on subsequent updates.

## Technologies
- Java 21, Spring Boot, Maven, Docker
- Azure Functions, Azure Container Apps, Azure Container Registry
- Azure Blob Storage

## Configuration
Configure ORDER_RESERVER_FUNCTION_URL, AZURE_STORAGE_CONNECTION_STRING, AzureWebJobsStorage and FUNCTIONS_WORKER_RUNTIME through environment variables or Azure secrets.
See petstore/.env.sample for local configuration variable names.

## Build Function
cd OrderItemsReserver && mvn clean package && mvn azure-functions:package

## Verification
Local and deployed Function tests succeeded. PetStore cart updates invoked the deployed Function, and session JSON blobs were created or overwritten in Blob Storage.

## Security
Never commit real credentials, connection strings, local.settings.json, or secret-bearing .env files.

## GitHub
https://github.com/chetanyaKushwah/module6
