
package com.chtrembl.petstore.pet.repository;

import com.chtrembl.petstore.pet.model.Category;
import com.chtrembl.petstore.pet.model.Pet;
import com.chtrembl.petstore.pet.model.Tag;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PetRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String SELECT_COLUMNS =
            "SELECT id, category, name, photo_url, tags, status FROM pets";

    public List<Pet> findAll() {
        return jdbcTemplate.query(
                SELECT_COLUMNS + " ORDER BY id",
                (rs, rowNum) -> mapPet(rs.getLong("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getString("photo_url"),
                        rs.getString("tags"),
                        rs.getString("status"))
        );
    }

    public Optional<Pet> findById(Long id) {
        List<Pet> pets = jdbcTemplate.query(
                SELECT_COLUMNS + " WHERE id = ?",
                (rs, rowNum) -> mapPet(rs.getLong("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getString("photo_url"),
                        rs.getString("tags"),
                        rs.getString("status")),
                id
        );

        return pets.stream().findFirst();
    }

    public List<Pet> findByStatus(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(
                ",", java.util.Collections.nCopies(statuses.size(), "?"));

        return jdbcTemplate.query(
                SELECT_COLUMNS + " WHERE status IN (" + placeholders + ") ORDER BY id",
                (rs, rowNum) -> mapPet(rs.getLong("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getString("photo_url"),
                        rs.getString("tags"),
                        rs.getString("status")),
                statuses.toArray()
        );
    }

    public void save(Pet pet) {
        try {
            String categoryJson = objectMapper.writeValueAsString(pet.getCategory());
            String tagsJson = objectMapper.writeValueAsString(pet.getTags());

            jdbcTemplate.update("""
                    INSERT INTO pets (id, category, name, photo_url, tags, status)
                    VALUES (?, ?::jsonb, ?, ?, ?::jsonb, ?)
                    ON CONFLICT (id) DO UPDATE SET
                        category = EXCLUDED.category,
                        name = EXCLUDED.name,
                        photo_url = EXCLUDED.photo_url,
                        tags = EXCLUDED.tags,
                        status = EXCLUDED.status
                    """,
                    pet.getId(),
                    categoryJson,
                    pet.getName(),
                    pet.getPhotoURL(),
                    tagsJson,
                    pet.getStatus() != null ? pet.getStatus().getValue() : null
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save pet: " + pet.getId(), e);
        }
    }

    private Pet mapPet(
            Long id,
            String categoryJson,
            String name,
            String photoUrl,
            String tagsJson,
            String status
    ) {
        try {
            Category category = categoryJson == null
                    ? null
                    : objectMapper.readValue(categoryJson, Category.class);

            List<Tag> tags = tagsJson == null
                    ? List.of()
                    : objectMapper.readValue(tagsJson, new TypeReference<List<Tag>>() {});

            return Pet.builder()
                    .id(id)
                    .category(category)
                    .name(name)
                    .photoURL(photoUrl)
                    .tags(tags)
                    .status(status == null ? null : Pet.Status.fromValue(status))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to map pet: " + id, e);
        }
    }
}
