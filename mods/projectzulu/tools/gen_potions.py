# Teil 11 – Potions (runs inside gen.py: write, n, NS, lang, assets).
#
# Brewing recipes of Project Zulu, worked out from the 1.5.2 bitmask brewing (PotionHelper.applyIngredient /
# potionRequirements / potionAmplifiers with PotionManager's Zulu requirements, "Alter Vanilla Potion Requirements"
# and the ingredient strings of TileEntityZuluBrewingStand + ItemGenerics). A simulation of every damage value the
# Zulu brewing stand could reach from a water bottle gave these steps (base -> ingredient -> result):
#   awkward + feather                        -> slowfall 45 s        (Zulu stand: feather "-0+1-2-3+10&4-4")
#   awkward + shiny bauble                   -> Shiny! (bit 8, instant, no effect) = base of the "Shiny" tier
#   awkward + prickly powder / powder slush  -> thorn 45 s
#   awkward + glowing goo                    -> curse 45 s
#   Shiny!  + feather / prickly powder / powder slush / glowing goo -> the same effect, level II 97.5 s
#   X + redstone -> long (120 s; Shiny tier II 135 s), X + glowstone -> strong (II 22.5 s; Shiny tier III 48.75 s),
#   long <-> strong both ways (redstone/glowstone flip bits 5 and 6)
#   fire resistance (normal or long) + glowstone -> incendiary (instant)
#   leaping (normal/long/strong) + fermented spider eye -> slowfall (normal/long/strong)   [vanilla: slowness]
#   thorn (normal/long/strong) + fermented spider eye    -> resistance (normal/long/strong)
#   awkward + antennae -> night vision, + talon / large heart / large unhealthy heart -> strength,
#   + gill -> water breathing, + frog legs / rabbits foot -> leaping, + bark -> resistance 180 s,
#   + small heart / small unhealthy heart -> haste 270 s; haste + fermented spider eye -> mining fatigue 90 s
# Cleansing needs bits 0-3 all set, which no ingredient does: in 1.5.2 it only existed in the creative tab.
# Not ported: the Shiny tier of vanilla effects (Shiny! + sugar = speed II 390 s, ...) and incendiary II (only from
# that tier), the reshuffled requirements of the vanilla potions themselves (26.3 keeps its own vanilla recipes).

CONTAINERS = ('potion', 'splash_potion', 'lingering_potion')
ZULU = ('slowfall', 'thorn', 'curse', 'cleansing')
VANILLA_ITEMS = {'feather', 'glowstone_dust', 'redstone', 'fermented_spider_eye', 'gunpowder', 'dragon_breath'}


def item_id(x):
    return 'minecraft:' + x if x in VANILLA_ITEMS else n(x)


def potion_id(x):
    return x if ':' in x else n(x)


def brew(container, base, reagent, out, out_container=None, path=None):
    out_container = out_container or container
    b, o = potion_id(base), potion_id(out)
    path = path or f'data/{NS}/recipe/brewing/{container}_{b.split(":")[1]}_{reagent}_{o.split(":")[1]}.json'
    write(path, {
        'type': 'minecraft:brewing',
        'input': {'item': f'minecraft:{container}', 'potion_contents': {'potions': b}},
        'output': {'components': {'minecraft:potion_contents': {'potion': o}}, 'id': f'minecraft:{out_container}'},
        'reagent': {'item': item_id(reagent)},
    })


mixes = []  # (base, reagent, result)
for e, reagents in (('slowfall', ['feather']), ('thorn', ['prickly_powder', 'powder_slush']), ('curse', ['glowing_goo'])):
    for r in reagents:
        mixes.append(('minecraft:awkward', r, e))
        mixes.append(('shining', r, f'shiny_{e}'))
mixes.append(('minecraft:awkward', 'shiny_bauble', 'shining'))
for e in ZULU + tuple(f'shiny_{z}' for z in ZULU) + ('haste', 'resistance', 'mining_fatigue'):
    if e in ('cleansing', 'shiny_cleansing'):
        continue  # not brewable in 1.5.2
    mixes += [(e, 'redstone', f'long_{e}'), (e, 'glowstone_dust', f'strong_{e}'),
              (f'long_{e}', 'glowstone_dust', f'strong_{e}'), (f'strong_{e}', 'redstone', f'long_{e}')]
for fr in ('minecraft:fire_resistance', 'minecraft:long_fire_resistance'):
    mixes.append((fr, 'glowstone_dust', 'incendiary'))
for pre in ('', 'long_', 'strong_'):
    mixes.append((f'{pre}thorn', 'fermented_spider_eye', f'{pre}resistance'))
    mixes.append((f'{pre}haste', 'fermented_spider_eye', f'{pre}mining_fatigue'))
