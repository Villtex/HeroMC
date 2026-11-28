#!/usr/bin/env python3
"""Error-driven second pass for Forge 1.20.1 -> NeoForge 26.3 ports (Teil 27): reads a javac log and rewrites the
reported spots for changes that a plain regex (tools/forge_to_neo.py) cannot place safely.

Usage: python3 tools/fix_by_errors.py <javac-log> [--dry]

Handled (each only at the line/column javac names):
- NBT getters that return Optional in 26.3: tag.getInt(k) -> tag.getIntOr(k, 0), getString -> getStringOr(k, ""),
  getCompound -> getCompoundOrEmpty, getList(k, type) -> getListOrEmpty(k), getBoolean -> getBooleanOr(k, false) ...
- ResourceKey.location() -> identifier()
- Registry lookups: registry.get(id) returning Optional<Reference> where a value is wanted -> getValue(id);
  getHolder(..) -> get(..); getHolderOrThrow -> getOrThrow; getOptional stays
Run the compiler again afterwards; repeat until it changes nothing.
"""
import re
import sys
from collections import defaultdict
from pathlib import Path

ERR = re.compile(r'^(/.*?\.java):(\d+): error: (.*)$')

NBT_DEFAULT = {
    'getInt': '0', 'getLong': '0L', 'getShort': '(short) 0', 'getByte': '(byte) 0', 'getFloat': '0f',
    'getDouble': '0d', 'getString': '""', 'getBoolean': 'false',
}


def match_paren(s, i):
    """index of the ')' closing the '(' at s[i]"""
    depth = 0
    in_str = False
    for j in range(i, len(s)):
        c = s[j]
        if in_str:
            if c == '\\':
                continue
            if c == '"' and s[j - 1] != '\\':
                in_str = False
            continue
        if c == '"':
            in_str = True
        elif c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return j
    return -1


def fix_optional(line, col, msg):
    m = re.match(r'incompatible types: Optional<(?:[A-Za-z.]+\.)?(\w+)[^>]*>+ cannot be converted to (.*)', msg)
    if not m:
        return None
    # find the first NBT getter call at or after the column (javac points at the start of the expression)
    for call in re.finditer(r'\.(get(?:Int|Long|Short|Byte|Float|Double|String|Boolean|Compound|List|ByteArray|IntArray|LongArray))\(', line):
        if call.start() < col - 1 and line.find(')', call.end()) < col - 1:
            continue
        name = call.group(1)
        open_i = call.end() - 1
        close_i = match_paren(line, open_i)
        if close_i < 0:
            return None
        args = line[open_i + 1:close_i]
        if name == 'getCompound':
            return line[:call.start()] + '.getCompoundOrEmpty(' + args + ')' + line[close_i + 1:]
        if name == 'getList':
            first = args.split(',')[0]
            return line[:call.start()] + '.getListOrEmpty(' + first + ')' + line[close_i + 1:]
        if name in ('getByteArray', 'getIntArray', 'getLongArray'):
            kind = {'getByteArray': 'byte', 'getIntArray': 'int', 'getLongArray': 'long'}[name]
            return line[:close_i + 1] + f'.orElseGet(() -> new {kind}[0])' + line[close_i + 1:]
        if name in NBT_DEFAULT:
            return line[:call.start()] + f'.{name}Or(' + args + ', ' + NBT_DEFAULT[name] + ')' + line[close_i + 1:]
        if m.group(1) == 'Reference' or 'Reference' in msg:
            break
    # registry.get(id) -> Optional<Reference<T>> where T is wanted
    if 'Reference<' in msg:
        for call in re.finditer(r'\.get\(', line):
            if call.start() >= col - 2 or line.find(')', call.end()) >= col - 1:
                return line[:call.start()] + '.getValue(' + line[call.end():]
    return None


EXPR = re.compile(r'(?:new \w+\([^()]*\)|[\w.]+(?:\([^()]*\))?(?:\.\w+(?:\([^()]*\))?)*)')
# Tinkers 3 IDs that wrap an Identifier in 26.3 (ResourceId), see Teil 27d
RESOURCE_IDS = ('MaterialId', 'MaterialStatsId', 'ModifierId', 'ToolStatId', 'Pattern', 'MaterialVariantId')
# built-in registry entries that 26.3 wants as Holder
HOLDER_REGISTRIES = {'MobEffect': 'MOB_EFFECT', 'Attribute': 'ATTRIBUTE', 'Potion': 'POTION', 'EntityType<?>': 'ENTITY_TYPE'}


def expr_at(line, col):
    """span of the expression javac points at (col is 1-based)"""
    m = EXPR.match(line, max(0, col - 1))
    return (m.start(), m.end()) if m and m.end() > m.start() else None


