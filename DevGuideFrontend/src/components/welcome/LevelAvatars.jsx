const FIGURE = "translate(60 120) scale(1.1) translate(-60 -120)";

function Frame({ id, fill, ring, children, behind }) {
  return (
    <svg viewBox="0 0 120 120" aria-hidden="true" className="size-full">
      <clipPath id={id}><circle cx="60" cy="60" r="58" /></clipPath>
      <circle cx="60" cy="60" r="58" fill={fill} />
      <circle cx="60" cy="60" r="57.5" fill="none" stroke={ring} />
      {behind}
      <g clipPath={`url(#${id})`} transform={FIGURE}>{children}</g>
    </svg>
  );
}

export function BeginnerAvatar() {
  return (
    <Frame id="avatar-beginner" fill="#1A2620" ring="#2F4A3A">
      <path d="M22 120c0-24 17-38 38-38s38 14 38 38z" fill="#3B5C49" />
      <path d="M22 120c0-20 12-33 28-37-8 8-12 20-12 37z" fill="#33503F" />
      <path d="M50 82l10 12 10-12" fill="none" stroke="#2C4537" strokeWidth="3" />
      <rect x="53" y="66" width="14" height="16" rx="5" fill="#B98F74" />
      <path d="M53 72c4 3 10 3 14 0v-2H53z" fill="#A47C63" />
      <ellipse cx="42.5" cy="52" rx="3.2" ry="4.4" fill="#BE957B" />
      <ellipse cx="77.5" cy="52" rx="3.2" ry="4.4" fill="#BE957B" />
      <circle cx="60" cy="50" r="18" fill="#C9A188" />
      <path d="M42 48c0-13 8-20 18-20s19 6 19 18c-6-5-14-7-22-6-6 1-11 4-15 8z" fill="#3A2B22" />
      <path d="M52 33c5-2 12-2 17 1" fill="none" stroke="#4E3A2E" strokeWidth="1.4" strokeLinecap="round" />
      <path d="M51 48.5h5M64 48.5h5" stroke="#3A2B22" strokeWidth="1.3" strokeLinecap="round" />
      <circle cx="53.5" cy="52.5" r="1.6" fill="#2A1F19" />
      <circle cx="66.5" cy="52.5" r="1.6" fill="#2A1F19" />
      <path d="M60 54.5v2.5l-1.2.8" fill="none" stroke="#A9806A" strokeWidth="1.2" strokeLinecap="round" />
      <path d="M55 60c3 2.5 7 2.5 10 0" fill="none" stroke="#7A4E3C" strokeWidth="1.6" strokeLinecap="round" />
      <path d="M36 104l24-6 24 6v14l-24-6-24 6z" fill="#E6E1D6" />
      <path d="M36 118l24-6 24 6v2H36z" fill="#2E4A3A" />
      <path d="M60 98v14" stroke="#B9B2A3" strokeWidth="1.2" />
      <path d="M42 104.5l14-3.5M42 108.5l14-3.5M64 101l14 3.5M64 105l14 3.5" stroke="#B9B2A3" strokeWidth="1" />
    </Frame>
  );
}

