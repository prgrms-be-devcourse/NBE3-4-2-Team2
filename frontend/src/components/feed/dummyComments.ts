// dummyComments.ts
export interface Comment {
    id: number;
    username: string;
    content: string;
    time: string;
    likeCount: number;
  }
  
  export const dummyComments: Comment[] = [
    {
      id: 1,
      username: "이지은",
      content:
        "새로 만 카페에서 좋은 사진들이 아직 올라가지 않았네요. 일본의 골목길 모습이 정말 잘 담겼네요.",
      time: "6일 전",
      likeCount: 0,
    },
    {
      id: 2,
      username: "Marcus Hall",
      content:
        "Great composition! I love the lighting in this shot. Was this your first trip to Japan? You did an excellent job of capturing the atmosphere.",
      time: "2 hours",
      likeCount: 2,
    },
    {
      id: 3,
      username: "Dianne Russell",
      content:
        "But don't you think the timing is off because many other apps have done this even earlier, causing people to switch apps?",
      time: "53 min",
      likeCount: 1,
    },
    {
      id: 4,
      username: "Esther Howard",
      content:
        "This could be due to them taking their time to release a stable version.",
      time: "32 min",
      likeCount: 12,
    },
    {
      id: 5,
      username: "You",
      content: "피드백 감사합니다. 다음에는 더 좋은 콘텐츠로 찾아뵙겠습니다!",
      time: "Just now",
      likeCount: 0,
    },
  ];