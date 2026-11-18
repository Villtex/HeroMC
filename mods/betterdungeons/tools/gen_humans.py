#!/usr/bin/env python3
"""Generates the data of the human mobs of Better Dungeons (strand b1: betterdungeons.mob.BDHumans).

Loot tables data/betterdungeons/loot_table/entities/<id>.json from the dropFewItems/onDeath methods of 1.5.2
(EntityHuman and its subclasses, the human bosses, the mages); the level-dependent drops of EntityHuman use the
condition betterdungeons:mob_level. XP orbs and the pirate captain's weapon stay in Java (dropCustomDeathLoot).
nextInt(n + looting) -> set_count 0..n-1 + enchanted_count_increase 0..1 per looting level; dropItem(x, 2) inside such a
loop (two at a time) -> rolls 0..n-1 of stacks of 2, without the looting part.
Also the undead tag for the mobs that were EnumCreatureAttribute.UNDEAD.
Run from anywhere: python3 mods/betterdungeons/tools/gen_humans.py
"""
import json, os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = ROOT + '/src/main/resources'
NS = 'betterdungeons'


def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w', encoding='utf-8') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


LOOTING = {'type': 'minecraft:enchanted_count_increase', 'enchantment': 'minecraft:looting',
           'count': {'type': 'minecraft:uniform', 'min': 0.0, 'max': 1.0}}


def item(name, count=None, looting=False, extra=None):
    e = {'type': 'minecraft:item', 'name': name if ':' in name else f'{NS}:{name}'}
    mods = []
    if count is not None:
        mods.append({'type': 'minecraft:set_count', 'count': count})
    if looting:
        mods.append(LOOTING)
    if extra:
        mods += extra
    if mods:
        e['modifier'] = mods
    return e


def pool(entries, condition=None, rolls=1):
    p = {'rolls': rolls, 'entries': entries if isinstance(entries, list) else [entries]}
    if condition is not None:
        p['condition'] = condition
    return p


def uniform(a, b):
    return {'type': 'minecraft:uniform', 'min': a, 'max': b}


def chance(c):
    return {'type': 'minecraft:random_chance', 'chance': c}


def lvl(a, b=None):
    c = {'type': f'{NS}:mob_level', 'min': a}
    if b is not None:
        c['max'] = b
    return c


def all_of(*terms):
    return {'type': 'minecraft:all_of', 'terms': list(terms)}


def rand_items(name, n):
    """for (k < rand.nextInt(n + looting)) dropItem(name, 1)."""
    return pool(item(name, uniform(0, n - 1), looting=True))


def rand_pairs(name, n):
    """for (k < rand.nextInt(n + looting)) dropItem(name, 2): stacks of two."""
    return pool(item(name, 2), rolls=uniform(0, n - 1))


# Potion damage 1..12 of 1.5.2 (OldGameIds.potion): 7 and 11 had no effect (mundane)
POTIONS = ['regeneration', 'swiftness', 'fire_resistance', 'poison', 'healing', 'night_vision', 'mundane', 'weakness', 'strength',
           'slowness', 'mundane', 'harming']


def potion_entries():
    return [item('minecraft:potion', extra=[{'type': 'minecraft:set_potion', 'id': f'minecraft:{p}'}]) for p in POTIONS]


def potion_pools(base):
    """if (getlvl() >= 2 && rand.nextInt(base - getlvl()) == 0) a random potion 1..12."""
    return [pool(potion_entries(), all_of(lvl(l, l), chance(1.0 / (base - l)))) for l in (2, 3, 4)]


ELEMENTS = ['water', 'poison', 'ice', 'fire', 'dark', 'electric', 'wind', 'life']

# EntityHuman.dropFewItems
HUMAN_BASE = potion_pools(40) + [
    # nextBoolean: an iron ingot at level 2, gold at 3, a diamond at 4
    pool(item('minecraft:iron_ingot'), all_of(lvl(2, 2), chance(0.5))),
    pool(item('minecraft:gold_ingot'), all_of(lvl(3, 3), chance(0.5))),
    pool(item('minecraft:diamond'), all_of(lvl(4, 4), chance(0.5))),
    # lvl >= 0 && rand.nextInt(min(2, 7 - lvl)) == 0: 1..2 of a random totem (essence 0..7)
    pool([item(f'{e}_essence', uniform(1, 2)) for e in ELEMENTS], all_of(lvl(0), chance(0.5))),
]
BASE_REF = pool({'type': 'minecraft:loot_table', 'value': f'{NS}:entities/human_base'})

