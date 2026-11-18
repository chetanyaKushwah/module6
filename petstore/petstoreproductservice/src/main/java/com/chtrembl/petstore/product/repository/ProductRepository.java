
package com.chtrembl.petstore.product.repository;

import com.chtrembl.petstore.product.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.chtrembl.petstore.product.model.Category;
import com.chtrembl.petstore.product.model.Tag;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public List<Product> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM products ORDER BY id",
                this::mapProduct
        );
    }

    public Optional<Product> findById(Long id) {
        List<Product> products = jdbcTemplate.query(
                "SELECT * FROM products WHERE id = ?",
                this::mapProduct,
                id
        );
        return products.stream().findFirst();
    }

    public List<Product> findByStatus(List<String> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        String placeholders = String.join(
                ",", java.util.Collections.nCopies(statuses.size(), "?")
        );

        return jdbcTemplate.query(
                "SELECT * FROM products WHERE status IN (" + placeholders + ") ORDER BY id",
                this::mapProduct,
                statuses.toArray()
        );
    }

    public void save(Product product) {
        try {
            String categoryJson = objectMapper.writeValueAsString(product.getCategory());
            String tagsJson = objectMapper.writeValueAsString(product.getTags());

            jdbcTemplate.update("""
                    INSERT INTO products
                    (id, category, name, photo_url, tags, status)
                    VALUES (?, CAST(? AS JSONB), ?, ?, CAST(? AS JSONB), ?)
                    ON CONFLICT (id) DO UPDATE SET
                    category = EXCLUDED.category,
                    name = EXCLUDED.name,
                    photo_url = EXCLUDED.photo_url,
                    tags = EXCLUDED.tags,
                    status = EXCLUDED.status
                    """,
                    product.getId(),
                    categoryJson,
                    product.getName(),
                    product.getPhotoURL(),
                    tagsJson,
                    product.getStatus() != null
                            ? product.getStatus().getValue() : null
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize product JSON", e);
        }
    }

    private Product mapProduct(ResultSet rs, int rowNum) throws SQLException {
        try {
            Product product = new Product();
            product.setId(rs.getLong("id"));
            product.setCategory(
                    objectMapper.readValue(
                            rs.getString("category"),
                            Category.class
                    )
            );
            product.setName(rs.getString("name"));
            product.setPhotoURL(rs.getString("photo_url"));
            product.setTags(
                    objectMapper.readValue(
                            rs.getString("tags"),
                            new TypeReference<List<Tag>>() {}
                    )
            );

            String status = rs.getString("status");
            if (status != null) {
                product.setStatus(Product.Status.fromValue(status));
            }

            return product;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to deserialize product JSON", e);
        }
    }
}
