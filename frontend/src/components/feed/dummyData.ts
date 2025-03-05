import { components } from '../../lib/backend/apiV1/schema';

type FeedListResponse = components["schemas"]["FeedListResponse"];

// 더미 데이터
export const dummyFeeds: FeedListResponse = {
  feedList: [
    {
      authorId: 1,
      authorName: "김민준",
      postId: 101,
      imgUrlList: [
        "https://images.unsplash.com/photo-1682687220923-c58b9a4592ea",
        "https://images.unsplash.com/photo-1682695796954-bad0d0f59ff1"
      ],
      content: "오늘 친구들과 홍대에서 만났어요. 맛있는 음식도 먹고 좋은 시간을 보냈습니다! 다음에 또 만나자~ #홍대 #주말 #친구들 #맛집탐방",
      likeCount: 42,
      commentCount: 7,
      createdDate: "2025-02-25T14:30:00",
      hashTagList: ["홍대", "주말", "친구들", "맛집탐방"],
      bookmarkId: undefined
    },
    {
      authorId: 2,
      authorName: "이지은",
      postId: 102,
      imgUrlList: [
        "https://images.unsplash.com/photo-1533050487297-09b450131914"
      ],
      content: "새로 산 카메라로 찍은 사진들! 아직 익숙하지 않지만 열심히 연습중이에요~ 카메라 좋아하시는 분들 팁 있으면 알려주세요 :)",
      likeCount: 85,
      commentCount: 12,
      createdDate: "2025-02-24T09:15:00",
      hashTagList: ["사진", "카메라", "취미"],
      bookmarkId: 305
    },
    {
      authorId: 3,
      authorName: "박서준",
      postId: 103,
      imgUrlList: [
        "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba",
        "https://images.unsplash.com/photo-1574144113084-b6f450cc5e0f",
        "https://images.unsplash.com/photo-1533743983669-94fa5c4338ec"
      ],
      content: "우리 고양이 나비에요~ 귀엽죠? 입양한지 1주년이 되어서 특별한 간식을 줬어요. 너무 잘 먹어서 기분이 좋네요 ㅎㅎ 고양이 좋아하시는 분들 소통해요!",
      likeCount: 156,
      commentCount: 23,
      createdDate: "2025-02-23T18:45:00",
      hashTagList: ["고양이", "집사", "반려동물", "입양"],
      bookmarkId: undefined
    },
    {
      authorId: 4,
      authorName: "최수아",
      postId: 104,
      imgUrlList: [],
      content: "오늘은 정말 피곤한 하루였어요... 일이 너무 많아서 정신이 없네요. 퇴근하고 집에 와서 바로 잘 것 같아요. 다들 오늘 하루 어떻게 보내셨나요?",
      likeCount: 27,
      commentCount: 15,
      createdDate: "2025-02-23T21:10:00",
      hashTagList: ["일상", "퇴근", "피곤"],
      bookmarkId: undefined
    },
    {
      authorId: 5,
      authorName: "정도윤",
      postId: 105,
      imgUrlList: [
        "https://images.unsplash.com/photo-1541534741688-6078c6bfb5c5"
      ],
      content: "새로 시작한 헬스 루틴을 공유합니다! 3개월째 꾸준히 하고 있는데 효과가 조금씩 나타나는 것 같아요. 다들 건강관리 어떻게 하고 계신가요?",
      likeCount: 78,
      commentCount: 32,
      createdDate: "2025-02-22T06:30:00",
      hashTagList: ["헬스", "운동", "다이어트", "건강"],
      bookmarkId: 306
    },
    {
      authorId: 6,
      authorName: "한지민",
      postId: 106,
      imgUrlList: [
        "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe",
        "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38"
      ],
      content: "직접 만든 요리! 스테이크와 파스타를 처음으로 만들어봤는데 생각보다 잘된 것 같아요. 요리 좋아하시는 분들 팁 부탁드려요~ #집밥 #홈쿡 #요리스타그램",
      likeCount: 112,
      commentCount: 19,
      createdDate: "2025-02-21T19:25:00",
      hashTagList: ["집밥", "홈쿡", "요리스타그램", "푸드"],
      bookmarkId: undefined
    },
    {
      authorId: 7,
      authorName: "송민호",
      postId: 107,
      imgUrlList: [
        "https://images.unsplash.com/photo-1503220317375-aaad61436b1b"
      ],
      content: "제주도 여행 중! 정말 아름다운 풍경이에요. 날씨도 좋고 바다도 너무 예뻐요. 제주도 여행 오시는 분들께 추천하는 명소는 성산일출봉, 우도, 한라산입니다. 꼭 가보세요!",
      likeCount: 203,
      commentCount: 41,
      createdDate: "2025-02-20T12:40:00",
      hashTagList: ["제주도", "여행", "바다", "휴가"],
      bookmarkId: 307
    },
    {
      authorId: 8,
      authorName: "황예지",
      postId: 108,
      imgUrlList: [
        "https://images.unsplash.com/photo-1534131707746-25d604851a1f"
      ],
      content: "오늘 첫 전시회를 열었어요! 많은 분들이 와주셔서 정말 감사했습니다. 앞으로도 더 좋은 작품으로 찾아뵙겠습니다. 전시는 이번 달 말까지 진행됩니다. 관심 있으신 분들은 DM 주세요!",
      likeCount: 167,
      commentCount: 28,
      createdDate: "2025-02-19T17:55:00",
      hashTagList: ["전시회", "아트", "그림", "작가"],
      bookmarkId: undefined
    },
    {
      authorId: 9,
      authorName: "강현우",
      postId: 109,
      imgUrlList: [
        "https://images.unsplash.com/photo-1500989145603-8e7ef71d639e",
        "https://images.unsplash.com/photo-1602934585418-f588bea4215c"
      ],
      content: "새로운 프로젝트를 시작했어요! 개발자분들과 함께 앱을 만들고 있는데, 정말 재미있습니다. 출시하면 또 공유할게요. 기대해주세요! #개발 #프로그래밍 #앱개발",
      likeCount: 91,
      commentCount: 14,
      createdDate: "2025-02-18T10:05:00",
      hashTagList: ["개발", "프로그래밍", "앱개발", "프로젝트"],
      bookmarkId: undefined
    },
    {
      authorId: 10,
      authorName: "임수진",
      postId: 110,
      imgUrlList: [],
      content: "오늘은 책읽기 챌린지 30일째! 하루에 30페이지씩 읽고 있는데 생각보다 꾸준히 하니 많은 책을 읽게 되네요. 여러분도 도전해보세요~ 지금 읽고 있는 책은 '사피엔스'입니다. 정말 추천합니다!",
      likeCount: 64,
      commentCount: 22,
      createdDate: "2025-02-17T22:15:00",
      hashTagList: ["독서", "책", "챌린지", "사피엔스"],
      bookmarkId: 308
    }
  ],
  lastTimestamp: "2025-02-17T22:15:00",
  lastPostId: 110
};