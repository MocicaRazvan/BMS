import TestComp from "@/components/dnd/test";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";

export default async function TestPage() {
  const session = await getServerSession(authOptions);

  const data = await fetch("http://localhost:8089/ws-http/test", {
    headers: {
      Authorization: `Bearer ${session?.user?.token}`,
    },
  }).then((res) => res.text());

  return (
    <div>
      <p>{JSON.stringify(data)}</p>
      <TestComp />
    </div>
  );
}
