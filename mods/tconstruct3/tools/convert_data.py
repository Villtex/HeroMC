#!/usr/bin/env python3
"""Converts the data of Tinkers' Construct 3.12.1 and Mantle 1.11.117 (Forge 1.20.1) to NeoForge 26.3 (Teil 27a).

    python3 tools/reference_tc3.py                  (the release tags, in .reference/tc3)
    python3 mods/tconstruct3/tools/convert_data.py  (writes mods/tconstruct3/src/converted, mods/mantle/src/converted)

What it does, file by file (data only; assets come from the tag at build time, see buildSrc ImportTagAssets):
  * namespace tconstruct -> tconstruct3 in paths, ids, tags, keys (mod tconstruct3; 1.4.3 keeps tconstruct)
  * folders of 26.3: recipes->recipe, loot_tables->loot_table, advancements->advancement, structures->structure,
    functions->function, tags/items->tags/item (blocks, fluids, entity_types, game_events, ...),
    forge/biome_modifier->neoforge/biome_modifier, data/forge/tags->data/c/tags; the Forge global loot modifier list
    is dropped (26.3 loads every file in loot_modifiers/)
  * forge: conditions, ingredient types, loot conditions, biome modifiers and attributes -> neoforge:/minecraft:;
    forge: tags -> c: tags (renamed where the c: convention differs, see TAG_RENAME)
  * load conditions "conditions" -> "neoforge:conditions", plus {"type": "worldgen152:world_type", "legacy": false}
    on everything that may only exist in Hexxit Integrated (GATED); forge:conditional recipes are split into one
    file per alternative
  * 26.3 formats: item stacks ({"item"} -> {"id"}, NBT -> components), ingredients ("x" / "#tag", neoforge custom
    ingredient types), recipe results, loot tables (condition/modifier/type), item, entity, block and location
    predicates, advancement icons and triggers
  * structure templates (.nbt): block names renamed
Anything it cannot convert with certainty is listed in mods/tconstruct3/tools/convert_report.md (and kept as it was,
or as minecraft:custom_data for unknown item NBT) instead of guessed. Run it again after changing a rule; the
output folders are rewritten completely.
"""
import collections
import gzip
import io
import json
import os
import re
import shutil
import struct
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
REF = os.path.join(ROOT, '.reference', 'tc3')
OUT_TC = os.path.join(ROOT, 'mods', 'tconstruct3', 'src', 'converted')
OUT_MANTLE = os.path.join(ROOT, 'mods', 'mantle', 'src', 'converted')
REPORT = os.path.join(ROOT, 'mods', 'tconstruct3', 'tools', 'convert_report.md')
NEO_DATA = None  # c: tags NeoForge 26.3 defines, read from the jar in the Gradle cache (only for the report)

NS_OLD, NS_NEW = 'tconstruct', 'tconstruct3'
WORLD_INTEGRATED = {'type': 'worldgen152:world_type', 'legacy': False}

# ------------------------------------------------------------------------------------------------ renames
FOLDERS = {'recipes': 'recipe', 'loot_tables': 'loot_table', 'advancements': 'advancement', 'structures': 'structure',
           'functions': 'function', 'predicates': 'predicate', 'item_modifiers': 'item_modifier'}
TAG_FOLDERS = {'items': 'item', 'blocks': 'block', 'fluids': 'fluid', 'entity_types': 'entity_type',
               'game_events': 'game_event', 'functions': 'function'}
# exact forge: ids that are not tags
FORGE_IDS = {
    'forge:mod_loaded': 'neoforge:mod_loaded', 'forge:and': 'neoforge:and', 'forge:or': 'neoforge:or',
    'forge:not': 'neoforge:not', 'forge:true': 'neoforge:always', 'forge:false': 'neoforge:never',
    'forge:item_exists': 'neoforge:registered', 'forge:tag_empty': 'neoforge:tag_empty',
    'forge:intersection': 'neoforge:intersection', 'forge:difference': 'neoforge:difference',
    'forge:compound': 'neoforge:compound',
    'forge:can_tool_perform_action': 'neoforge:can_item_perform_ability', 'forge:loot_table_id': 'neoforge:loot_table_id',
    'forge:add_features': 'neoforge:add_features', 'forge:add_spawns': 'neoforge:add_spawns',
    'forge:remove_features': 'neoforge:remove_features', 'forge:remove_spawns': 'neoforge:remove_spawns',
    # attributes: Forge's own ones became vanilla attributes (or stayed NeoForge ones)
    'forge:block_reach': 'minecraft:block_interaction_range', 'forge:entity_reach': 'minecraft:entity_interaction_range',
    'forge:step_height_addition': 'minecraft:step_height', 'forge:entity_gravity': 'minecraft:gravity',
    'forge:swim_speed': 'neoforge:swim_speed', 'forge:nametag_distance': 'neoforge:nametag_distance',
}
FORGE_UNSUPPORTED = {'forge:nbt', 'forge:partial_nbt', 'forge:conditional'}  # handled structurally or reported
# first path element of a forge: tag whose c: name differs (c: conventions of NeoForge 1.21+)
TAG_RENAME = {
    'slimeball': 'slime_balls', 'slimeballs': 'slime_balls', 'stone': 'stones', 'normal_stone': 'normal_stones',
    'sand': 'sands', 'glass': 'glass_blocks', 'cobblestone': 'cobblestones', 'gravel': 'gravels', 'obsidian': 'obsidians',
    'netherrack': 'netherracks', 'string': 'strings', 'leather': 'leathers', 'gunpowder': 'gunpowders',
    'heads': 'skulls', 'shears': 'tools/shear', 'workbench': 'player_workstations/crafting_tables',
    'workbenches': 'player_workstations/crafting_tables',
}

