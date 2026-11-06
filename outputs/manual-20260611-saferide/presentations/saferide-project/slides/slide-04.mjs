import { baseSlide, card, C } from "./shared.mjs";

export async function slide04(presentation, ctx) {
  const slide = baseSlide(presentation, ctx, "Abstract", "Problem, Approach, Outcome");

  ctx.addShape(slide, { left: 70, top: 142, width: 1080, height: 128, fill: "#0A2032", line: ctx.line("#24516A", 1), borderRadius: 8 });
  ctx.addText(slide, {
    text: "Road accidents need quick communication, accurate location details, and a simple way to alert trusted contacts. SafeRide addresses this by combining Android sensors, location services, Firebase, Google Maps, and SMS intent into one emergency-response workflow.",
    left: 104,
    top: 170,
    width: 1012,
    height: 76,
    fontSize: 22,
    color: C.text,
  });

  card(ctx, slide, { left: 88, top: 326, width: 300, height: 170, title: "Detection", body: "SensorManager reads accelerometer and gyroscope signals to identify sudden impact patterns based on configurable sensitivity.", accent: C.cyan });
  card(ctx, slide, { left: 460, top: 326, width: 300, height: 170, title: "Confirmation", body: "A 15-second countdown gives the user time to cancel false positives before emergency actions are prepared.", accent: C.amber });
  card(ctx, slide, { left: 832, top: 326, width: 300, height: 170, title: "Response", body: "GPS location, emergency contacts, hospital search, notifications, and incident history support faster assistance.", accent: C.green });

  ctx.addText(slide, { text: "Technology used", left: 94, top: 548, width: 180, height: 26, fontSize: 15, bold: true, color: C.cyan });
  ["Firebase Auth", "Cloud Firestore", "Fused Location Provider", "Google Maps", "Local Notifications", "SharedPreferences"].forEach((t, i) => {
    const x = 94 + (i % 3) * 270;
    const y = 584 + Math.floor(i / 3) * 40;
    ctx.addShape(slide, { left: x, top: y, width: 220, height: 28, fill: "#10243A", line: ctx.line("#244B62", 1), borderRadius: 14 });
    ctx.addText(slide, { text: t, left: x + 12, top: y + 6, width: 196, height: 16, fontSize: 12, color: C.muted, align: "center" });
  });
  return slide;
}
