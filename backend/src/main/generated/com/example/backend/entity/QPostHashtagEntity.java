package com.example.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPostHashtagEntity is a Querydsl query type for PostHashtagEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPostHashtagEntity extends EntityPathBase<PostHashtagEntity> {

    private static final long serialVersionUID = -918710129L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPostHashtagEntity postHashtagEntity = new QPostHashtagEntity("postHashtagEntity");

    public final QHashtagEntity hashtag;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPostEntity post;

    public QPostHashtagEntity(String variable) {
        this(PostHashtagEntity.class, forVariable(variable), INITS);
    }

    public QPostHashtagEntity(Path<? extends PostHashtagEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPostHashtagEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPostHashtagEntity(PathMetadata metadata, PathInits inits) {
        this(PostHashtagEntity.class, metadata, inits);
    }

    public QPostHashtagEntity(Class<? extends PostHashtagEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.hashtag = inits.isInitialized("hashtag") ? new QHashtagEntity(forProperty("hashtag")) : null;
        this.post = inits.isInitialized("post") ? new QPostEntity(forProperty("post"), inits.get("post")) : null;
    }

}

