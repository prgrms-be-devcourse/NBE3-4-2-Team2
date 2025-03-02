import FeedDetail from "@/components/feed/FeedDetail";

export default async function FeedPage({ params }: { params: { id: string } }) {
  const id = params.id;

  console.log("피드 페이지 렌더링, ID:", id);

  return <FeedDetail id={id} />;
}
