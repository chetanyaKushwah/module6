package com.chtrembl.petstore.order.repository;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.chtrembl.petstore.order.model.Order;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final CosmosContainer cosmosContainer;
    private final ObjectMapper objectMapper;

    public Order save(Order order) {
        try {
            JsonNode json = objectMapper.valueToTree(order);

            cosmosContainer.upsertItem(
                    json,
                    new PartitionKey(order.getId()),
                    new CosmosItemRequestOptions()
            );

            return order;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to save order: " + order.getId(), e);
        }
    }

    public Optional<Order> findById(String orderId) {
        try {
            var response = cosmosContainer.readItem(
                    orderId,
                    new PartitionKey(orderId),
                    new CosmosItemRequestOptions(),
                    JsonNode.class
            );

            Order order = objectMapper.treeToValue(
                    response.getItem(), Order.class);

            return Optional.of(order);

        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                return Optional.empty();
            }
            throw new RuntimeException(
                    "Failed to retrieve order: " + orderId, e);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to deserialize order: " + orderId, e);
        }
    }
}