export function IntermediateAvatar() {
  return (
    <Frame id="avatar-intermediate" fill="#18202C" ring="#2C3D55">
      <path d="M20 120c0-25 18-40 40-40s40 15 40 40z" fill="#34465F" />
      <path d="M20 120c0-21 13-35 30-39-8 8-13 21-13 39z" fill="#2E3F56" />
      <path d="M40 84c4 10 12 14 20 14s16-4 20-14c-6-3-13-4-20-4s-14 1-20 4z" fill="#2A3A50" />
      <path d="M56 98v10M64 98v10" stroke="#8796AB" strokeWidth="1.5" strokeLinecap="round" />
      <rect x="53" y="66" width="14" height="16" rx="5" fill="#9C7258" />
      <path d="M53 72c4 3 10 3 14 0v-2H53z" fill="#8A6450" />
      <ellipse cx="42.5" cy="53" rx="3.2" ry="4.4" fill="#A27860" />
      <ellipse cx="77.5" cy="53" rx="3.2" ry="4.4" fill="#A27860" />
      <circle cx="60" cy="50" r="18" fill="#A97E63" />
      <path d="M41 50c-1-14 8-23 19-23 12 0 20 8 19 22-2-6-6-10-10-11-4 4-12 6-21 5-3 2-5 4-7 7z" fill="#1E1A18" />
      <path d="M50 31c4-2 10-2 15 0" fill="none" stroke="#332C28" strokeWidth="1.4" strokeLinecap="round" />
      <path d="M51 49.5h5M64 49.5h5" stroke="#1E1A18" strokeWidth="1.4" strokeLinecap="round" />
      <circle cx="53.5" cy="53.5" r="1.6" fill="#1E1714" />
      <circle cx="66.5" cy="53.5" r="1.6" fill="#1E1714" />
      <path d="M60 55.5v2.5l-1.2.8" fill="none" stroke="#8A6450" strokeWidth="1.2" strokeLinecap="round" />
      <path d="M56 61.5c2.5 1 5.5 1 8 0" fill="none" stroke="#5E3E30" strokeWidth="1.6" strokeLinecap="round" />
      <path d="M34 104h52l6 16H28z" fill="#5A6371" />
      <path d="M38 104l4-14h36l4 14z" fill="#2B313B" />
      <rect x="44" y="93" width="32" height="9" rx="1" fill="#6C8DB8" opacity="0.35" />
      <circle cx="60" cy="97.5" r="1.4" fill="#8FA5C4" opacity="0.6" />
    </Frame>
  );
}

export function AdvancedAvatar() {
  const terminal = (
    <>
      <rect x="12" y="20" width="42" height="32" rx="3" fill="#0F1217" stroke="#3A3F48" clipPath="url(#avatar-advanced)" />
      <path d="M18 29l4 3-4 3M25 35h8M18 41h16M18 46h10" stroke="#B08A45" strokeWidth="1.6" strokeLinecap="round" fill="none" />
    </>
  );

  return (
    <Frame id="avatar-advanced" fill="#241F17" ring="#4A3D25" behind={terminal}>
      <path d="M22 120c0-24 17-38 38-38s38 14 38 38z" fill="#2B2D33" />
      <path d="M22 120c0-20 12-33 28-37-8 8-12 20-12 37z" fill="#25272C" />
      <path d="M52 82c2 4 5 6 8 6s6-2 8-6" fill="none" stroke="#1D1F24" strokeWidth="3" />
      <rect x="53" y="66" width="14" height="16" rx="5" fill="#C29A80" />
      <path d="M53 72c4 3 10 3 14 0v-2H53z" fill="#AE8770" />
      <circle cx="60" cy="50" r="18" fill="#D0AA90" />
      <path d="M42 46c1-12 8-18 18-18s17 6 18 18c-5-4-11-6-18-6s-13 2-18 6z" fill="#5A5550" />
      <path d="M48 37c4-3 12-4 18-1" fill="none" stroke="#6E6964" strokeWidth="1.3" strokeLinecap="round" />
      <path d="M40 52c0-16 9-26 20-26s20 10 20 26" fill="none" stroke="#3B3E45" strokeWidth="3.5" />
      <rect x="36" y="46" width="8" height="14" rx="3.5" fill="#4A4E57" />
      <rect x="76" y="46" width="8" height="14" rx="3.5" fill="#4A4E57" />
      <path d="M38 52h4M78 52h4" stroke="#5E636D" strokeWidth="1" />
      <rect x="48" y="48" width="10" height="7" rx="2" fill="#E6EDF3" fillOpacity="0.08" stroke="#2A2622" strokeWidth="1.4" />
      <rect x="62" y="48" width="10" height="7" rx="2" fill="#E6EDF3" fillOpacity="0.08" stroke="#2A2622" strokeWidth="1.4" />
      <path d="M58 51h4" stroke="#2A2622" strokeWidth="1.4" />
      <circle cx="53" cy="51.5" r="1.4" fill="#2A1F19" />
      <circle cx="67" cy="51.5" r="1.4" fill="#2A1F19" />
      <path d="M60 56v2.5l-1.2.8" fill="none" stroke="#B08A72" strokeWidth="1.2" strokeLinecap="round" />
      <path d="M55 62c3 1.5 7 1.5 10 0" fill="none" stroke="#7F5544" strokeWidth="1.6" strokeLinecap="round" />
    </Frame>
  );
}
