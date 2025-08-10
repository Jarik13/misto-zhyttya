package org.example.mediaservice.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "medias")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    @Id
    private String id;

    @Field(name = "filename")
    private String filename;

    @Field(name = "content_type")
    private String contentType;

    @Field(name = "url")
    private String url;

    @Field(name = "size")
    private long size;

    @CreatedDate
    @Field(name = "uploaded_at")
    private Instant uploadedAt;
}
