package org.example.mediaservice.repository;

import org.example.mediaservice.model.Media;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaRepository extends MongoRepository<Media, String> {
    Optional<Media> findByKey(String key);
}