REPORT_DATA = collections.defaultdict(collections.Counter)


def note(section, text, where=''):
    REPORT_DATA[section][text if not where else f'{text}  ({where})'] += 1


ID_RE = re.compile(r'(?<![a-z0-9_.\-])' + NS_OLD + r':')
LANG_KEY_RE = re.compile(r'^[a-z0-9_]+(\.[a-z0-9_\-]+)+$')
LANG_NS_RE = re.compile(r'(?<![^.])' + NS_OLD + r'(?=\.)')
FORGE_RE = re.compile(r'^(#?)forge:([a-z0-9_/.\-]+)$')


def forge_tag(path):
    parts = path.split('/')
    head = TAG_RENAME.get(parts[0])
    return '/'.join(([head] if head else [parts[0]]) + parts[1:])


C_TAGS_USED = collections.Counter()


def fix_str(s, where=''):
    """Renames inside one string: tconstruct: ids, forge: ids and forge: tags."""
    if 'forge:' in s:
        if s in FORGE_IDS:
            return FORGE_IDS[s]
        if s in FORGE_UNSUPPORTED:
            note('forge ids without a 26.3 counterpart', s, where)
            return s
        m = FORGE_RE.match(s)
        if m:
            new = 'c:' + forge_tag(m.group(2))
            C_TAGS_USED[new] += 1
            return m.group(1) + new
        if s.startswith('forge:') or s.startswith('#forge:'):
            note('forge strings left as they were', s, where)
    if NS_OLD + ':' in s:
        s = ID_RE.sub(NS_NEW + ':', s)
    if NS_OLD + '.' in s and LANG_KEY_RE.match(s):
        # translation keys follow the registry names (item.tconstruct3.x); the language files are renamed the same way
        s = LANG_NS_RE.sub(NS_NEW, s)
    return s


def rename_all(o, where):
    if isinstance(o, dict):
        return {fix_str(k, where): rename_all(v, where) for k, v in o.items()}
    if isinstance(o, list):
        return [rename_all(v, where) for v in o]
    if isinstance(o, str):
        return fix_str(o, where)
    return o


# ------------------------------------------------------------------------------------------------ SNBT -> components
class Snbt:
    """Small SNBT reader (what the data uses: compounds, lists, strings, numbers with suffixes, bare words)."""

    def __init__(self, text):
        self.t, self.i = text, 0

    def ws(self):
        while self.i < len(self.t) and self.t[self.i].isspace():
            self.i += 1

    def value(self):
        self.ws()
        c = self.t[self.i]
        if c == '{':
            self.i += 1
            out = {}
            self.ws()
            if self.t[self.i] == '}':
                self.i += 1
                return out
            while True:
                self.ws()
                k = self.word()
                self.ws()
                assert self.t[self.i] == ':', self.t
                self.i += 1
                out[k] = self.value()
                self.ws()
                c = self.t[self.i]
                self.i += 1
                if c == '}':
                    return out
                assert c == ',', self.t
        if c == '[':
            self.i += 1
            out = []
            self.ws()
            if self.t[self.i:self.i + 2] in ('B;', 'I;', 'L;'):
                self.i += 2
            self.ws()
            if self.t[self.i] == ']':
                self.i += 1
                return out
            while True:
                out.append(self.value())
                self.ws()
                c = self.t[self.i]
                self.i += 1
                if c == ']':
                    return out
                assert c == ',', self.t
        w = self.word()
        if not isinstance(w, str) or w.startswith('"'):
            return w
        m = re.fullmatch(r'(-?\d+(?:\.\d+)?)([bBsSlLfFdD]?)', w)
        if m:
            num, suf = m.groups()
            if suf in 'bB' and suf:
                return int(num)
            return float(num) if ('.' in num or suf in 'fFdD' and suf) else int(num)
        if w in ('true', 'false'):
            return w == 'true'
        return w

    def word(self):
        self.ws()
        if self.t[self.i] in '"\'':
            q = self.t[self.i]
            self.i += 1
            out = []
            while self.t[self.i] != q:
                if self.t[self.i] == '\\':
                    self.i += 1
                out.append(self.t[self.i])
                self.i += 1
            self.i += 1
            return ''.join(out)
        j = self.i
        while self.i < len(self.t) and (self.t[self.i].isalnum() or self.t[self.i] in '_-.+'):
            self.i += 1
        return self.t[j:self.i]


# provisional component names of Tinkers' item data (Teil 27d decides the final ones and fixes this table)
# tool NBT of 1.20.1 -> data components of Teil 27d (slimeknights.tconstruct.library.tools.nbt.ToolDataComponents)
TOOL_COMPONENTS = {'tic_materials': 'materials', 'tic_upgrades': 'upgrades', 'tic_modifiers': 'modifiers',
                   'tic_stats': 'stats', 'tic_multipliers': 'multipliers', 'tic_persistent': 'persistent_data',
                   'tic_volatile_data': 'volatile_data'}


