### *元素定位方法 Fallback 机制*
- Selenium方法（Xpath，CssSelector等）
- Anchor（相对于屏幕固定位置或某个特定元素左边的偏移量）
- 图像匹配（AirTest或cv2库）
- CV（需训练模型）


### *Selenium Locator Fallback 机制：*
- 【Tag + 中文文本】，或【Tag + ClassName + 中文文本】（全匹配或contains）
- 【Tag + 英文文本】，或【Tag + ClassName + 英文文本】（避免翻译问题导致元素查找失败）
- 【Tag + ClassName】（一般在特定功能区域内使用ClassName唯一定位，页面全局搜索可能会出现多个匹配的情况）
- 【Tag + 相对结构位】（公司自定义的复杂标准控件，利用其中稳定的父子、兄弟等相对位置关系）
- 【Tag + Index】（一组同类型元素，传入index变量，通过字符串拼接使用index查找目标元素）


### *策略：*
- 除非有ID，否则应尽量避免全页面查找元素。
- 给页面划分功能区域，查找每个区域内的元素时，首先查找该区域的root元素
- 然后通过root.find_element()查找目标元素

