import BookmarkList from './bookmarkList';

export default function BookmarkPage() {
  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold mb-6">북마크</h1>
      <BookmarkList />
    </div>
  );
}