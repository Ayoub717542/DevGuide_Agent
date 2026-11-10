import { useEffect, useRef } from "react";
import ChallengeCard from "../cards/ChallengeCard";
import QuizCard from "../cards/QuizCard";
import TrelloCard from "../cards/TrelloCard";
import Message from "./Message";

const SUGGESTIONS = [
  "Walk me through my repo",
  "Explain abstract class vs interface",
  "Give me a small Java exercise",
];

export default function MessageList({ chatId, messages, pdfs, pending, answer, error, onAsk, onTrelloFinished }) {
  const endRef = useRef(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ block: "end" });
  }, [messages, answer, pending]);

  const empty = messages.length === 0 && answer === null;

  return (
    <div className="flex-1 overflow-auto">
      <div className="mx-auto flex max-w-190 flex-col gap-6.5 px-7 pt-7 pb-2">
        {empty && (
          <div className="pt-[12vh] text-center">
            <h2 className="text-[22px] font-semibold tracking-tight">What do you want to learn today?</h2>
            <p className="mt-1.5 mb-5.5 text-muted-foreground">Ask about your code, a concept, or your GitHub repo.</p>
            <div className="flex flex-wrap justify-center gap-2">
              {SUGGESTIONS.map((text) => (
                <button
                  key={text}
                  onClick={() => onAsk(text)}
                  className="rounded-[10px] border bg-secondary px-3.5 py-2 text-[13px] hover:bg-card"
                >
                  {text}
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((message) => {
          if (message.type === "quiz") return <QuizCard key={message.id} chatId={chatId} quiz={message.data} />;
          if (message.type === "challenge") return <ChallengeCard key={message.id} challenge={message.data} />;
          return <Message key={message.id} message={message} pdfs={pdfs} />;
        })}

        {answer !== null && <Message message={{ role: "ASSISTANT", content: answer }} pdfs={pdfs} />}

        {pending.map((action) => (
          <TrelloCard key={action.id} action={action} onFinished={onTrelloFinished} />
        ))}

        {error && <p className="text-sm text-destructive">{error}</p>}
        <div ref={endRef} />
      </div>
    </div>
  );
}
