import LoginForm from "./LoginForm";

export default function LoginPage() {
  return (
    <div className="min-h-screen bg-gray-100">
      <div className="container mx-auto py-24">
        <div className="flex justify-center">
          <LoginForm />
        </div>
      </div>
    </div>
  );
} 