def fix_line(line, col, msg, extra):
    if msg.startswith('incompatible types: Optional<'):
        return fix_optional(line, col, msg)
    m = re.match(r'incompatible types: (\w+) cannot be converted to Identifier$', msg)
    if m and m.group(1) in RESOURCE_IDS:
        span = expr_at(line, col)
        if span:
            return line[:span[1]] + '.location()' + line[span[1]:]
    m = re.match(r'incompatible types: ([\w<>?]+) cannot be converted to Holder<', msg)
    if m and m.group(1) in HOLDER_REGISTRIES:
        span = expr_at(line, col)
        if span:
            reg = HOLDER_REGISTRIES[m.group(1)]
            return line[:span[0]] + f'net.minecraft.core.registries.BuiltInRegistries.{reg}.wrapAsHolder(' + line[span[0]:span[1]] + ')' + line[span[1]:]
    # Teil 27e: recipes in 26.3 want RecipeType<? extends Recipe<C>>; the raw type is return-type-substitutable
    if re.match(r'getType\(\) in \w+ (cannot implement|clashes with) getType\(\) in Recipe', msg):
        return line.replace('RecipeType<?> getType()', 'RecipeType getType()', 1)
    if re.match(r'getSerializer\(\) in \w+ (cannot implement|clashes with) getSerializer\(\) in Recipe', msg):
        return re.sub(r'RecipeSerializer<[^()]*> getSerializer\(\)', 'RecipeSerializer getSerializer()', line, count=1)
    if msg == 'cannot find symbol':
        sym = extra.get('symbol', '')
        loc = extra.get('location', '')
        if sym == 'method location()' and 'ResourceKey' in loc:
            idx = line.find('.location()', max(0, col - 2))
            if idx < 0:
                idx = line.find('.location()')
            if idx >= 0:
                return line[:idx] + '.identifier()' + line[idx + len('.location()'):]
        if sym == 'method getItems()' and 'Ingredient' in loc:
            return line.replace('.getItems()', '.items().map(net.minecraft.world.item.ItemStack::new).toArray(net.minecraft.world.item.ItemStack[]::new)', 1)
        if sym == 'method isInstantenous()':
            return line.replace('.isInstantenous()', '.isInstantaneous()')
        if sym.startswith('method setSecondsOnFire('):
            return line.replace('.setSecondsOnFire(', '.igniteForSeconds(')
        if sym == 'method isFluidEqual(FluidStack)':
            return re.sub(r'([\w.()]+?)\.isFluidEqual\(', r'net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents(\1, ', line, count=1)
        if sym.startswith('method getHolder(') and 'Registry' in loc:
            return line.replace('.getHolder(', '.get(', 1)
        if sym.startswith('method getHolderOrThrow(') :
            return line.replace('.getHolderOrThrow(', '.getOrThrow(', 1)
        if sym == 'method getRegistryName()' and 'Registry' in loc:
            return line.replace('.getRegistryName()', '.key().identifier()', 1)
        if sym == 'method getKeys()' and 'Registry' in loc:
            return line.replace('.getKeys()', '.keySet()', 1)
        if sym == 'method getValues()' and 'Registry' in loc:
            return line.replace('.getValues()', '.stream().toList()', 1)
        if sym == 'method getEntries()' and 'Registry' in loc:
            return line.replace('.getEntries()', '.entrySet()', 1)
    return None


def main(argv):
    log = Path(argv[0]).read_text(encoding='utf-8', errors='replace').split('\n')
    dry = '--dry' in argv
    fixes = defaultdict(list)  # file -> [(line, col, msg, extra)]
    i = 0
    while i < len(log):
        m = ERR.match(log[i])
        if not m:
            i += 1
            continue
        path, ln, msg = m.group(1), int(m.group(2)), m.group(3)
        extra = {}
        col = 0
        j = i + 1
        while j < len(log) and not ERR.match(log[j]) and j < i + 8:
            t = log[j]
            if re.match(r'^\s*\^\s*$', t):
                col = t.index('^') + 1
            ts = t.strip()
            for key in ('symbol', 'location', 'required', 'found'):
                if ts.startswith(key + ':'):
                    extra[key] = ts[len(key) + 1:].strip()
            j += 1
        fixes[path].append((ln, col, msg, extra))
        i = j
    total = 0
    for path, items in fixes.items():
        p = Path(path)
        lines = p.read_text(encoding='utf-8').split('\n')
        changed = False
        # right to left on the same line so earlier columns stay valid
        for ln, col, msg, extra in sorted(items, key=lambda x: (x[0], -x[1])):
            old = lines[ln - 1]
            new = fix_line(old, col, msg, extra)
            if new is not None and new != old:
                lines[ln - 1] = new
                changed = True
                total += 1
                if dry:
                    print(f'{p.name}:{ln}: {old.strip()}  ->  {new.strip()}')
        if changed and not dry:
            p.write_text('\n'.join(lines), encoding='utf-8')
    print(f'{total} spots fixed')


if __name__ == '__main__':
    main(sys.argv[1:])
