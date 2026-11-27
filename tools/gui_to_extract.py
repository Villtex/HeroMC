#!/usr/bin/env python3
"""Mechanical pass for GUI code: 1.20.1 GuiGraphics (immediate mode) -> 26.3 GuiGraphicsExtractor (retained mode).

Usage: python3 tools/gui_to_extract.py <dir-or-file> [...]

Run after tools/forge_to_neo.py. Rewrites in place and is idempotent (calls that already start with
RenderPipelines are left alone). What it does:
- class/type rename GuiGraphics -> GuiGraphicsExtractor, Screen method renames (render -> extractRenderState, renderBg ->
  extractBackground, renderLabels -> extractLabels, renderBackground -> extractBackground)
- blit(tex, ...) by argument count (1.20.1 overloads) -> blit(RenderPipelines.GUI_TEXTURED, tex, ...):
    7  (tex,x,y,u,v,w,h)                        -> + texture size 256x256
    9  (tex,x,y,u,v,w,h,texW,texH)              -> same order
    10 (tex,x,y,blitOffset,u,v,w,h,texW,texH)   -> blitOffset dropped
    11 (tex,x,y,w,h,u,v,uW,vH,texW,texH)        -> (tex,x,y,u,v,w,h,uW,vH,texW,texH)
  a call with a different count gets a "// TODO gui_to_extract" marker
- drawString -> text, drawCenteredString -> centeredText, renderItem -> item, renderFakeItem -> fakeItem,
  renderItemDecorations -> itemDecorations, renderTooltip -> setTooltipForNextFrame,
  renderComponentTooltip -> setComponentTooltipForNextFrame, pose().pushPose/popPose -> pushMatrix/popMatrix
- 6-digit hex colour literals in text/fill calls get an opaque alpha (26.3 skips colours with alpha 0)
Left for the compiler / by hand: setColor (tint now goes into the blit colour argument), RenderSystem state,
3D pose calls (translate with z, mulPose), custom shaders.
"""
import re
import sys
from pathlib import Path

PIPE = 'RenderPipelines.GUI_TEXTURED'
IMPORT_PIPE = 'import net.minecraft.client.renderer.RenderPipelines;'

SIMPLE = [
    (r'\bnet\.minecraft\.client\.gui\.GuiGraphics\b(?!Extractor)', 'net.minecraft.client.gui.GuiGraphicsExtractor'),
    (r'\bGuiGraphics\b(?!Extractor)', 'GuiGraphicsExtractor'),
    (r'\.drawString\(', '.text('),
    (r'\.drawCenteredString\(', '.centeredText('),
    (r'\.renderFakeItem\(', '.fakeItem('),
    (r'\.renderItemDecorations\(', '.itemDecorations('),
    (r'(\b(?:graphics|guiGraphics|g|gui)\s*)\.renderItem\(', r'\1.item('),
    (r'(\b(?:graphics|guiGraphics|g|gui)\s*)\.renderTooltip\(', r'\1.setTooltipForNextFrame('),
    (r'(\b(?:graphics|guiGraphics|g|gui)\s*)\.renderComponentTooltip\(', r'\1.setComponentTooltipForNextFrame('),
    (r'\.pose\(\)\.pushPose\(\)', '.pose().pushMatrix()'),
    (r'\.pose\(\)\.popPose\(\)', '.pose().popMatrix()'),
    # Screen / widget overrides
    (r'\bvoid render\(GuiGraphicsExtractor\b', 'void extractRenderState(GuiGraphicsExtractor'),
    (r'\bvoid renderWidget\(GuiGraphicsExtractor\b', 'void extractWidgetRenderState(GuiGraphicsExtractor'),
    (r'\bvoid renderLabels\(GuiGraphicsExtractor\b', 'void extractLabels(GuiGraphicsExtractor'),
    (r'\bsuper\.render\((\w+), ', r'super.extractRenderState(\1, '),
    (r'\bsuper\.renderWidget\((\w+), ', r'super.extractWidgetRenderState(\1, '),
    (r'\bthis\.renderBackground\((\w+)\);', r'// background is extracted by Screen itself (26.3)'),
    (r'\brenderBackground\((\w+)\);', r'// background is extracted by Screen itself (26.3)'),
]


def split_args(text, start):
    """text[start] is just after '('. Returns (args, end) with end at the closing ')'."""
    depth = 0
    args = []
    cur = start
    i = start
    in_str = None
    while i < len(text):
        c = text[i]
        if in_str:
            if c == '\\':
                i += 2
                continue
            if c == in_str:
                in_str = None
        elif c in '"\'':
            in_str = c
        elif c in '([{':
            depth += 1
        elif c in ')]}':
            if depth == 0:
                args.append(text[cur:i])
                return args, i
            depth -= 1
        elif c == ',' and depth == 0:
            args.append(text[cur:i])
            cur = i + 1
        i += 1
    raise ValueError('unbalanced call')


