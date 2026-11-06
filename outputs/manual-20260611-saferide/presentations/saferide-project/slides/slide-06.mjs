import { baseSlide, C } from "./shared.mjs";

function row(ctx, slide, y, existing, proposed, accent) {
  ctx.addShape(slide, { left: 74, top: y, width: 514, height: 70, fill: "#0A1B2A", line: ctx.line("#203E54", 1), borderRadius: 6 });
  ctx.addShape(slide, { left: 632, top: y, width: 514, height: 70, fill: "#0A1B2A", line: ctx.line(accent, 1), borderRadius: 6 });
  ctx.addText(slide, { text: existing, left: 100, top: y + 17, width: 455, height: 36, fontSize: 15, color: C.muted });
  ctx.addText(slide, { text: proposed, left: 658, top: y + 17, width: 455, height: 36, fontSize: 15, color: C.text });
}

export async function slide06(presentation, ctx) {
  const slide = baseSlide(presentation, ctx, "Existing System", "Gap Between Manual Response and SafeRide");

  ctx.addText(slide, { text: "Existing / manual safety process", left: 80, top: 136, width: 480, height: 30, fontSize: 22, bold: true, color: C.red });
  ctx.addText(slide, { text: "SafeRide proposed system", left: 638, top: 136, width: 480, height: 30, fontSize: 22, bold: true, color: C.green });

  row(ctx, slide, 190, "Victim or passerby must manually call family, ambulance, or police.", "App can prepare emergency alert flow immediately after impact detection.", C.green);
  row(ctx, slide, 284, "Accident location may be unclear or delayed during panic situations.", "GPS coordinates are captured and shared with emergency contacts.", C.cyan);
  row(ctx, slide, 378, "No automatic accident history or stored evidence is available.", "Incidents are saved in Firestore with time, location, and sensor value.", C.amber);
  row(ctx, slide, 472, "Hospital discovery depends on local knowledge or separate map search.", "Nearby hospital search opens from the current location in Google Maps.", C.green);

  ctx.addShape(slide, { left: 94, top: 594, width: 1010, height: 42, fill: "#10243A", line: ctx.line("#244B62", 1), borderRadius: 8 });
  ctx.addText(slide, { text: "Improvement focus: reduce delay, improve location accuracy, support trusted-contact communication, and keep incident records for review.", left: 124, top: 604, width: 950, height: 22, fontSize: 16, bold: true, color: C.text, align: "center" });
  return slide;
}
