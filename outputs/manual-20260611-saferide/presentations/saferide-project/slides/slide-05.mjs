import { baseSlide, bulletList, card, C } from "./shared.mjs";

export async function slide05(presentation, ctx) {
  const slide = baseSlide(presentation, ctx, "Objectives & Purpose", "What SafeRide Is Designed To Achieve");

  ctx.addText(slide, { text: "Objectives", left: 76, top: 132, width: 420, height: 40, fontSize: 28, bold: true, color: C.text, face: ctx.fonts.title });
  bulletList(ctx, slide, [
    "Detect possible accidents using accelerometer and gyroscope readings.",
    "Send emergency-ready SMS content with current GPS location.",
    "Maintain emergency contacts through Firestore CRUD operations.",
    "Show nearby hospitals through Google Maps search.",
    "Save incident history for user review and demo evidence."
  ], 88, 196, 534, 70, C.green);

  ctx.addShape(slide, { left: 680, top: 142, width: 420, height: 330, fill: "#0A2032", line: ctx.line("#24516A", 1), borderRadius: 10 });
  ctx.addText(slide, { text: "Purpose", left: 720, top: 182, width: 330, height: 40, fontSize: 30, bold: true, color: C.cyan, face: ctx.fonts.title });
  ctx.addText(slide, {
    text: "To reduce emergency response delay by converting a user's smartphone into a simple safety companion that can detect impact, confirm the situation, notify contacts, and guide the user toward nearby medical help.",
    left: 720,
    top: 244,
    width: 340,
    height: 138,
    fontSize: 20,
    color: C.text,
  });
  ctx.addShape(slide, { left: 720, top: 410, width: 320, height: 2, fill: C.green });
  ctx.addText(slide, { text: "Primary users: riders, drivers, students, commuters, and people travelling alone.", left: 720, top: 432, width: 330, height: 44, fontSize: 14, color: C.muted });

  card(ctx, slide, { left: 96, top: 560, width: 280, height: 74, title: "Safety", body: "Faster location sharing during emergencies.", accent: C.red });
  card(ctx, slide, { left: 460, top: 560, width: 280, height: 74, title: "Reliability", body: "Cloud-backed contacts and incident records.", accent: C.cyan });
  card(ctx, slide, { left: 824, top: 560, width: 280, height: 74, title: "Usability", body: "Manual SOS and false-alert cancellation.", accent: C.amber });
  return slide;
}
