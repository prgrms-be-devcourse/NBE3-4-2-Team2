package com.example.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHashtagEntity is a Querydsl query type for HashtagEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHashtagEntity extends EntityPathBase<HashtagEntity> {

    private static final long serialVersionUID = 1398750543L;

    public static final QHashtagEntity hashtagEntity = new QHashtagEntity("hashtagEntity");

    public final StringPath content = createString("content");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastUsedAt = createDateTime("lastUsedAt", java.time.LocalDateTime.class);

    public QHashtagEntity(String variable) {
        super(HashtagEntity.class, forVariable(variable));
    }

    public QHashtagEntity(Path<? extends HashtagEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHashtagEntity(PathMetadata metadata) {
        super(HashtagEntity.class, metadata);
    }

}

