package com.chtrembl.petstore.order.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheService {

    public int getOrdersCacheSize() {
        // Orders are persisted in Azure Cosmos DB, not in the local cache.
        return 0;
    }
}
