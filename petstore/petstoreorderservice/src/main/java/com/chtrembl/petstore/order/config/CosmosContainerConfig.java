package com.chtrembl.petstore.order.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CosmosContainerConfig {

    @Value("${petstore.cosmos.database}")
    private String databaseName;

    @Value("${petstore.cosmos.container}")
    private String containerName;

    @Bean
    public CosmosContainer cosmosContainer(CosmosClient cosmosClient) {
        return cosmosClient
                .getDatabase(databaseName)
                .getContainer(containerName);
    }
}
