import { cookies } from 'next/headers';
import LandingPage from '@/components/LandingPage';
import Feed from '../components/Feed';

export default async function HomePage() {
  const cookieStore = await cookies();
  const hasRefreshToken = cookieStore.has('refresh_token');

  return hasRefreshToken ? <Feed /> : <LandingPage />;
}
