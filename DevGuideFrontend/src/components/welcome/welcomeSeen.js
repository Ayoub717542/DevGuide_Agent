const keyFor = (email) => `welcome-done-${email}`;

export const markWelcomeSeen = (email) => localStorage.setItem(keyFor(email), "1");

export const hasSeenWelcome = (email) => localStorage.getItem(keyFor(email)) === "1";
