export async function slide03(presentation, ctx) {
  const slide = presentation.slides.add();
  ctx.addShape(slide, {
    left: 0,
    top: 0,
    width: ctx.W,
    height: ctx.H,
    fill: "#FFFFFF",
    line: ctx.line("#FFFFFF", 0),
  });
  return slide;
}
