import { useState } from "react";
import { saveQuizResult } from "../../api/chats";

export default function QuizCard({ chatId, quiz, onScoreSaved }) {
  const [index, setIndex] = useState(0);
  const [picked, setPicked] = useState(null);
  const [score, setScore] = useState(0);
  const [finished, setFinished] = useState(false);

  const total = quiz.questions.length;
  const question = quiz.questions[index];

  const pick = (option) => {
    if (picked !== null) return;
    setPicked(option);
    if (option === question.correctIndex) setScore(score + 1);
  };

  const next = () => {
    if (index === total - 1) {
      setFinished(true);
      saveQuizResult(chatId, quiz.topic, score, total).then(onScoreSaved);
      return;
    }
    setIndex(index + 1);
    setPicked(null);
  };

  const optionStyle = (option) => {
    if (picked === null) return "hover:border-muted-foreground/40";
    if (option === question.correctIndex) return "border-success/60 bg-success/10";
    if (option === picked) return "border-destructive/60 bg-destructive/10";
    return "opacity-55";
  };

  return (
    <div className="overflow-hidden rounded-xl border bg-secondary">
      <header className="flex items-center gap-2.5 border-b px-4.5 py-3.5">
        <b className="font-semibold">Quiz: {quiz.topic}</b>
        <span className="ml-auto text-xs text-muted-foreground">
          {finished ? "Done" : `${index + 1} / ${total}`}
        </span>
      </header>

      {finished ? (
        <p className="px-4.5 py-5">
          You got <b>{score} / {total}</b>. Your score is saved, and your tutor will use it.
        </p>
      ) : (
        <>
          <p className="px-4.5 pt-4.5 pb-1.5 text-base font-medium">{question.question}</p>
          <div className="grid gap-2 px-4.5 py-2.5">
            {question.options.map((text, option) => (
              <button
                key={option}
                onClick={() => pick(option)}
                disabled={picked !== null}
                className={`flex items-center gap-3 rounded-lg border bg-card px-3.5 py-2.5 text-left ${optionStyle(option)}`}
              >
                <span className="grid size-6 shrink-0 place-items-center rounded-md border font-mono text-xs text-muted-foreground">
                  {"ABCD"[option]}
                </span>
                {text}
              </button>
            ))}
          </div>
          {picked !== null && (
            <>
              <p className="mx-4.5 mt-2 rounded-lg bg-card px-3.5 py-3 text-[0.8125rem] text-muted-foreground">
                {question.explanation}
              </p>
              <div className="flex justify-end px-4.5 py-3.5">
                <button onClick={next} className="h-8.5 rounded-lg bg-primary px-3.5 text-[0.8125rem] font-semibold text-primary-foreground">
                  {index === total - 1 ? "See my score" : "Next question"}
                </button>
              </div>
            </>
          )}
        </>
      )}
    </div>
  );
}
