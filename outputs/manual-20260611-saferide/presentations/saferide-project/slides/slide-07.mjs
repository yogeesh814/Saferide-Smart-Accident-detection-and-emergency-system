import { baseSlide, dfdNode, arrow, C } from "./shared.mjs";

export async function slide07(presentation, ctx) {
  const slide = baseSlide(presentation, ctx, "DFD", "Level 0 Data Flow Diagram");

  dfdNode(ctx, slide, "User / Rider", 70, 250, 150, 76, C.green);
  dfdNode(ctx, slide, "SafeRide Android App\nAuth, Monitor, SOS, Maps", 330, 220, 240, 136, C.cyan);
  dfdNode(ctx, slide, "Phone Sensors\nAccelerometer + Gyroscope", 330, 456, 240, 76, C.amber);
  dfdNode(ctx, slide, "GPS Location\nFused Location Provider", 674, 456, 230, 76, C.green);
  dfdNode(ctx, slide, "Firebase Auth\nUser Login / Register", 700, 126, 210, 78, C.cyan);
  dfdNode(ctx, slide, "Cloud Firestore\nUsers, Contacts, Incidents", 700, 248, 210, 88, C.amber);
  dfdNode(ctx, slide, "Emergency Contacts\nSMS App", 1010, 218, 160, 78, C.red);
  dfdNode(ctx, slide, "Google Maps\nNearby Hospitals", 1010, 392, 160, 78, C.green);

  arrow(ctx, slide, 220, 288, 330, 288, "login / SOS / settings");
  arrow(ctx, slide, 450, 456, 450, 356, "sensor values");
  arrow(ctx, slide, 570, 288, 700, 166, "credentials");
  arrow(ctx, slide, 570, 288, 700, 292, "contacts + incidents");
  arrow(ctx, slide, 674, 494, 570, 318, "GPS data");
  arrow(ctx, slide, 910, 292, 1010, 256, "alert message");
  arrow(ctx, slide, 904, 494, 1010, 430, "hospital query");
  arrow(ctx, slide, 1010, 450, 904, 512, "map results");
  arrow(ctx, slide, 1010, 256, 570, 318, "confirmation status");

  ctx.addShape(slide, { left: 80, top: 604, width: 1040, height: 34, fill: "#0A2032", line: ctx.line("#244B62", 1), borderRadius: 6 });
  ctx.addText(slide, {
    text: "Main process: authenticated user starts monitoring; sensor and GPS data feed the SafeRide app; alerts use contact data and store incident details in Firestore.",
    left: 110,
    top: 612,
    width: 980,
    height: 18,
    fontSize: 13,
    color: C.muted,
    align: "center",
  });
  return slide;
}
