import FeedDetail from "@/components/feed/FeedDetail";

export default async function FeedPage({ params }: { params: { id: string } }) {
  return <FeedDetail id={params.id} />;
}
