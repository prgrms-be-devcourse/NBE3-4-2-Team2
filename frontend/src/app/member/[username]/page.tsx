import { components } from '@/lib/backend/apiV1/schema';
import ProfileClient from './ProfileClient';
type MemberResponse = components['schemas']['MemberResponse'];
type PostResponse = components['schemas']['SearchPostResponse'];


/**
 * 1. api-v1/members/{username}으로 MemberResponse 조회
 * 
 */

export default async function Page({params,} : {
    params: Promise<{username: string}>
}) {
  const {username} = await params;
  return <div className="container mx-auto py-24 flex justify-center">
    <ProfileClient username = {username}></ProfileClient>
  </div>
}
















// export default async function Page({params,} : {
//   params: Promise<{username: string}>
// }) {
//   const {username} = await params;

//   // const user : MemberResponse = {
//   //   username: 'K-haechan',
//   //   profileUrl: 'https://via.placeholder.com/150',
//   //   followerCount: 120,
//   //   followingCount: 75,
//   //   postCount: 40, // 전체 게시물 수
//   // };


//   const _user = await client.GET('/api-v1/members/{username}', {
//     params : {
//       path : {
//         username
//       }
//     }
//   })

//   console.log("_user!!!==",_user);



//   const user:MemberResponse = _user.data?.data!;

//   return (
//     <div className="container mx-auto py-24 flex justify-center">
//       <div className="bg-white p-8 rounded-lg shadow-lg w-full max-w-md flex flex-col">
//         {/* 프로필 정보 */}
//         <div className="flex flex-col items-center">
//           <img
//             src={user.profileUrl||'khc.jpg'}
//             alt="User Avatar"
//             className="w-32 h-32 rounded-full mb-4"
//           />
//           <h1 className="text-2xl font-bold text-gray-900">{user.username}</h1>
//           <div className="flex justify-around text-center text-gray-600 w-full my-4">
//             <div>
//               <p className="font-bold text-gray-900">{user.postCount}</p>
//               <p>Posts</p>
//             </div>
//             <div>
//               <p className="font-bold text-gray-900">{user.followerCount}</p>
//               <p>Followers</p>
//             </div>
//             <div>
//               <p className="font-bold text-gray-900">{user.followingCount}</p>
//               <p>Following</p>
//             </div>
//           </div>
//         </div>

//         {/* 게시물 섹션 */}
//         <div className="mt-6">
//           <h2 className="text-lg font-bold mb-2 text-gray-900">Posts</h2>
//           <div
//             // ref={postsContainerRef}
//             className="posts-container overflow-y-auto max-h-[600px] grid grid-cols-3 gap-2"
//           >
//             {/* {posts.map((post, index) => (
//               <div key={index} className="bg-gray-200 p-1 rounded-lg">
//                 <img
//                   src={post}
//                   alt={`Post ${index + 1}`}
//                   className="w-full h-40 object-cover rounded-md hover:opacity-80 transition"
//                 />
//               </div>
//             ))} */}
//           </div>

//           {/* {loading && (
//             <div className="flex justify-center py-4">
//               <div className="w-6 h-6 border-4 border-gray-300 border-t-gray-600 rounded-full animate-spin"></div>
//             </div>
//           )}

//           {posts.length >= user.postCount! && posts.length > 0 && (
//             <div className="text-center text-gray-500 mt-4">
//               모든 포스트를 불러왔습니다.
//             </div>
//           )} */}
//         </div>
//       </div>
//     </div>
//   );
// }
  
































// // app/page.tsx
// import { useAuth } from '@/contexts/AuthContext';
// import { parseAccessToken } from '@/lib/auth/token';
// import { components } from '@/lib/backend/apiV1/schema';
// import client from '@/lib/backend/client';
// import { useEffect, useState } from 'react';
// import ProfileClient from './ProfileClient';

// 더미 데이터 (실제로는 API나 DB에서 가져올 수 있음)
// const user = {
//   username: 'K-haechan',
//   avatarUrl: 'https://via.placeholder.com/150',
//   followers: 120,
//   following: 75,
//   totalPosts: 40, // 전체 게시물 수
// };

// type MemberResponse = components['schemas']['MemberResponse'];

// parseAccessToken(localStorage.getItem('accessToken') ?? undefined); // TODO : localStorage말고 Context?
// export default function Page({ username }: { username: string }) {
//   const [user, setUser] = useState<MemberResponse | null>(null);
//   const { accessToken } = useAuth();

//   useEffect(() => {
//     async function fetchUser() {
//       let _user;

//       if (accessToken) {
//         _user = parseAccessToken(accessToken).me;
//       }

//       if (_user && username !== _user.username) {
//         const response = await client.GET('/api-v1/members/{username}', { params: { path: { username } } });
//         if (!response.data) {
//           throw new Error(`API 응답 오류: ${response.error}`);
//         }
//         _user = response.data.data;
//       }

//       if (!_user) {
//         throw new Error('해당 이름을 가진 사용자가 존재하지 않습니다');
//       }

//       setUser(_user);
//     }

//     fetchUser();
//   }, [username, accessToken]); // username 또는 accessToken이 변경될 때 실행

//   if (!user) {
//     return <div>로딩 중...</div>; // 데이터가 올 때까지 로딩 상태 표시
//   }

//   return (
//     <div className="min-h-screen bg-gray-100">
//       <ProfileClient user={user} />
//     </div>
//   );
// }