def nbt_to_components(nbt, where):
    if isinstance(nbt, str):
        nbt = Snbt(nbt).value()
    comps, rest = {}, {}
    for k, v in nbt.items():
        if k == 'Damage':
            if v:
                comps['minecraft:damage'] = v
        elif k == 'tic_broken':
            if v:
                comps[f'{NS_NEW}:broken'] = {}  # unit component
        elif k in TOOL_COMPONENTS:
            comps[f'{NS_NEW}:{TOOL_COMPONENTS[k]}'] = v  # same NBT shape as 1.20.1 (ToolDataComponents)
        elif k == 'tic_display':
            if v:
                comps[f'{NS_NEW}:display'] = {}  # unit component
        elif k == 'tank' and isinstance(v, dict) and set(v) <= {'Amount', 'FluidName'}:
            comps[f'{NS_NEW}:tank'] = {'id': v.get('FluidName'), 'amount': v.get('Amount', 0)}
        elif k == 'Material':
            comps[f'{NS_NEW}:material'] = v
        elif k == 'Potion':
            comps['minecraft:potion_contents'] = {'potion': v}
        elif k == 'display' and isinstance(v, dict) and set(v) <= {'Name'}:
            comps['minecraft:custom_name'] = rename_all(json.loads(v['Name']), where)
        else:
            rest[k] = v
    if rest:
        comps['minecraft:custom_data'] = rest
        note('item NBT kept as minecraft:custom_data (no component known)', json.dumps(rest), where)
    if 'tank' in nbt:
        note('item NBT tank converted to provisional component tconstruct3:tank (27i decides)', ','.join(sorted(nbt)), where)
    return comps


# ------------------------------------------------------------------------------------------------ item stacks, ingredients
ING_KEYS = {'ingredient', 'ingredients', 'input', 'inputs', 'cast', 'tools', 'tool', 'pattern', 'pattern_item',
            'children', 'base', 'subtracted', 'match', 'bottle', 'extra_requirements', 'clear_input', 'display',
            'repair', 'repair_ingredient', 'items_ingredient', 'container'}


def ingredient(o, where):
    """1.20 ingredient JSON -> 26.3 (string id, "#tag", list, or neoforge custom ingredient)."""
    if isinstance(o, list):
        flat = [ingredient(v, where) for v in o]
        return flat[0] if len(flat) == 1 else flat
    if not isinstance(o, dict):
        return o
    keys = set(o)
    if 'fluid' in keys:
        return o  # fluid ingredient (Mantle FluidIngredient), not an item
    if keys == {'item'}:
        return o['item']
    if keys == {'tag'}:
        return '#' + o['tag']
    if 'type' in o:
        t = o['type']
        rest = {k: v for k, v in o.items() if k != 'type'}
        if t in ('neoforge:intersection',):
            rest['children'] = [ingredient(c, where) for c in rest.get('children', [])]
        elif t == 'neoforge:difference':
            rest['base'] = ingredient(rest.get('base'), where)
            rest['subtracted'] = ingredient(rest.get('subtracted'), where)
        elif t in ('forge:nbt', 'forge:partial_nbt'):
            note('NBT ingredients (rewrite as neoforge:components)', t, where)
            return o
        else:
            # Tinkers / Mantle custom ingredient: nested ingredients keep the same rules
            rest = {k: (ingredient(v, where) if k in ING_KEYS else v) for k, v in rest.items()}
            note('custom ingredient types (port in 27b-27f)', t)
        return {'neoforge:ingredient_type': t, **rest}
    if keys <= {'item', 'tag', 'amount_needed', 'amount', 'count', 'nbt', 'display', 'ingredient'}:
        # sized ingredients of Tinkers (amount_needed) or Mantle: keep the shape, ingredient part in 26.3 form
        note('sized/mixed ingredient objects kept in Tinkers form', ','.join(sorted(keys)))
        return o
    note('unknown ingredient objects', ','.join(sorted(keys)), where)
    return o


def stack(o, where):
    """Item output: {"item", "count", "nbt"} -> {"id", "count", "components"}; strings -> {"id"}; tags kept."""
    if isinstance(o, str):
        return {'id': o}
    if isinstance(o, dict) and 'item' in o and 'fluid' not in o:
        out = {'id': o['item']}
        if 'count' in o:
            out['count'] = o['count']
        if 'nbt' in o:
            out['components'] = nbt_to_components(o['nbt'], where)
        for k in o:
            if k not in ('item', 'count', 'nbt'):
                out[k] = o[k]
        return out
    return o


def recipe_like(o, where, key=None):
    """Generic walk over recipe-shaped data: ingredient keys -> ingredients, output keys -> stacks."""
    if isinstance(o, dict):
        out = {}
        for k, v in o.items():
            if k == 'key' and isinstance(v, dict):
                out[k] = {s: ingredient(i, where) for s, i in v.items()}
            elif k in ING_KEYS and isinstance(v, (dict, list)) and not (isinstance(v, list) and v and isinstance(v[0], str)):
                out[k] = ingredient(v, where)
            elif k in ('result', 'leftover', 'adult_result', 'output', 'icon'):
                out[k] = stack(v, where) if isinstance(v, dict) and 'item' in v and 'fluid' not in v else v
            elif k == 'results' and isinstance(v, dict):
                out[k] = {rk: stack(rv, where) for rk, rv in v.items()}
            else:
                out[k] = recipe_like(v, where, k)
        return out
    if isinstance(o, list):
        return [recipe_like(v, where, key) for v in o]
    return o


