package com.example.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFollowEntity is a Querydsl query type for FollowEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFollowEntity extends EntityPathBase<FollowEntity> {

    private static final long serialVersionUID = 818657396L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFollowEntity followEntity = new QFollowEntity("followEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final QMemberEntity receiver;

    public final QMemberEntity sender;

    public QFollowEntity(String variable) {
        this(FollowEntity.class, forVariable(variable), INITS);
    }

    public QFollowEntity(Path<? extends FollowEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFollowEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFollowEntity(PathMetadata metadata, PathInits inits) {
        this(FollowEntity.class, metadata, inits);
    }

    public QFollowEntity(Class<? extends FollowEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.receiver = inits.isInitialized("receiver") ? new QMemberEntity(forProperty("receiver")) : null;
        this.sender = inits.isInitialized("sender") ? new QMemberEntity(forProperty("sender")) : null;
    }

}