def rewrite_blit(text):
    out = []
    pos = 0
    for m in re.finditer(r'\.blit\(', text):
        if m.start() < pos:
            continue
        try:
            args, end = split_args(text, m.end())
        except ValueError:
            continue
        a = [x.strip() for x in args]
        if not a or a[0].startswith('RenderPipelines') or 'RenderPipeline' in a[0] or 'RenderType' in a[0]:
            continue
        n = len(a)
        if n == 7:
            new = [PIPE] + a + ['256', '256']
        elif n == 9:
            new = [PIPE] + a
        elif n == 10:
            new = [PIPE] + a[0:3] + a[4:]
        elif n == 11:
            new = [PIPE, a[0], a[1], a[2], a[5], a[6], a[3], a[4], a[7], a[8], a[9], a[10]]
        else:
            if text[max(0, m.start() - 50):m.start()].endswith('*/'):
                continue  # already marked
            out.append(text[pos:m.start()])
            out.append('/* TODO gui_to_extract: blit with %d args */' % n)
            pos = m.start()
            continue
        out.append(text[pos:m.start()])
        out.append('.blit(' + ', '.join(new) + ')')
        pos = end + 1
    out.append(text[pos:])
    return ''.join(out)


def opaque_colours(text):
    def fix(m):
        return m.group(1) + '0xFF' + m.group(2)
    # only inside text/centeredText/fill/fillGradient calls
    def repl_call(m):
        return re.sub(r'(?<![\w])(0x)([0-9A-Fa-f]{6})\b', lambda h: '0xFF' + h.group(2).upper(), m.group(0))
    return re.sub(r'\.(?:text|centeredText|fill|fillGradient)\([^;]*;', repl_call, text)


def pose_2d(text):
    """PoseStack variables taken from graphics.pose() become Matrix3x2fStack (2D): push/pop and drop the z argument."""
    names = set(re.findall(r'\bPoseStack (\w+) = \w+\.pose\(\);', text))
    text = re.sub(r'\bPoseStack (\w+) = (\w+)\.pose\(\);', r'Matrix3x2fStack \1 = \2.pose();', text)
    names |= {'pose()'}
    for n in names:
        q = re.escape(n)
        text = re.sub(r'\b' + q + r'\.pushPose\(\)', n + '.pushMatrix()', text)
        text = re.sub(r'\b' + q + r'\.popPose\(\)', n + '.popMatrix()', text)
        for call in ('translate', 'scale'):
            out, pos = [], 0
            for m in re.finditer(r'\b' + q + r'\.' + call + r'\(', text):
                if m.start() < pos:
                    continue
                try:
                    args, end = split_args(text, m.end())
                except ValueError:
                    continue
                if len(args) != 3:
                    continue
                out.append(text[pos:m.start()])
                out.append(n + '.' + call + '(' + ', '.join(a.strip() for a in args[:2]) + ')')
                pos = end + 1
            out.append(text[pos:])
            text = ''.join(out)
    if 'Matrix3x2fStack' in text and 'import org.joml.Matrix3x2fStack;' not in text:
        text = re.sub(r'(\npackage [^;]+;\n)', r'\1\nimport org.joml.Matrix3x2fStack;\n', text, count=1)
    return text


def add_import(text):
    if PIPE in text and IMPORT_PIPE not in text:
        text = re.sub(r'(\npackage [^;]+;\n)', r'\1\n' + IMPORT_PIPE + '\n', text, count=1)
    return text