VANILLA_RECIPES = {'minecraft:crafting_shaped', 'minecraft:crafting_shapeless', 'minecraft:smelting',
                   'minecraft:blasting', 'minecraft:smoking', 'minecraft:campfire_cooking', 'minecraft:stonecutting',
                   'minecraft:smithing_transform', 'minecraft:smithing_trim'}


def convert_recipe(o, where):
    t = o.get('type')
    o = recipe_like(o, where)
    if t in VANILLA_RECIPES:
        r = o.get('result')
        if isinstance(r, str):
            o['result'] = {'id': r}
        if t == 'minecraft:stonecutting' and 'count' in o:
            o['result']['count'] = o.pop('count')
        if t == 'minecraft:smithing_transform':
            for k in ('template', 'base', 'addition'):
                if k in o:
                    o[k] = ingredient(o[k], where)
    elif t and not t.startswith((NS_NEW + ':', 'mantle:')):
        note('recipe types of other mods (kept, need that mod)', t)
    return o


# ------------------------------------------------------------------------------------------------ predicates
def items_value(items=None, tag=None):
    if tag:
        return '#' + tag
    if items is None:
        return None
    if isinstance(items, list):
        return items[0] if len(items) == 1 else items
    return items


def item_predicate(p, where):
    if not isinstance(p, dict):
        return p
    if 'type' in p and set(p) <= {'type', 'predicate'}:
        # Forge custom item predicate (tconstruct:tool_stack) -> 26.3 item sub predicate of the same name
        note('custom item predicates as item sub predicates (register in 27d/27e)', p['type'])
        return {'predicates': {p['type']: p.get('predicate', {})}}
    out, preds = {}, {}
    if 'items' in p or 'tag' in p:
        v = items_value(p.get('items'), p.get('tag'))
        if v is not None:
            out['items'] = v
    for k, v in p.items():
        if k in ('items', 'tag'):
            continue
        if k == 'count':
            out['count'] = v
        elif k == 'durability':
            preds['minecraft:damage'] = {'durability': v}
        elif k in ('enchantments', 'stored_enchantments'):
            preds['minecraft:' + k] = [{**({'enchantments': e['enchantment']} if 'enchantment' in e else {}),
                                        **({'levels': e['levels']} if 'levels' in e else {})} for e in v]
        elif k == 'potion':
            preds['minecraft:potion_contents'] = v
        elif k == 'nbt':
            out['components'] = nbt_to_components(v, where)
        else:
            note('unknown item predicate keys', k, where)
            out[k] = v
    if preds:
        out['predicates'] = preds
    return out


def block_predicate(p, where):
    if not isinstance(p, dict):
        return p
    out = {}
    v = items_value(p.get('blocks'), p.get('tag'))
    if v is not None:
        out['blocks'] = v
    for k, val in p.items():
        if k in ('blocks', 'tag'):
            continue
        if k in ('state', 'nbt'):
            out[k] = val
        else:
            note('unknown block predicate keys', k, where)
            out[k] = val
    return out


def location_predicate(p, where):
    if not isinstance(p, dict):
        return p
    out = {}
    for k, v in p.items():
        if k == 'structure':
            out['structures'] = v
        elif k == 'biome':
            out['biomes'] = v
        elif k == 'block':
            out['block'] = block_predicate(v, where)
        elif k == 'fluid':
            out['fluid'] = {('fluids' if fk == 'fluid' else fk): (('#' + fv) if fk == 'tag' else fv)
                            for fk, fv in v.items()}
            if 'tag' in out['fluid']:
                out['fluid']['fluids'] = out['fluid'].pop('tag')
        elif k in ('position', 'dimension', 'smokey', 'light', 'can_see_sky'):
            out[k] = v
        else:
            note('unknown location predicate keys', k, where)
            out[k] = v
    return out


TYPE_SPECIFIC = {'slime': 'cube_mob', 'player': 'player', 'fishing_hook': 'fishing_hook', 'lightning': 'lightning',
                 'raider': 'raider', 'sheep': 'sheep'}
ENTITY_SUB = {'nbt', 'location', 'stepping_on', 'flags', 'equipment', 'effects', 'distance', 'vehicle', 'passenger',
              'targeted_entity', 'team', 'slots', 'movement', 'movement_affected_by', 'periodic_tick'}


