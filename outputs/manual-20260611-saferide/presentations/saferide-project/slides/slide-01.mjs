import { C } from "./shared.mjs";

export async function slide01(presentation, ctx) {
  const slide = presentation.slides.add();
  ctx.addShape(slide, { left: 0, top: 0, width: ctx.W, height: ctx.H, fill: C.bg });
  ctx.addShape(slide, { left: 0, top: 0, width: ctx.W, height: 86, fill: "#081826" });
  ctx.addShape(slide, { left: 0, top: 86, width: ctx.W, height: 2, fill: C.cyan });
  return slide;
}
