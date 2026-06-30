/**
 * 与试题表 exam_question.formula_type 一致。
 * 各学科只展示本学科常用项：数学用「完整数学栏」；化/生/地不再附带 ∑∫lim 等数学课专属按钮。
 */

const inlineMath = {
  label: "行内公式",
  tip: "在一行里写公式：用 $ 把公式包起来，例如 $x^2$。点击插入一对 $ $，光标在中间。",
  insert: "$$",
  caretInInsert: 1,
};

const blockMath = {
  label: "整段公式",
  tip: "单独一行的大公式：用 $$ 包住。点击插入 $$ 空行 $$，光标在中间。",
  insert: "$$\n\n$$",
  caretInInsert: 3,
};

const fracBtn = {
  label: "分式",
  tip: "插入分数结构 \\frac{分子}{分母}，再在大括号里填写。",
  insert: "\\frac{}{}",
  caretInInsert: 6,
};

const sqrtBtn = {
  label: "根号 √",
  tip: "插入平方根 \\sqrt{}，在大括号里写被开方数。",
  insert: "\\sqrt{}",
  caretInInsert: 6,
};

const supBtn = {
  label: "上标",
  tip: "插入 ^{ }，在大括号里写上标内容，如 x^{2}。",
  insert: "^{}",
  caretInInsert: 2,
};

const subBtn = {
  label: "下标",
  tip: "插入 _{ }，在大括号里写下标内容，如 H_{2}O 里的 2。",
  insert: "_{}",
  caretInInsert: 2,
};

/** 数学科：含微积分、希腊字母等 */
const MATH = [
  inlineMath,
  blockMath,
  fracBtn,
  sqrtBtn,
  supBtn,
  subBtn,
  {
    label: "求和 Σ",
    tip: "插入求和号及上下标占位：\\sum_{}^{}",
    insert: "\\sum_{}^{}",
    caretInInsert: 6,
  },
  {
    label: "积分 ∫",
    tip: "插入积分号及上下限占位：\\int_{}^{}",
    insert: "\\int_{}^{}",
    caretInInsert: 6,
  },
  {
    label: "极限",
    tip: "插入极限及下标占位：\\lim_{}",
    insert: "\\lim_{}",
    caretInInsert: 6,
  },
  { label: "π", tip: "插入圆周率 \\pi", insert: "\\pi", caretInInsert: null },
  { label: "α", tip: "插入希腊字母 alpha：\\alpha", insert: "\\alpha", caretInInsert: null },
  { label: "β", tip: "插入希腊字母 beta：\\beta", insert: "\\beta", caretInInsert: null },
  { label: "≤", tip: "插入小于等于 \\leq", insert: "\\leq", caretInInsert: null },
  { label: "≥", tip: "插入大于等于 \\geq", insert: "\\geq", caretInInsert: null },
  { label: "≠", tip: "插入不等于 \\neq", insert: "\\neq", caretInInsert: null },
  { label: "∞", tip: "插入无穷大 \\infty", insert: "\\infty", caretInInsert: null },
];

/** 物理：公式线 + 微积分（物理常用）+ 物理量写法，不含化学箭头 */
const PHYSICS = [
  inlineMath,
  blockMath,
  fracBtn,
  sqrtBtn,
  supBtn,
  subBtn,
  {
    label: "积分 ∫",
    tip: "插入积分号及上下限占位：\\int_{}^{}",
    insert: "\\int_{}^{}",
    caretInInsert: 6,
  },
  {
    label: "极限",
    tip: "插入极限及下标占位：\\lim_{}",
    insert: "\\lim_{}",
    caretInInsert: 6,
  },
  { label: "π", tip: "插入圆周率 \\pi", insert: "\\pi", caretInInsert: null },
  { label: "α", tip: "插入希腊字母 alpha：\\alpha", insert: "\\alpha", caretInInsert: null },
  { label: "β", tip: "插入希腊字母 beta：\\beta", insert: "\\beta", caretInInsert: null },
  { label: "≤", tip: "插入小于等于 \\leq", insert: "\\leq", caretInInsert: null },
  { label: "≥", tip: "插入大于等于 \\geq", insert: "\\geq", caretInInsert: null },
  { label: "≠", tip: "插入不等于 \\neq", insert: "\\neq", caretInInsert: null },
  { label: "∞", tip: "插入无穷大 \\infty", insert: "\\infty", caretInInsert: null },
  {
    label: "矢量",
    tip: "在字母上加箭头矢量符号，如 \\vec{F}。点击插入 \\vec{}。",
    insert: "\\vec{}",
    caretInInsert: 5,
  },
  { label: "·", tip: "点乘 \\cdot", insert: "\\cdot", caretInInsert: null },
  { label: "×", tip: "叉乘 \\times", insert: "\\times", caretInInsert: null },
  { label: "°", tip: "度数上标，如 30^{\\circ}。点击插入 ^{\\circ}", insert: "^{\\circ}", caretInInsert: null },
  { label: "Δ", tip: "增量/三角符号 \\Delta（后接字母或空格）", insert: "\\Delta ", caretInInsert: null },
  {
    label: "正体",
    tip: "物理量正体字母 \\mathrm{}，在大括号里写字母。",
    insert: "\\mathrm{}",
    caretInInsert: 9,
  },
];