def entity_predicate(p, where):
    if isinstance(p, list):  # 1.20 "player": [loot conditions]
        return conditions_to_one(p, where)
    if not isinstance(p, dict):
        return p
    if 'condition' in p or ('type' in p and ':' in str(p.get('type')) and 'entity' in p):
        return loot_condition(p, where)
    out = {}
    for k, v in p.items():
        if k == 'type':
            out['minecraft:entity_type'] = v
        elif k in ('location', 'stepping_on', 'movement_affected_by'):
            out['minecraft:' + k] = location_predicate(v, where)
        elif k in ('vehicle', 'passenger', 'targeted_entity'):
            out['minecraft:' + k] = entity_predicate(v, where)
        elif k == 'equipment':
            out['minecraft:equipment'] = {slot: item_predicate(ip, where) for slot, ip in v.items()}
        elif k == 'type_specific' and isinstance(v, dict) and v.get('type') in TYPE_SPECIFIC:
            out['minecraft:type_specific/' + TYPE_SPECIFIC[v['type']]] = {a: b for a, b in v.items() if a != 'type'}
        elif k in ENTITY_SUB:
            out['minecraft:' + k] = v
        else:
            note('unknown entity predicate keys (e.g. type_specific)', k, where)
            out[k] = v
    return out


def damage_source_predicate(p, where):
    out = {}
    for k, v in p.items():
        if k in ('direct_entity', 'source_entity'):
            out[k] = entity_predicate(v, where)
        else:
            out[k] = v
    return out


# ------------------------------------------------------------------------------------------------ loot
def conditions_to_one(lst, where):
    conds = [loot_condition(c, where) for c in lst]
    if len(conds) == 1:
        return conds[0]
    return {'type': 'minecraft:all_of', 'terms': conds}


def loot_condition(c, where):
    if not isinstance(c, dict):
        return c
    c = dict(c)
    if 'condition' in c:
        c = {'type': c.pop('condition'), **c}
    t = c.get('type')
    if t == 'minecraft:alternative':
        c['type'] = t = 'minecraft:any_of'
    if t == 'minecraft:block_state_property':
        out = {'type': 'minecraft:match_block', 'blocks': c['block']}
        if 'properties' in c:
            out['state'] = c['properties']
        return out
    if t == 'minecraft:match_tool' and 'predicate' in c:
        c['predicate'] = item_predicate(c['predicate'], where)
    elif t == 'minecraft:entity_properties' and 'predicate' in c:
        c['predicate'] = entity_predicate(c['predicate'], where)
    elif t == 'minecraft:damage_source_properties' and 'predicate' in c:
        c['predicate'] = damage_source_predicate(c['predicate'], where)
    elif t == 'minecraft:location_check' and 'predicate' in c:
        c['predicate'] = location_predicate(c['predicate'], where)
    elif t == 'neoforge:can_item_perform_ability' and 'action' in c:
        c['ability'] = c.pop('action')
    if 'term' in c:
        c['term'] = loot_condition(c['term'], where)
    if 'terms' in c:
        c['terms'] = [loot_condition(x, where) for x in c['terms']]
    return c


def loot_function(f, where):
    if not isinstance(f, dict):
        return f
    f = dict(f)
    if 'function' in f:
        f = {'type': f.pop('function'), **f}
    t = f.get('type')
    if 'conditions' in f:
        f['condition'] = conditions_to_one(f.pop('conditions'), where)
    if t == 'minecraft:copy_name':
        out = {'type': 'minecraft:copy_components', 'include': ['minecraft:custom_name'],
               'source': f.get('source', 'block_entity')}
        if 'condition' in f:
            out['condition'] = f['condition']
        return out
    if t == 'minecraft:looting_enchant':
        f['type'] = 'minecraft:enchanted_count_increase'
        f['enchantment'] = 'minecraft:looting'
        return f
    if t in ('minecraft:copy_nbt', 'minecraft:set_nbt'):
        note('loot functions on NBT (rewrite as copy_components/set_components when the block entity is ported)', t, where)
    return f


def modifier_value(lst, where):
    fs = [loot_function(x, where) for x in lst]
    return fs[0] if len(fs) == 1 else fs


def loot_entry(e, where):
    e = dict(e)
    if 'conditions' in e:
        e['condition'] = conditions_to_one(e.pop('conditions'), where)
    if 'functions' in e:
        e['modifier'] = modifier_value(e.pop('functions'), where)
    if 'children' in e:
        e['children'] = [loot_entry(x, where) for x in e['children']]
    t = e.get('type')
    if t == 'minecraft:tag' and 'name' in e:
        e['items'] = '#' + e.pop('name')
    elif t == 'minecraft:loot_table' and 'name' in e:
        e['value'] = e.pop('name')
    return e


def loot_table(o, where):
    o = dict(o)
    if 'functions' in o:
        o['modifier'] = modifier_value(o.pop('functions'), where)
    pools = []
    for p in o.get('pools', []):
        p = dict(p)
        if 'conditions' in p:
            p['condition'] = conditions_to_one(p.pop('conditions'), where)
        if 'functions' in p:
            p['modifier'] = modifier_value(p.pop('functions'), where)
        p['entries'] = [loot_entry(e, where) for e in p.get('entries', [])]
        pools.append(p)
    if 'pools' in o:
        o['pools'] = pools
    return o


def loot_modifier(o, where):
    o = dict(o)
    if 'conditions' in o:
        conds = o.pop('conditions')
        if conds:
            o['condition'] = conditions_to_one(conds, where)
    for k in ('post_conditions',):
        if k in o:
            o[k] = [loot_condition(c, where) for c in o[k]]
    if 'functions' in o:
        o['functions'] = [loot_function(f, where) for f in o['functions']]
    for k in ('entry', 'entries'):
        if k in o:
            o[k] = loot_entry(o[k], where) if isinstance(o[k], dict) else [loot_entry(e, where) for e in o[k]]
    return o


