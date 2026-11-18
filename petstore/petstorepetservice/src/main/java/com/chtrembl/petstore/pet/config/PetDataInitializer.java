
package com.chtrembl.petstore.pet.config;

import com.chtrembl.petstore.pet.model.DataPreload;
import com.chtrembl.petstore.pet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PetDataInitializer {

    private final DataPreload dataPreload;
    private final PetRepository petRepository;

    @Bean
    public ApplicationRunner initializePets() {
        return args -> {
            var pets = dataPreload.getPets();

            if (pets == null || pets.isEmpty()) {
                log.warn("No pet seed data found in application.yml");
                return;
            }

            pets.forEach(petRepository::save);

            log.info("Successfully initialized {} pets in PostgreSQL", pets.size());
        };
    }
}
