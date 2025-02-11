package com.example.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotificationEntity is a Querydsl query type for NotificationEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotificationEntity extends EntityPathBase<NotificationEntity> {

    private static final long serialVersionUID = 1291227694L;

    public static final QNotificationEntity notificationEntity = new QNotificationEntity("notificationEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isRead = createBoolean("isRead");

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final NumberPath<Long> targetId = createNumber("targetId", Long.class);

    public final EnumPath<com.example.backend.content.notification.type.NotificationType> type = createEnum("type", com.example.backend.content.notification.type.NotificationType.class);

    public QNotificationEntity(String variable) {
        super(NotificationEntity.class, forVariable(variable));
    }

    public QNotificationEntity(Path<? extends NotificationEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotificationEntity(PathMetadata metadata) {
        super(NotificationEntity.class, metadata);
    }

}

