import { C } from "./shared.mjs";

export async function slide08(presentation, ctx) {
  const slide = presentation.slides.add();
  ctx.addShape(slide, { left: 0, top: 0, width: ctx.W, height: ctx.H, fill: C.bg });
  ctx.addShape(slide, { left: 0, top: 0, width: ctx.W, height: 720, fill: "#081826" });
  ctx.addShape(slide, { left: 0, top: 0, width: ctx.W, height: 4, fill: C.cyan });
  ctx.addShape(slide, { left: 0, top: 716, width: ctx.W, height: 4, fill: C.green });
  ctx.addText(slide, {
    text: "Thank You",
    left: 220,
    top: 250,
    width: 840,
    height: 90,
    fontSize: 62,
    bold: true,
    color: C.text,
    align: "center",
    face: ctx.fonts.title,
  });
  ctx.addText(slide, {
    text: "SafeRide | Smart Accident Detection and Emergency Response System",
    left: 260,
    top: 356,
    width: 760,
    height: 34,
    fontSize: 20,
    color: C.muted,
    align: "center",
  });
  ctx.addShape(slide, { left: 478, top: 422, width: 324, height: 2, fill: C.cyan });
  return slide;
}
