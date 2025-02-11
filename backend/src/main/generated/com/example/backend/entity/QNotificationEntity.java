package com.example.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotificationEntity is a Querydsl query type for NotificationEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationEntity extends EntityPathBase<NotificationEntity> {

    private static final long serialVersionUID = 1291227694L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotificationEntity notificationEntity = new QNotificationEntity("notificationEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isRead = createBoolean("isRead");

    public final QMemberEntity member;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public QNotificationEntity(String variable) {
        this(NotificationEntity.class, forVariable(variable), INITS);
    }

    public QNotificationEntity(Path<? extends NotificationEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotificationEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotificationEntity(PathMetadata metadata, PathInits inits) {
        this(NotificationEntity.class, metadata, inits);
    }

    public QNotificationEntity(Class<? extends NotificationEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMemberEntity(forProperty("member")) : null;
    }

}