# event handler declarations: old parameter lists -> 26.3 event records; the old names stay usable as locals
D = r'(?:double|float)'
EVENTS = [
    (r'boolean mouseClicked\(double (\w+), double (\w+), int (\w+)\)\s*\{',
     r'boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {\n    double \1 = event.x(), \2 = event.y(); int \3 = event.button();'),
    (r'boolean mouseReleased\(double (\w+), double (\w+), int (\w+)\)\s*\{',
     r'boolean mouseReleased(MouseButtonEvent event) {\n    double \1 = event.x(), \2 = event.y(); int \3 = event.button();'),
    (r'boolean mouseDragged\(double (\w+), double (\w+), int (\w+), double (\w+), double (\w+)\)\s*\{',
     r'boolean mouseDragged(MouseButtonEvent event, double \4, double \5) {\n    double \1 = event.x(), \2 = event.y(); int \3 = event.button();'),
    (r'boolean mouseScrolled\(double (\w+), double (\w+), double (\w+)\)\s*\{',
     r'boolean mouseScrolled(double \1, double \2, double scrollX, double \3) {'),
    (r'boolean keyPressed\(int (\w+), int (\w+), int (\w+)\)\s*\{',
     r'boolean keyPressed(KeyEvent event) {\n    int \1 = event.key(), \2 = event.keycode(), \3 = event.modifiers();'),
    (r'boolean keyReleased\(int (\w+), int (\w+), int (\w+)\)\s*\{',
     r'boolean keyReleased(KeyEvent event) {\n    int \1 = event.key(), \2 = event.keycode(), \3 = event.modifiers();'),
    (r'boolean charTyped\(char (\w+), int (\w+)\)\s*\{',
     r'boolean charTyped(CharacterEvent event) {\n    char \1 = (char) event.codepoint(); int \2 = 0;'),
    # super calls inside the handlers
    (r'super\.mouseClicked\([^()]*\)', 'super.mouseClicked(event, doubleClick)'),
    (r'super\.mouseReleased\([^()]*\)', 'super.mouseReleased(event)'),
    (r'super\.mouseDragged\((\w+), (\w+), (\w+), ([^,()]+), ([^,()]+)\)', r'super.mouseDragged(event, \4, \5)'),
    (r'super\.mouseScrolled\((\w+), (\w+), ([^,()]+)\)', r'super.mouseScrolled(\1, \2, scrollX, \3)'),
    (r'super\.keyPressed\([^()]*\)', 'super.keyPressed(event)'),
    (r'super\.keyReleased\([^()]*\)', 'super.keyReleased(event)'),
    (r'super\.charTyped\([^()]*\)', 'super.charTyped(event)'),
    # container background: renderBg(g, partialTicks, mouseX, mouseY) -> extractBackground(g, mouseX, mouseY, partialTicks)
    # Screen#extractBackground draws the dimmed/blurred world (old renderBackground call), so it is called first
    (r'(?:protected|public) void renderBg\(GuiGraphicsExtractor (\w+), float (\w+), int (\w+), int (\w+)\)\s*\{',
     r'public void extractBackground(GuiGraphicsExtractor \1, int \3, int \4, float \2) {\n    super.extractBackground(\1, \3, \4, \2);'),
    (r'\brenderLabels\((\w+), ([^,()]+), ([^,()]+)\)', r'extractLabels(\1, \2, \3)'),
    # AbstractContainerScreen#extractRenderState extracts the tooltip itself; other 3-arg calls -> extractTooltip
    (r'\n\s*(?:this\.)?renderTooltip\((\w+), (\w+), (\w+)\);(?=\s*\n\s*\})', ''),
    (r'\b(this\.)?renderTooltip\((\w+), (\w+), (\w+)\)', r'\1extractTooltip(\2, \3, \4)'),
    (r'\brenderBg\((\w+), ([^,()]+), ([^,()]+), ([^,()]+)\)', r'extractBackground(\1, \3, \4, \2)'),
]
IMPORTS = {'MouseButtonEvent': 'net.minecraft.client.input.MouseButtonEvent', 'KeyEvent': 'net.minecraft.client.input.KeyEvent',
           'CharacterEvent': 'net.minecraft.client.input.CharacterEvent'}


def convert(text):
    for pat, rep in SIMPLE:
        text = re.sub(pat, rep, text)
    for pat, rep in EVENTS:
        text = re.sub(pat, rep, text)
    for name, full in IMPORTS.items():
        if re.search(r'\b' + name + r' event\b', text) and ('import ' + full + ';') not in text:
            text = re.sub(r'(\npackage [^;]+;\n)', r'\1\nimport ' + full + ';\n', text, count=1)
    text = rewrite_blit(text)
    text = opaque_colours(text)
    text = pose_2d(text)
    return add_import(text)


def main():
    changed = total = 0
    for arg in sys.argv[1:]:
        p = Path(arg)
        files = [p] if p.is_file() else sorted(p.rglob('*.java'))
        for f in files:
            total += 1
            old = f.read_text(encoding='utf-8')
            if not old.startswith('package') and '\npackage ' not in old:
                continue
            new = convert(old if old.startswith('\n') else '\n' + old)[1:] if not old.startswith('\n') else convert(old)
            if new != old:
                f.write_text(new, encoding='utf-8')
                changed += 1
    print(f'{changed} of {total} files changed')


if __name__ == '__main__':
    main()