# ------------------------------------------------------------------------------------------------ advancements
def advancement(o, where):
    o = dict(o)
    d = o.get('display')
    if d:
        d = dict(d)
        if 'icon' in d:
            d['icon'] = stack(d['icon'], where)
        if 'background' in d:
            bg = d['background']
            m = re.match(r'^([a-z0-9_.\-]+):textures/(.+)\.png$', bg)
            if m:
                d['background'] = f'{m.group(1)}:{m.group(2)}'
        o['display'] = d
    crit = {}
    for name, c in o.get('criteria', {}).items():
        c = dict(c)
        cond = dict(c.get('conditions', {}))
        trig = c.get('trigger')
        if 'player' in cond:
            cond['player'] = entity_predicate(cond['player'], where)
        if trig == 'minecraft:inventory_changed' and 'items' in cond:
            cond['items'] = [item_predicate(i, where) for i in cond['items']]
        elif trig == 'minecraft:recipe_unlocked' and 'recipe' in cond:
            cond['recipes'] = cond.pop('recipe')
        elif trig == 'minecraft:placed_block':
            if 'location' in cond and isinstance(cond['location'], list):
                cond['location'] = conditions_to_one(cond['location'], where)
            if 'block' in cond:
                note('placed_block with "block" (1.19 form)', trig, where)
            if 'item' in cond:
                cond['item'] = item_predicate(cond['item'], where)
        elif trig == 'minecraft:player_interacted_with_entity':
            if 'item' in cond:
                cond['item'] = item_predicate(cond['item'], where)
            if 'entity' in cond:
                cond['entity'] = entity_predicate(cond['entity'], where)
        elif trig in ('minecraft:location', 'minecraft:tick', 'minecraft:impossible'):
            pass
        elif trig and not trig.startswith((NS_OLD + ':', NS_NEW + ':', 'mantle:')):
            note('advancement triggers not checked', trig, where)
        if cond:
            c['conditions'] = cond
        crit[name] = c
    if 'criteria' in o:
        o['criteria'] = crit
    return o


# ------------------------------------------------------------------------------------------------ files
def category(rest):
    p = rest.split('/')
    if p[0] in ('recipe',):
        return 'recipe'
    if p[0] == 'loot_table':
        return 'loot_table'
    if p[0] == 'advancement':
        return 'advancement'
    if p[0] == 'loot_modifiers':
        return 'loot_modifier'
    if p[0] == 'tags':
        return 'tags'
    if p[0] == 'tinkering':
        return 'tinkering'
    if p[0] == 'mantle':
        return 'mantle'
    if rest.startswith('neoforge/biome_modifier'):
        return 'biome_modifier'
    if p[0] == 'worldgen':
        return 'worldgen'
    return p[0]


def gated(cat, rest):
    """Data that may only exist in Hexxit Integrated: gets the world type condition."""
    if cat == 'loot_table':
        return not rest.startswith('loot_table/blocks/')
    if cat == 'worldgen':
        return rest.startswith('worldgen/structure_set/')
    return cat in ('recipe', 'advancement', 'loot_modifier', 'biome_modifier', 'tinkering', 'mantle')


def map_path(ns, rest):
    """(namespace, path below it) of 1.20 -> of 26.3, or None to drop the file."""
    p = rest.split('/')
    if ns == 'forge' and p[0] == 'loot_modifiers':
        return None  # global_loot_modifiers.json: 26.3 loads all modifiers without a list
    if ns == 'forge' and p[0] == 'tags':
        ns = 'c'
        folder = TAG_FOLDERS.get(p[1], p[1])
        tag = forge_tag('/'.join(p[2:])[:-5]) + '.json'
        return ns, '/'.join(['tags', folder, tag])
    if ns == NS_OLD:
        ns = NS_NEW
    if p[0] == 'forge':
        p[0] = 'neoforge'
    if p[0] in FOLDERS:
        p[0] = FOLDERS[p[0]]
    if p[0] == 'tags' and len(p) > 1:
        p[1] = TAG_FOLDERS.get(p[1], p[1])
    return ns, '/'.join(p)


def with_world_condition(o, where):
    conds = o.pop('conditions', None) if isinstance(o, dict) else None
    conds = [rename_all(c, where) for c in (conds or [])]
    return {'neoforge:conditions': [WORLD_INTEGRATED] + [load_condition(c, where) for c in conds], **o}


def load_condition(c, where):
    c = dict(c)
    t = c.get('type')
    if t == 'neoforge:registered' and 'item' in c:
        c['value'] = c.pop('item')
    if t in ('neoforge:and', 'neoforge:or'):
        c['values'] = [load_condition(x, where) for x in c.get('values', [])]
    if t == 'neoforge:not':
        c['value'] = load_condition(c['value'], where)
    return c


