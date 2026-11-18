package com.chtrembl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class BlobStorageService {

    public void uploadOrder(String sessionId, String json) {

        String connectionString =
                System.getenv("AZURE_STORAGE_CONNECTION_STRING");

        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException(
                    "Azure Storage connection string is missing");
        }

        BlobContainerClient containerClient =
                new BlobContainerClientBuilder()
                        .connectionString(connectionString)
                        .containerName("orders")
                        .buildClient();

        if (!containerClient.exists()) {
            containerClient.create();
        }

        String fileName = sessionId + ".json";

        BlobClient blobClient =
                containerClient.getBlobClient(fileName);

        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        blobClient.upload(
                new ByteArrayInputStream(data),
                data.length,
                true
        );
    }
}
