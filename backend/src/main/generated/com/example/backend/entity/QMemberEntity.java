package com.example.backend.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberEntity is a Querydsl query type for MemberEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberEntity extends EntityPathBase<MemberEntity> {

    private static final long serialVersionUID = -1475709795L;

    public static final QMemberEntity memberEntity = new QMemberEntity("memberEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final ListPath<BookmarkEntity, QBookmarkEntity> bookmarkList = this.<BookmarkEntity, QBookmarkEntity>createList("bookmarkList", BookmarkEntity.class, QBookmarkEntity.class, PathInits.DIRECT2);

    public final ListPath<CommentEntity, QCommentEntity> commentList = this.<CommentEntity, QCommentEntity>createList("commentList", CommentEntity.class, QCommentEntity.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createDate = _super.createDate;

    public final StringPath email = createString("email");

    public final ListPath<FollowEntity, QFollowEntity> followerList = this.<FollowEntity, QFollowEntity>createList("followerList", FollowEntity.class, QFollowEntity.class, PathInits.DIRECT2);

    public final ListPath<FollowEntity, QFollowEntity> followingList = this.<FollowEntity, QFollowEntity>createList("followingList", FollowEntity.class, QFollowEntity.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ListPath<LikesEntity, QLikesEntity> likeList = this.<LikesEntity, QLikesEntity>createList("likeList", LikesEntity.class, QLikesEntity.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifyDate = _super.modifyDate;

    public final ListPath<NotificationEntity, QNotificationEntity> notificationList = this.<NotificationEntity, QNotificationEntity>createList("notificationList", NotificationEntity.class, QNotificationEntity.class, PathInits.DIRECT2);

    public final StringPath password = createString("password");

    public final ListPath<PostEntity, QPostEntity> postList = this.<PostEntity, QPostEntity>createList("postList", PostEntity.class, QPostEntity.class, PathInits.DIRECT2);

    public final StringPath username = createString("username");

    public QMemberEntity(String variable) {
        super(MemberEntity.class, forVariable(variable));
    }

    public QMemberEntity(Path<? extends MemberEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMemberEntity(PathMetadata metadata) {
        super(MemberEntity.class, metadata);
    }

}