def convert(o, cat, rest, where):
    o = rename_all(o, where)
    if cat == 'recipe':
        o = convert_recipe(o, where)
    elif cat == 'loot_table':
        o = loot_table(o, where)
    elif cat == 'advancement':
        o = advancement(o, where)
    elif cat == 'loot_modifier':
        o = loot_modifier(o, where)
    elif cat in ('tinkering', 'mantle'):
        o = recipe_like(o, where)
        if rest.startswith('mantle/loot_injectors') or rest.startswith('mantle/loot'):
            for pool in o.get('pools', []) if isinstance(o, dict) else []:
                pool['entries'] = [loot_entry(e, where) for e in pool.get('entries', [])]
    elif cat == 'trim_material':
        note('trim materials (26.3: no ingredient/item_model_index; the item gets a provides_trim_material component)',
             rest)
    elif cat == 'worldgen':
        t = o.get('type') if isinstance(o, dict) else None
        if t and t.startswith('minecraft:'):
            note('vanilla worldgen types (check the format against 26.3 in Teil 27j)', f'{rest.split("/")[1]}: {t}')
    return o


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


def convert_file(src, out_base, ns, rest, counts):
    where = f'{ns}/{rest}'
    mapped = map_path(ns, rest)
    if mapped is None:
        note('dropped files', where)
        return
    ons, orest = mapped
    dst = os.path.join(out_base, 'data', ons, orest)
    cat = category(orest)
    counts[cat] += 1
    if src.endswith('.nbt'):
        convert_nbt(src, dst)
        return
    if not src.endswith('.json'):
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        with open(src, encoding='utf-8') as f:
            text = f.read()
        with open(dst, 'w', encoding='utf-8') as f:
            f.write(ID_RE.sub(NS_NEW + ':', text))
        return
    with open(src, encoding='utf-8') as f:
        o = json.load(f)
    if isinstance(o, dict) and o.get('type') == 'forge:conditional':
        alts = o['recipes']
        prev = []
        for i, alt in enumerate(alts):
            conds = [rename_all(c, where) for c in alt.get('conditions', [])]
            conds = [load_condition(c, where) for c in conds if c.get('type') != 'neoforge:always']
            mine = conds + [{'type': 'neoforge:not', 'value': p} for p in prev]
            prev.append(conds[0] if len(conds) == 1 else {'type': 'neoforge:and', 'values': conds}) if conds else None
            r = convert(alt['recipe'], cat, orest, where)
            r = {'conditions': mine, **r} if mine else r
            r = with_world_condition(r, where) if gated(cat, orest) else r
            path = dst if i == 0 else dst[:-5] + f'_alt{i}.json'
            write_json(path, r)
            if not conds and i < len(alts) - 1:
                note('forge:conditional with an unconditional alternative before the last', where)
                break
        note('forge:conditional recipes split into one file per alternative', f'{len(alts)} alternatives')
        return
    top_conds = o.pop('conditions', None) if isinstance(o, dict) and cat not in ('loot_modifier',) else None
    o = convert(o, cat, orest, where)
    if top_conds:
        o = {'conditions': top_conds, **o}
    if isinstance(o, dict) and (gated(cat, orest) or top_conds):
        if gated(cat, orest):
            o = with_world_condition(o, where)
        else:
            conds = [load_condition(rename_all(c, where), where) for c in o.pop('conditions')]
            o = {'neoforge:conditions': conds, **o}
            note('load conditions on ungated data', cat)
    write_json(dst, o)


# ------------------------------------------------------------------------------------------------ binary NBT (structures)
def nbt_read(f, tag):
    if tag == 1:
        return struct.unpack('>b', f.read(1))[0]
    if tag == 2:
        return struct.unpack('>h', f.read(2))[0]
    if tag == 3:
        return struct.unpack('>i', f.read(4))[0]
    if tag == 4:
        return struct.unpack('>q', f.read(8))[0]
    if tag == 5:
        return struct.unpack('>f', f.read(4))[0]
    if tag == 6:
        return struct.unpack('>d', f.read(8))[0]
    if tag == 7:
        n = struct.unpack('>i', f.read(4))[0]
        return f.read(n)
    if tag == 8:
        n = struct.unpack('>H', f.read(2))[0]
        return f.read(n).decode('utf-8')
    if tag == 9:
        et = f.read(1)[0]
        n = struct.unpack('>i', f.read(4))[0]
        return (et, [nbt_read(f, et) for _ in range(n)])
    if tag == 10:
        out = []
        while True:
            t = f.read(1)[0]
            if t == 0:
                return out
            n = struct.unpack('>H', f.read(2))[0]
            name = f.read(n).decode('utf-8')
            out.append((t, name, nbt_read(f, t)))
    if tag == 11:
        n = struct.unpack('>i', f.read(4))[0]
        return list(struct.unpack(f'>{n}i', f.read(4 * n)))
    if tag == 12:
        n = struct.unpack('>i', f.read(4))[0]
        return list(struct.unpack(f'>{n}q', f.read(8 * n)))
    raise ValueError(tag)


