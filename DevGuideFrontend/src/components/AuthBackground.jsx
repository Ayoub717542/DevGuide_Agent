const GLOWS = [
  { color: "rgb(35 134 54 / 75%)", width: "60rem", height: "34rem", left: "-14rem", top: "-14rem", drift: "auth-drift-1 22s" },
  { color: "rgb(108 164 248 / 60%)", width: "56rem", height: "32rem", right: "-12rem", top: "-12rem", drift: "auth-drift-2 26s" },
  { color: "rgb(45 186 160 / 45%)", width: "44rem", height: "24rem", left: "30%", top: "-10rem", drift: "auth-drift-3 30s" },
];

const FADE = "linear-gradient(to bottom, #000 35%, transparent 100%)";

export default function AuthBackground() {
  return (
    <div
      aria-hidden="true"
      className="pointer-events-none fixed inset-x-0 top-0 h-[34rem] overflow-hidden opacity-60 lg:opacity-100"
      style={{ maskImage: FADE, WebkitMaskImage: FADE }}
    >
      {GLOWS.map(({ color, drift, ...position }) => (
        <div
          key={drift}
          className="auth-glow"
          style={{ ...position, background: `radial-gradient(closest-side, ${color}, transparent)`, "--drift": drift }}
        />
      ))}
    </div>
  );
}