mixes.append(('minecraft:strong_leaping', 'fermented_spider_eye', 'strong_slowfall'))
for r, out in (('antennae', 'minecraft:night_vision'), ('talon', 'minecraft:strength'), ('large_heart', 'minecraft:strength'),
               ('large_unhealthy_heart', 'minecraft:strength'), ('gill', 'minecraft:water_breathing'),
               ('frog_legs', 'minecraft:leaping'), ('rabbits_foot', 'minecraft:leaping'), ('bark', 'resistance'),
               ('small_heart', 'haste'), ('small_unhealthy_heart', 'haste')):
    mixes.append(('minecraft:awkward', r, out))

for c in CONTAINERS:
    for base, reagent, out in mixes:
        brew(c, base, reagent, out)
    # "Alter Vanilla Potion Requirements": leaping + fermented spider eye gave slowfall, not slowness
    for pre in ('', 'long_'):
        brew(c, f'minecraft:{pre}leaping', 'fermented_spider_eye', f'{pre}slowfall',
             path=f'data/minecraft/recipe/brewing/{c}_{pre}leaping_fermented_spider_eye.json')

# splash (gunpowder) and lingering (dragon's breath) versions of every Zulu potion
POTIONS = ['shining', 'incendiary', 'strong_incendiary']
for e in ZULU:
    POTIONS += [e, f'long_{e}', f'strong_{e}', f'shiny_{e}', f'long_shiny_{e}', f'strong_shiny_{e}']
for e in ('haste', 'resistance', 'mining_fatigue'):
    POTIONS += [e, f'long_{e}', f'strong_{e}']
for p in POTIONS:
    brew('potion', p, 'gunpowder', p, 'splash_potion')
    brew('splash_potion', p, 'dragon_breath', p, 'lingering_potion')

# ---------------------------------------------------------------- names (PotionManager.registerPotion)
EFFECTS = {'bubbling': 'Shiny!', 'incendiary': 'Incendiary', 'slowfall': 'Slowfall', 'cleansing': 'Cleansing',
           'curse': 'Curse', 'thorn': 'Thorn'}
for e, title in EFFECTS.items():
    lang[f'effect.{NS}.{e}'] = title
for name, (potion, arrow) in {
        'shining': ('Shining Potion', 'Arrow of Shining'), 'incendiary': ('Incendiary Potion', 'Arrow of Incendiary'),
        'slowfall': ('Slowfall Potion', 'Arrow of Slowfall'), 'cleansing': ('Cleansing Potion', 'Arrow of Cleansing'),
        'curse': ('Cursed Potion', 'Arrow of Curse'), 'thorn': ('Thorn Potion', 'Arrow of Thorn'),
        'haste': ('Potion of Haste', 'Arrow of Haste'), 'resistance': ('Potion of Resistance', 'Arrow of Resistance'),
        'mining_fatigue': ('Potion of Mining Fatigue', 'Arrow of Mining Fatigue')}.items():
    lang[f'item.minecraft.potion.effect.{NS}.{name}'] = potion
    lang[f'item.minecraft.splash_potion.effect.{NS}.{name}'] = 'Splash ' + potion
    lang[f'item.minecraft.lingering_potion.effect.{NS}.{name}'] = 'Lingering ' + potion
    lang[f'item.minecraft.tipped_arrow.effect.{NS}.{name}'] = arrow

# ---------------------------------------------------------------- effect icons
# The original drew them from projectzulucore/gui/guielements.png (255x255, 18x18 icons from y=198 like the vanilla
# inventory sheet). The GUI atlas cuts them out with an unstitch source. Shiny! and incendiary used the empty
# slot (1,2) of that sheet; both are instant effects and almost never show up.
assets.append(('mods/projectzulucore/gui/guielements.png', f'assets/{NS}/textures/gui/zulu_effects.png'))
ICONS = {'slowfall': (0, 0), 'cleansing': (1, 0), 'curse': (2, 0), 'thorn': (3, 0), 'bubbling': (1, 2), 'incendiary': (1, 2)}
write('assets/minecraft/atlases/gui.json', {'sources': [{
    'type': 'minecraft:unstitch',
    'resource': n('gui/zulu_effects'),
    'divisor_x': 255.0,
    'divisor_y': 255.0,
    'regions': [{'sprite': n(f'mob_effect/{e}'), 'x': x * 18, 'y': 198 + y * 18, 'width': 18, 'height': 18}
                for e, (x, y) in ICONS.items()],
}]})
