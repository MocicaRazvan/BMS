"use client";

import { useSubscription } from "react-stomp-hooks";
import { useState } from "react";
const email = "razvanmocica1@gmail.com"; // Original email
const sanitizedEmail = email.replace("@", "-").replace(".", "-"); // Sanitize email
export default function TextComp() {
  const [messages, setMessages] = useState<any[]>([]);

  // Subscribe to the specific user's queue
  //todo schimbi de peste tot /user blblb cu -email la final mereu cu /queue/ce vrei
  // si in back trb sa ai aceleasi rute ai grija
  // user/{email}/queue/messages -> /queue/messages-{email}
  // user/{email}/queue/chat/{chatId} -> /queue/chat-{chatId}-{email} mereu email e la final
  // grija la alea cu /chat trb sa bagi queue in fata
  useSubscription(`/queue/messages-${email}`, (message) => {
    const newMessage = JSON.parse(message.body);
    console.log("Received message:", newMessage);
    setMessages((prevMessages) => [...prevMessages, newMessage]);
  });

  return (
    <div>
      <h2>User-Specific Messages:</h2>
      <ul>
        {messages.map((msg, index) => (
          <li key={index}>{msg}</li>
        ))}
      </ul>
    </div>
  );
}
