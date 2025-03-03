import MainFeed from "@/components/feed/MainFeed";

export default function Home() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-24">
      <h1>홈페이지</h1>
      <div className="flex justify-center">
        <div className="w-full max-w-screen-xl flex justify-center px-4">
          <MainFeed />
        </div>
      </div>
    </main>
  );
}
