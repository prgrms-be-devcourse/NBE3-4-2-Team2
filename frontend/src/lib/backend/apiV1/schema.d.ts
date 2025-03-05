/**
 * This file was auto-generated by openapi-typescript.
 * Do not make direct changes to the file.
 */

export interface paths {
    "/api-v1/post/{postId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["modifyPost"];
        post?: never;
        delete: operations["deletePost"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/comment/{commentId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put: operations["modifyComment"];
        post?: never;
        delete: operations["deleteComment"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/post": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["createPost"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/members/login": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * 로그인
         * @description username, password를 받아 로그인을 진행합니다.
         */
        post: operations["login"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/members/join": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * 회원가입
         * @description username, password, email을 받아 회원가입을 진행합니다.
         */
        post: operations["join"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/likes/{postId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * 게시물 좋아요 요청
         * @description 게시물에 좋아요 요청을 보냅니다.
         */
        post: operations["likePost"];
        /**
         * 게시물 좋아요 취소
         * @description 게시물에 좋아요 취소 요청을 보냅니다.
         */
        delete: operations["unlikePost"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/follow/{receiverId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * 상대방 팔로우 요청
         * @description 상대 멤버와 팔로우 관계를 맺습니다.
         */
        post: operations["followMember"];
        /**
         * 상대방 팔로우 취소
         * @description 상대 멤버와 팔로우 관계를 끊습니다.
         */
        delete: operations["unfollowMember"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/comment": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post: operations["createComment"];
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/bookmark/{postId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        /**
         * 게시물 북마크 등록
         * @description 게시물을 자신의 북마크에 등록합니다.
         */
        post: operations["addBookmarkPost"];
        /**
         * 게시물 북마크 삭제
         * @description 게시물을 자신의 북마크에서 삭제합니다.
         */
        delete: operations["removeBookmarkPost"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/search": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["search"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/members/{id}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * 멤버 정보 조회
         * @description 멤버의 정보를 조회합니다.
         */
        get: operations["publicMemberDetails"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/follow/mutual/{memberId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * 맞팔로우 확인
         * @description 상대 멤버와 서로 팔로우 관계인지 확인합니다.
         */
        get: operations["isMutualFollow"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/feed": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * 메인 피드 요청
         * @description 자신 및 팔로잉 게시물과 추천 게시물로 이뤄진 피드를 반환합니다.
         */
        get: operations["findFeedList"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/feed/{postId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * 단건 피드 요청
         * @description 단건 게시물을 피드로 반환합니다.
         */
        get: operations["findFeedInfo"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/feed/member": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        /**
         * 멤버 피드 요청
         * @description 해당 멤버의 게시물에 대한 피드를 요청합니다.
         */
        get: operations["findMemberFeedList"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/comment/replies/{parentId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getReplies"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/comment/post/{postId}": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get: operations["getComments"];
        put?: never;
        post?: never;
        delete?: never;
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
    "/api-v1/members/logout": {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        get?: never;
        put?: never;
        post?: never;
        /**
         * 로그아웃
         * @description 로그아웃을 진행합니다.
         */
        delete: operations["logout"];
        options?: never;
        head?: never;
        patch?: never;
        trace?: never;
    };
}
export type webhooks = Record<string, never>;
export interface components {
    schemas: {
        PostModifyRequest: {
            /** Format: int64 */
            postId: number;
            content: string;
            /** Format: int64 */
            memberId: number;
            images?: string[];
        };
        PostModifyResponse: {
            /** Format: int64 */
            id: number;
            content: string;
            /** Format: int64 */
            memberId: number;
        };
        CommentModifyRequest: {
            /** Format: int64 */
            commentId: number;
            /** Format: int64 */
            memberId: number;
            content: string;
        };
        CommentModifyResponse: {
            /** Format: int64 */
            id?: number;
            content?: string;
            /** Format: int64 */
            postId?: number;
            /** Format: int64 */
            memberId?: number;
        };
        PostCreateRequest: {
            /** Format: int64 */
            memberId: number;
            content: string;
            images?: string[];
        };
        PostCreateResponse: {
            /** Format: int64 */
            id: number;
            content: string;
            /** Format: int64 */
            memberId: number;
        };
        MemberLoginRequest: {
            /**
             * @description 유저 이름
             * @example testuser
             */
            username: string;
            /**
             * @description 비밀번호
             * @example @test1234!@
             */
            password: string;
        };
        MemberLoginResponse: {
            /** Format: int64 */
            id?: number;
            username?: string;
            profileUrl?: string;
        };
        RsDataMemberLoginResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["MemberLoginResponse"];
            success?: boolean;
        };
        MemberJoinRequest: {
            /**
             * @description 이메일
             * @example testuser@naver.com
             */
            email: string;
            /**
             * @description 비밀번호
             * @example @test1234!@
             */
            password: string;
            /**
             * @description 유저 이름
             * @example testuser
             */
            username: string;
        };
        MemberJoinResponse: {
            /** Format: int64 */
            id?: number;
            username?: string;
        };
        RsDataMemberJoinResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["MemberJoinResponse"];
            success?: boolean;
        };
        CreateLikeResponse: {
            /** Format: int64 */
            likeId?: number;
            /** Format: int64 */
            memberId?: number;
            /** Format: int64 */
            postId?: number;
            /** Format: date-time */
            createDate?: string;
        };
        RsDataCreateLikeResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["CreateLikeResponse"];
            success?: boolean;
        };
        CreateFollowResponse: {
            /** Format: int64 */
            followId?: number;
            /** Format: int64 */
            senderId?: number;
            /** Format: int64 */
            receiverId?: number;
            /** Format: date-time */
            createDate?: string;
        };
        RsDataCreateFollowResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["CreateFollowResponse"];
            success?: boolean;
        };
        CommentCreateRequest: {
            /** Format: int64 */
            postId: number;
            /** Format: int64 */
            memberId: number;
            content: string;
            /** Format: int64 */
            parentId?: number;
        };
        CommentCreateResponse: {
            /** Format: int64 */
            id?: number;
            content?: string;
            /** Format: int64 */
            ref?: number;
            /** Format: int64 */
            postId?: number;
            /** Format: int64 */
            memberId?: number;
            /** Format: int64 */
            parentId?: number;
        };
        CreateBookmarkResponse: {
            /** Format: int64 */
            bookmarkId?: number;
            /** Format: int64 */
            memberId?: number;
            /** Format: int64 */
            postId?: number;
            /** Format: date-time */
            createDate?: string;
        };
        RsDataCreateBookmarkResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["CreateBookmarkResponse"];
            success?: boolean;
        };
        RsDataSearchPostCursorResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["SearchPostCursorResponse"];
            success?: boolean;
        };
        SearchPostCursorResponse: {
            searchPostResponses?: components["schemas"]["SearchPostResponse"][];
            /** Format: int64 */
            lastPostId?: number;
            hasNext?: boolean;
        };
        SearchPostResponse: {
            /** Format: int64 */
            postId?: number;
            imageUrl?: string;
        };
        MemberResponse: {
            /** Format: int64 */
            id?: number;
            username?: string;
            profileUrl?: string;
            /** Format: int64 */
            followerCount?: number;
            /** Format: int64 */
            followingCount?: number;
        };
        RsDataMemberResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["MemberResponse"];
            success?: boolean;
        };
        MutualFollowResponse: {
            isMutualFollow?: boolean;
        };
        RsDataMutualFollowResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["MutualFollowResponse"];
            success?: boolean;
        };
        FeedRequest: {
            /** Format: date-time */
            timestamp?: string;
            /** Format: int64 */
            lastPostId?: number;
            /** Format: int32 */
            maxSize?: number;
        };
        FeedInfoResponse: {
            /** Format: int64 */
            authorId?: number;
            authorName?: string;
            /** Format: int64 */
            postId?: number;
            imgUrlList?: string[];
            content?: string;
            /** Format: int64 */
            likesCount?: number;
            /** Format: int64 */
            commentCount?: number;
            /** Format: date-time */
            createdDate?: string;
            hashTagList?: string[];
            /** Format: int64 */
            bookmarkId?: number;
        };
        FeedListResponse: {
            feedList?: components["schemas"]["FeedInfoResponse"][];
            /** Format: date-time */
            lastTimestamp?: string;
            /** Format: int64 */
            lastPostId?: number;
        };
        RsDataFeedListResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["FeedListResponse"];
            success?: boolean;
        };
        RsDataFeedInfoResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["FeedInfoResponse"];
            success?: boolean;
        };
        FeedMemberRequest: {
            /** Format: int64 */
            lastPostId?: number;
            /** Format: int32 */
            maxSize?: number;
        };
        FeedMemberResponse: {
            feedList?: components["schemas"]["FeedInfoResponse"][];
            /** Format: int64 */
            lastPostId?: number;
        };
        RsDataFeedMemberResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["FeedMemberResponse"];
            success?: boolean;
        };
        CommentResponse: {
            /** Format: int64 */
            id?: number;
            content?: string;
            username?: string;
            /** Format: int64 */
            postId?: number;
            /** Format: date-time */
            createdAt?: string;
            /** Format: int32 */
            step?: number;
            /** Format: int32 */
            refOrder?: number;
            /** Format: int64 */
            ref?: number;
        };
        PageCommentResponse: {
            /** Format: int32 */
            totalPages?: number;
            /** Format: int64 */
            totalElements?: number;
            first?: boolean;
            last?: boolean;
            /** Format: int32 */
            size?: number;
            content?: components["schemas"]["CommentResponse"][];
            /** Format: int32 */
            number?: number;
            sort?: components["schemas"]["Sortnull"];
            pageable?: components["schemas"]["Pageablenull"];
            /** Format: int32 */
            numberOfElements?: number;
            empty?: boolean;
        };
        Pageablenull: {
            /** Format: int64 */
            offset?: number;
            sort?: components["schemas"]["Sortnull"];
            paged?: boolean;
            /** Format: int32 */
            pageNumber?: number;
            /** Format: int32 */
            pageSize?: number;
            unpaged?: boolean;
        };
        Sortnull: {
            empty?: boolean;
            sorted?: boolean;
            unsorted?: boolean;
        };
        PostDeleteResponse: {
            /** Format: int64 */
            postId: number;
            message: string;
        };
        RsDataVoid: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: Record<string, never>;
            success?: boolean;
        };
        DeleteLikeRequest: {
            /** Format: int64 */
            likeId: number;
        };
        DeleteLikeResponse: {
            /** Format: int64 */
            likeId?: number;
            /** Format: int64 */
            memberId?: number;
            /** Format: int64 */
            postId?: number;
            /** Format: date-time */
            deleteDate?: string;
        };
        RsDataDeleteLikeResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["DeleteLikeResponse"];
            success?: boolean;
        };
        DeleteFollowRequest: {
            /** Format: int64 */
            followId: number;
        };
        DeleteFollowResponse: {
            /** Format: int64 */
            followId?: number;
            /** Format: int64 */
            senderId?: number;
            /** Format: int64 */
            receiverId?: number;
            /** Format: date-time */
            deleteDate?: string;
        };
        RsDataDeleteFollowResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["DeleteFollowResponse"];
            success?: boolean;
        };
        CommentDeleteResponse: {
            /** Format: int64 */
            id?: number;
            /** Format: int64 */
            memberId?: number;
            message?: string;
        };
        DeleteBookmarkRequest: {
            /** Format: int64 */
            bookmarkId: number;
        };
        DeleteBookmarkResponse: {
            /** Format: int64 */
            bookmarkId?: number;
            /** Format: int64 */
            memberId?: number;
            /** Format: int64 */
            postId?: number;
            /** Format: date-time */
            deleteDate?: string;
        };
        RsDataDeleteBookmarkResponse: {
            /** Format: date-time */
            time?: string;
            message?: string;
            data?: components["schemas"]["DeleteBookmarkResponse"];
            success?: boolean;
        };
    };
    responses: never;
    parameters: never;
    requestBodies: never;
    headers: never;
    pathItems: never;
}
export type $defs = Record<string, never>;
export interface operations {
    modifyPost: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                postId: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["PostModifyRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["PostModifyResponse"];
                };
            };
        };
    };
    deletePost: {
        parameters: {
            query: {
                memberId: number;
            };
            header?: never;
            path: {
                postId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["PostDeleteResponse"];
                };
            };
        };
    };
    modifyComment: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                commentId: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CommentModifyRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["CommentModifyResponse"];
                };
            };
        };
    };
    deleteComment: {
        parameters: {
            query: {
                memberId: number;
            };
            header?: never;
            path: {
                commentId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["CommentDeleteResponse"];
                };
            };
        };
    };
    createPost: {
        parameters: {
            query: {
                request: components["schemas"]["PostCreateRequest"];
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["PostCreateResponse"];
                };
            };
        };
    };
    login: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["MemberLoginRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["RsDataMemberLoginResponse"];
                };
            };
        };
    };
    join: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["MemberJoinRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["RsDataMemberJoinResponse"];
                };
            };
        };
    };
    likePost: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                postId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["RsDataCreateLikeResponse"];
                };
            };
        };
    };
    unlikePost: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                postId: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["DeleteLikeRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["RsDataDeleteLikeResponse"];
                };
            };
        };
    };
    followMember: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                receiverId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["RsDataCreateFollowResponse"];
                };
            };
        };
    };
    unfollowMember: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                receiverId: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["DeleteFollowRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["RsDataDeleteFollowResponse"];
                };
            };
        };
    };
    createComment: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["CommentCreateRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["CommentCreateResponse"];
                };
            };
        };
    };
    addBookmarkPost: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                postId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["RsDataCreateBookmarkResponse"];
                };
            };
        };
    };
    removeBookmarkPost: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                postId: number;
            };
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["DeleteBookmarkRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["RsDataDeleteBookmarkResponse"];
                };
            };
        };
    };
    search: {
        parameters: {
            query: {
                type: "AUTHOR" | "HASHTAG";
                keyword: string;
                lastPostId?: number;
                size?: number;
            };
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["RsDataSearchPostCursorResponse"];
                };
            };
        };
    };
    publicMemberDetails: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                id: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["RsDataMemberResponse"];
                };
            };
        };
    };
    isMutualFollow: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                memberId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json": components["schemas"]["RsDataMutualFollowResponse"];
                };
            };
        };
    };
    findFeedList: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["FeedRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["RsDataFeedListResponse"];
                };
            };
        };
    };
    findFeedInfo: {
        parameters: {
            query?: never;
            header?: never;
            path: {
                postId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["RsDataFeedInfoResponse"];
                };
            };
        };
    };
    findMemberFeedList: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody: {
            content: {
                "application/json": components["schemas"]["FeedMemberRequest"];
            };
        };
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["RsDataFeedMemberResponse"];
                };
            };
        };
    };
    getReplies: {
        parameters: {
            query?: {
                page?: number;
                size?: number;
            };
            header?: never;
            path: {
                parentId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["PageCommentResponse"];
                };
            };
        };
    };
    getComments: {
        parameters: {
            query?: {
                page?: number;
                size?: number;
            };
            header?: never;
            path: {
                postId: number;
            };
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["PageCommentResponse"];
                };
            };
        };
    };
    logout: {
        parameters: {
            query?: never;
            header?: never;
            path?: never;
            cookie?: never;
        };
        requestBody?: never;
        responses: {
            /** @description OK */
            200: {
                headers: {
                    [name: string]: unknown;
                };
                content: {
                    "application/json;charset=UTF-8": components["schemas"]["RsDataVoid"];
                };
            };
        };
    };
}
