const C = {
  bg: "#07111F",
  panel: "#0D1B2A",
  panel2: "#10243A",
  cyan: "#34D5E5",
  green: "#65D48A",
  red: "#FF5A66",
  amber: "#F4C95D",
  text: "#F7FBFF",
  muted: "#AFC3D6",
  line: "#29516B",
};

export function baseSlide(presentation, ctx, kicker, title, subtitle = "") {
  const slide = presentation.slides.add();
  ctx.addShape(slide, { left: 0, top: 0, width: ctx.W, height: ctx.H, fill: C.bg });
  ctx.addShape(slide, { left: 0, top: 0, width: ctx.W, height: 86, fill: "#081826" });
  ctx.addShape(slide, { left: 0, top: 86, width: ctx.W, height: 2, fill: C.cyan });
  ctx.addText(slide, {
    text: kicker.toUpperCase(),
    left: 58,
    top: 26,
    width: 280,
    height: 22,
    fontSize: 13,
    bold: true,
    color: C.cyan,
    face: ctx.fonts.body,
  });
  ctx.addText(slide, {
    text: title,
    left: 56,
    top: 42,
    width: subtitle ? 820 : 940,
    height: 48,
    fontSize: 31,
    bold: true,
    color: C.text,
    face: ctx.fonts.title,
  });
  if (subtitle) {
    ctx.addText(slide, {
      text: subtitle,
      left: 830,
      top: 42,
      width: 360,
      height: 34,
      fontSize: 15,
      color: C.muted,
      align: "right",
    });
  }
  ctx.addText(slide, {
    text: "SafeRide | Smart Accident Detection and Emergency Response System",
    left: 56,
    top: 674,
    width: 720,
    height: 22,
    fontSize: 11,
    color: "#6F8BA3",
  });
  ctx.addText(slide, {
    text: String(ctx.slideNumber).padStart(2, "0"),
    left: 1160,
    top: 674,
    width: 64,
    height: 22,
    fontSize: 11,
    color: "#6F8BA3",
    align: "right",
  });
  return slide;
}

export function card(ctx, slide, { left, top, width, height, title, body, accent = C.cyan }) {
  ctx.addShape(slide, { left, top, width, height, fill: C.panel, line: ctx.line("#1D4159", 1), borderRadius: 8 });
  ctx.addShape(slide, { left, top, width: 5, height, fill: accent });
  ctx.addText(slide, { text: title, left: left + 22, top: top + 18, width: width - 44, height: 28, fontSize: 18, bold: true, color: C.text });
  ctx.addText(slide, { text: body, left: left + 22, top: top + 54, width: width - 44, height: height - 70, fontSize: 14, color: C.muted });
}

export function bulletList(ctx, slide, items, left, top, width, gap = 48, color = C.cyan) {
  items.forEach((item, index) => {
    const y = top + index * gap;
    ctx.addShape(slide, { left, top: y + 6, width: 11, height: 11, fill: color, geometry: "ellipse" });
    ctx.addText(slide, { text: item, left: left + 28, top: y, width, height: 40, fontSize: 17, color: C.text });
  });
}

export function pill(ctx, slide, text, left, top, width, accent = C.cyan) {
  ctx.addShape(slide, { left, top, width, height: 42, fill: C.panel2, line: ctx.line(accent, 1), borderRadius: 20 });
  ctx.addText(slide, { text, left: left + 14, top: top + 10, width: width - 28, height: 22, fontSize: 13, bold: true, color: C.text, align: "center" });
}

export function dfdNode(ctx, slide, text, left, top, width, height, accent = C.cyan) {
  ctx.addShape(slide, { left, top, width, height, fill: C.panel2, line: ctx.line(accent, 2), borderRadius: 8 });
  ctx.addText(slide, { text, left: left + 14, top: top + 15, width: width - 28, height: height - 24, fontSize: 14, bold: true, color: C.text, align: "center", valign: "mid" });
}

export function arrow(ctx, slide, x1, y1, x2, y2, label = "") {
  const horizontal = Math.abs(x2 - x1) >= Math.abs(y2 - y1);
  if (horizontal) {
    const left = Math.min(x1, x2);
    const width = Math.abs(x2 - x1);
    ctx.addShape(slide, { left, top: y1 - 2, width, height: 4, fill: C.line });
    ctx.addText(slide, { text: x2 >= x1 ? ">" : "<", left: x2 - 8, top: y2 - 13, width: 18, height: 22, fontSize: 19, bold: true, color: C.cyan, align: "center" });
    if (label) ctx.addText(slide, { text: label, left: left + 8, top: y1 - 27, width: Math.max(90, width - 16), height: 18, fontSize: 10, color: C.muted, align: "center" });
  } else {
    const top = Math.min(y1, y2);
    const height = Math.abs(y2 - y1);
    ctx.addShape(slide, { left: x1 - 2, top, width: 4, height, fill: C.line });
    ctx.addText(slide, { text: y2 >= y1 ? "v" : "^", left: x2 - 9, top: y2 - 10, width: 18, height: 20, fontSize: 18, bold: true, color: C.cyan, align: "center" });
    if (label) ctx.addText(slide, { text: label, left: x1 + 10, top: top + height / 2 - 10, width: 132, height: 20, fontSize: 10, color: C.muted });
  }
}

export { C };
