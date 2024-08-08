"use client";
import { useEffect } from "react";
import { Button } from "@/components/ui/button";
import { signIn } from "next-auth/react";

export default function TestComp() {
  const testBackendConnection = async () => {
    // try {
    //   const response = await fetch(
    //     `${process.env.NEXT_PUBLIC_SPRING}/auth/login`,
    //     {
    //       method: "POST",
    //       body: JSON.stringify({
    //         email: "razvanmocica1@gmail.com",
    //         password: "1234",
    //       }),
    //       headers: { "Content-Type": "application/json" },
    //     },
    //   );
    //   console.log("Backend connection test response:", await response.text());
    // } catch (error) {
    //   console.error("Backend connection test error:", error);
    // }
    const result = await signIn("credentials", {
      redirect: false,
      email: "razvanmocica1@gmail.com",
      password: "1234",
    });
    console.log("Sign-in result:", result);
  };
  // useEffect(() => {
  //   testBackendConnection();
  // }, []);

  return (
    <div>
      <Button onClick={testBackendConnection}>CLICK</Button>
    </div>
  );
}