def nbt_write(f, tag, v):
    if tag == 1:
        f.write(struct.pack('>b', v))
    elif tag == 2:
        f.write(struct.pack('>h', v))
    elif tag == 3:
        f.write(struct.pack('>i', v))
    elif tag == 4:
        f.write(struct.pack('>q', v))
    elif tag == 5:
        f.write(struct.pack('>f', v))
    elif tag == 6:
        f.write(struct.pack('>d', v))
    elif tag == 7:
        f.write(struct.pack('>i', len(v)) + v)
    elif tag == 8:
        b = v.encode('utf-8')
        f.write(struct.pack('>H', len(b)) + b)
    elif tag == 9:
        et, items = v
        f.write(bytes([et]) + struct.pack('>i', len(items)))
        for x in items:
            nbt_write(f, et, x)
    elif tag == 10:
        for t, name, x in v:
            b = name.encode('utf-8')
            f.write(bytes([t]) + struct.pack('>H', len(b)) + b)
            nbt_write(f, t, x)
        f.write(b'\0')
    elif tag == 11:
        f.write(struct.pack(f'>i{len(v)}i', len(v), *v))
    elif tag == 12:
        f.write(struct.pack(f'>i{len(v)}q', len(v), *v))


def nbt_rename(tag, v):
    if tag == 8:
        return ID_RE.sub(NS_NEW + ':', v)
    if tag == 9:
        return (v[0], [nbt_rename(v[0], x) for x in v[1]])
    if tag == 10:
        return [(t, n, nbt_rename(t, x)) for t, n, x in v]
    return v


def convert_nbt(src, dst):
    with gzip.open(src, 'rb') as f:
        data = io.BytesIO(f.read())
    t = data.read(1)[0]
    n = struct.unpack('>H', data.read(2))[0]
    name = data.read(n).decode('utf-8')
    root = nbt_rename(t, nbt_read(data, t))
    out = io.BytesIO()
    out.write(bytes([t]))
    nbt_write(out, 8, name)
    nbt_write(out, t, root)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    with open(dst, 'wb') as raw, gzip.GzipFile(fileobj=raw, mode='wb', mtime=0) as f:  # mtime 0: same bytes on every run
        f.write(out.getvalue())


# ------------------------------------------------------------------------------------------------ main
def neo_c_tags():
    """c: tags NeoForge 26.3 defines (for the report), from the universal jar in the Gradle cache."""
    import glob
    import zipfile
    jars = glob.glob(os.path.expanduser('~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/*/*/neoforge-*-universal.jar'))
    tags = set()
    for jar in jars[:1]:
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                m = re.match(r'data/c/tags/[a-z_]+/(.+)\.json$', n)
                if m:
                    tags.add('c:' + m.group(1))
    return tags


def main():
    sources = [
        (os.path.join(REF, 'tconstruct', 'src', 'generated', 'resources', 'data'), OUT_TC),
        (os.path.join(REF, 'tconstruct', 'src', 'main', 'resources', 'data'), OUT_TC),
        (os.path.join(REF, 'mantle', 'src', 'generated', 'resources', 'data'), OUT_MANTLE),
        (os.path.join(REF, 'mantle', 'src', 'main', 'resources', 'data'), OUT_MANTLE),
    ]
    if not os.path.isdir(sources[0][0]):
        sys.exit('needs the release tags: python3 tools/reference_tc3.py')
    for out in (OUT_TC, OUT_MANTLE):
        shutil.rmtree(out, ignore_errors=True)
    counts = collections.Counter()
    defined_c = set()
    for base, out in sources:
        if not os.path.isdir(base):
            continue
        for top, dirs, files in os.walk(base):
            dirs.sort()
            for name in sorted(files):
                full = os.path.join(top, name)
                rel = os.path.relpath(full, base).replace('\\', '/')
                ns, rest = rel.split('/', 1)
                convert_file(full, out, ns, rest, counts)
                mapped = map_path(ns, rest)
                if mapped and mapped[0] == 'c' and mapped[1].startswith('tags/'):
                    defined_c.add('c:' + mapped[1].split('/', 2)[2][:-5])
    neo = neo_c_tags()
    for tag, n in sorted(C_TAGS_USED.items()):
        if tag not in neo and tag not in defined_c:
            note('c: tags neither NeoForge nor Tinkers define (fine when other mods fill them)', tag)
    write_report(counts)
    total = sum(counts.values())
    print(f'{total} files converted ->', os.path.relpath(OUT_TC, ROOT), '+', os.path.relpath(OUT_MANTLE, ROOT))
    print('report:', os.path.relpath(REPORT, ROOT))


def write_report(counts):
    lines = ['# Daten-Konverter Tinkers 3.12.1 / Mantle – Bericht',
             '',
             'Erzeugt von `convert_data.py` (nicht von Hand bearbeiten). Was hier steht, hat der Konverter nicht sicher',
             'umsetzen können oder nur vorläufig umgesetzt; die Teile 27b–27k arbeiten die Punkte ab.',
             '',
             '## Dateien je Art', '']
    for k, v in sorted(counts.items()):
        lines.append(f'- {k}: {v}')
    for section in sorted(REPORT_DATA):
        entries = REPORT_DATA[section]
        lines += ['', f'## {section} ({sum(entries.values())})', '']
        for text, n in sorted(entries.items(), key=lambda x: (-x[1], x[0]))[:60]:
            lines.append(f'- {n}× `{text}`' if n > 1 else f'- `{text}`')
        if len(entries) > 60:
            lines.append(f'- … und {len(entries) - 60} weitere')
    with open(REPORT, 'w') as f:
        f.write('\n'.join(lines) + '\n')


if __name__ == '__main__':
    main()
