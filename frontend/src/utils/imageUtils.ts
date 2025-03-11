export const getImageUrl = (imgUrl?: string | null): string => {
  // 이미지 URL이 undefined, null 또는 빈 문자열인 경우 기본 이미지 반환
  if (!imgUrl || imgUrl === '') {
    console.log("이미지가 없습니다.");
    return 'http://localhost/uploads/defaultPostImage.png';
  }
  
  // HTTP 또는 HTTPS로 시작하는 경우 원본 URL 반환
  if (imgUrl.startsWith('http://') || imgUrl.startsWith('https://')) {
    return imgUrl;
  }

  // 로컬 이미지인 경우 기본 경로 추가
  return `http://localhost/uploads/${imgUrl}`;
};