# diamonds of the bosses: flag (killed by a player) && (rand(5) == 0 || rand(1 + looting) > 0)
def boss_diamonds(count):
    return pool(item('minecraft:diamond', count), all_of({'type': 'minecraft:killed_by_player'}, {
        'type': 'minecraft:any_of', 'terms': [chance(0.2), {
            'type': 'minecraft:random_chance_with_enchanted_bonus', 'enchantment': 'minecraft:looting', 'unenchanted_chance': 0.0,
            'enchanted_chance': {'type': 'minecraft:fraction', 'numerator': {'type': 'minecraft:linear', 'base': 1.0, 'per_level_above_first': 1.0},
                                 'denominator': {'type': 'minecraft:linear', 'base': 2.0, 'per_level_above_first': 1.0}}}]}))


TABLES = {
    'human_base': HUMAN_BASE,
    'armored_skeleton': [rand_items('minecraft:bone', 2), BASE_REF],
    'armored_zombie': [rand_items('minecraft:rotten_flesh', 2), BASE_REF] + potion_pools(80),
    # EntityDwarf.onDeath called super (the drops) only at level 5, which no dwarf has
    'dwarf': [],
    'pirate': [rand_items('minecraft:melon_slice', 2), BASE_REF],
    'armored_pig_zombie': [rand_items('minecraft:glistering_melon_slice', 2), BASE_REF],
    'walker': [BASE_REF], 'specter': [BASE_REF], 'triton': [BASE_REF], 'monkey': [BASE_REF], 'gremlin': [BASE_REF],
    'golem_small': [BASE_REF], 'ogre_armored': [BASE_REF], 'minotaur_armored': [BASE_REF], 'orc_armored': [BASE_REF],
    'goblin': [pool({'type': 'minecraft:loot_table', 'value': f'{NS}:entities/human_base'}, chance(1 / 20))] + potion_pools(40),
    'snow_goblin': [pool({'type': 'minecraft:loot_table', 'value': f'{NS}:entities/human_base'}, chance(1 / 15))] + potion_pools(40),
    'armored_enderman': [BASE_REF, pool(item('minecraft:ender_pearl', 0, looting=True))],
    'friendly_human': [],
    'pirate_boss': [BASE_REF, boss_diamonds(2)],
    'walker_boss': [pool(item('walker_sword')), pool(item('minecraft:diamond_block')), BASE_REF],
    'specter_boss': [BASE_REF, pool(item('defense_sword')), boss_diamonds(2)],
    'gremlin_boss': [pool(item('spider_hook')), boss_diamonds(1)],
    'minotaur_boss': [pool(item('earth_sword')), boss_diamonds(2)],
    'goblin_boss': [pool(item('moon_sword')), BASE_REF],
    'necromancer': [rand_pairs('minecraft:diamond', 3), rand_pairs('minecraft:bone', 3), pool(item('cursed_bone'))],
    'liche': [rand_pairs('minecraft:diamond', 3), rand_pairs('minecraft:bone', 3), rand_items('cursed_bone', 2)],
    'pig_mage': [rand_pairs('minecraft:magma_cream', 3), rand_items('minecraft:gold_ingot', 4), rand_items('cursed_bone', 2)],
}

for name, pools in TABLES.items():
    write(f'data/{NS}/loot_table/entities/{name}.json', {'type': 'minecraft:entity', 'pools': pools,
                                                        'random_sequence': f'{NS}:entities/{name}'})

# EnumCreatureAttribute.UNDEAD (smite, healing/harming potions)
UNDEAD = ['armored_skeleton', 'armored_zombie', 'armored_pig_zombie', 'armored_enderman', 'necromancer', 'liche', 'pig_mage']
tag_path = os.path.join(RES, 'data/minecraft/tags/entity_type/undead.json')
values = []
if os.path.exists(tag_path):
    with open(tag_path, encoding='utf-8') as f:
        values = json.load(f).get('values', [])
values = sorted(set(values) | {f'{NS}:{u}' for u in UNDEAD})
write('data/minecraft/tags/entity_type/undead.json', {'replace': False, 'values': values})
print('loot tables', len(TABLES), 'undead', len(UNDEAD))
