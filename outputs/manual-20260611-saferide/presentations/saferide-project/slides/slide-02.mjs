import { baseSlide, bulletList, card, pill, C } from "./shared.mjs";

export async function slide02(presentation, ctx) {
  const slide = baseSlide(presentation, ctx, "Introduction", "SafeRide", "Native Android accident alert project");

  ctx.addShape(slide, { left: 54, top: 130, width: 470, height: 430, fill: "#091827", line: ctx.line("#1B4963", 1), borderRadius: 10 });
  ctx.addText(slide, { text: "Smart Accident Detection and Emergency Response System", left: 84, top: 166, width: 410, height: 102, fontSize: 34, bold: true, color: C.text, face: ctx.fonts.title });
  ctx.addText(slide, { text: "SafeRide is a mobile safety application that monitors crash-like motion, confirms emergency events, shares GPS location, and stores incident history for later review.", left: 86, top: 294, width: 398, height: 116, fontSize: 18, color: C.muted });
  pill(ctx, slide, "Android + Java", 88, 452, 136, C.cyan);
  pill(ctx, slide, "Firebase", 242, 452, 120, C.green);
  pill(ctx, slide, "Maps + Sensors", 380, 452, 142, C.amber);

  card(ctx, slide, { left: 590, top: 138, width: 250, height: 146, title: "Core idea", body: "Use phone motion sensors and GPS to reduce emergency response delay after a suspected road accident.", accent: C.green });
  card(ctx, slide, { left: 878, top: 138, width: 250, height: 146, title: "User flow", body: "Login, add contacts, start monitoring, cancel false alerts, or trigger manual SOS.", accent: C.cyan });
  card(ctx, slide, { left: 590, top: 324, width: 250, height: 146, title: "Emergency flow", body: "15-second alert countdown prepares SMS/location sharing before notifying contacts.", accent: C.red });
  card(ctx, slide, { left: 878, top: 324, width: 250, height: 146, title: "Records", body: "Contacts, user profile, and incident history are saved through Firestore-backed services.", accent: C.amber });

  bulletList(ctx, slide, ["Package: com.example.saferide", "Version: 1.1", "Minimum SDK: Android 6.0"], 606, 526, 480, 34, C.green);
  return slide;
}