/** 化学：式子排版 + 反应符号，无 ∑∫lim 等 */
const CHEMISTRY = [
  inlineMath,
  blockMath,
  fracBtn,
  sqrtBtn,
  supBtn,
  subBtn,
  { label: "·", tip: "点乘（如结晶水）\\cdot", insert: "\\cdot", caretInInsert: null },
  { label: "×", tip: "叉乘 \\times", insert: "\\times", caretInInsert: null },
  { label: "≤", tip: "小于等于 \\leq", insert: "\\leq", caretInInsert: null },
  { label: "≥", tip: "大于等于 \\geq", insert: "\\geq", caretInInsert: null },
  { label: "→", tip: "反应方向箭头 \\rightarrow", insert: "\\rightarrow", caretInInsert: null },
  { label: "可逆", tip: "左右双箭头 \\leftrightarrow（可逆反应常用）", insert: "\\leftrightarrow", caretInInsert: null },
  { label: "↑", tip: "气体符号 \\uparrow", insert: "\\uparrow", caretInInsert: null },
  { label: "↓", tip: "沉淀符号 \\downarrow", insert: "\\downarrow", caretInInsert: null },
  { label: "Δ", tip: "加热或反应条件 \\Delta", insert: "\\Delta ", caretInInsert: null },
  { label: "⁺", tip: "离子正电荷上标 ^{+}", insert: "^{+}", caretInInsert: null },
  { label: "⁻", tip: "离子负电荷上标 ^{-}", insert: "^{-}", caretInInsert: null },
  { label: "价态2+", tip: "多价离子片段 ^{2+}（可按需改成 3+ 等）", insert: "^{2+}", caretInInsert: null },
];

/** 生物：比例、杂交、百分数等 */
const BIOLOGY = [
  inlineMath,
  blockMath,
  fracBtn,
  supBtn,
  subBtn,
  { label: "×", tip: "杂交或乘号 \\times", insert: "\\times", caretInInsert: null },
  { label: "≈", tip: "约等于 \\approx", insert: "\\approx", caretInInsert: null },
  { label: "%", tip: "百分号 \\%", insert: "\\%", caretInInsert: null },
];

/** 地理：经纬、方位、约等 */
const GEOGRAPHY = [
  inlineMath,
  blockMath,
  fracBtn,
  supBtn,
  subBtn,
  { label: "°", tip: "度数 ^{\\circ}", insert: "^{\\circ}", caretInInsert: null },
  { label: "′", tip: "角分 ^{\\prime}", insert: "^{\\prime}", caretInInsert: null },
  { label: "北 N", tip: "北纬等文字 \\text{N}", insert: "\\text{N}", caretInInsert: null },
  { label: "南 S", tip: "南纬等文字 \\text{S}", insert: "\\text{S}", caretInInsert: null },
  { label: "≈", tip: "约等于 \\approx", insert: "\\approx", caretInInsert: null },
];

export function getFormulaToolbarButtons(formulaType) {
  const t = String(formulaType || "none")
    .trim()
    .toLowerCase();
  switch (t) {
    case "math":
      return MATH;
    case "physics":
      return PHYSICS;
    case "chemistry":
      return CHEMISTRY;
    case "biology":
      return BIOLOGY;
    case "geography":
      return GEOGRAPHY;
    case "none":
    case "":
    default:
      return [];
  }
}
