
package com.chtrembl;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;

public class OrderItemsReserverFunction {

    @FunctionName("OrderItemsReserver")
    public HttpResponseMessage run(

            @HttpTrigger(
                    name = "request",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS
            )
            HttpRequestMessage<Optional<String>> request,

            ExecutionContext context) {

        context.getLogger().info(
                "OrderItemsReserver function triggered.");

        String requestBody = request.getBody().orElse("");

        if (requestBody.isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Request body cannot be empty")
                    .build();
        }

        // Read shopping session ID from HTTP header
        String sessionId = request.getHeaders().get("x-session-id");

        if (sessionId == null || !sessionId.matches("[a-zA-Z0-9._-]{1,128}")) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Valid X-Session-ID header is required")
                    .build();
        }

        try {
            BlobStorageService blobStorageService =
                    new BlobStorageService();

            blobStorageService.uploadOrder(sessionId, requestBody);

            context.getLogger().info(
                    "Order JSON uploaded for session: " + sessionId);

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Order JSON uploaded successfully for session: "
                            + sessionId)
                    .build();

        } catch (Exception e) {

            context.getLogger().severe(
                    "Failed to upload order JSON: " + e.getMessage());

            return request.createResponseBuilder(
                            HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload order JSON to Azure Blob Storage")
                    .build();
        }
    }
}
