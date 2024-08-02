"use client";
import { useEffect, useState } from "react";
import { Link } from "@/navigation";

const Success = () => {
  const [queryParams, setQueryParams] = useState("");

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    setQueryParams(params.toString().split("&").join("\n"));
  }, []);

  return (
    <div className="flex justify-center items-center h-screen text-green-600">
      <div className="flex flex-col items-center space-y-3">
        <h1 className="text-4xl font-bold">Success!</h1>
        <p className="text-black whitespace-pre-line">{queryParams}</p>
        <Link
          href={"/"}
          className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
        >
          Go Home
        </Link>
      </div>
    </div>
  );
};

export default Success;
