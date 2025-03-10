export const getImageUrl = (imgUrl: string): string => {
    // HTTP 또는 HTTPS로 시작하는 경우 원본 URL 반환
    if (imgUrl.startsWith('http://') || imgUrl.startsWith('https://')) {
      return imgUrl;
    }
    
    // 로컬 이미지인 경우 기본 경로 추가
    return `http://localhost/uploads/${imgUrl}`;
  };