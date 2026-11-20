#!/usr/bin/env python3
"""Generates the assets/data JSON of the Xeno's Reliquary port (mods/xreliquary/src/main/resources), the language
files and original-assets.txt. Everything under resources/assets and resources/data is overwritten."""
import json, os, re, shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = ROOT + '/src/main/resources'
NS = 'xreliquary'
M = 'minecraft:'
for d in ('assets', 'data'):
    shutil.rmtree(os.path.join(RES, d), ignore_errors=True)


def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, 'w') as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write('\n')


def n(x):
    return x if ':' in x else f'{NS}:{x}'


assets = []
en, de = {}, {}


def name(key, english, german):
    en[key] = english
    de[key] = german


def snake(s):
    return re.sub(r'(?<!^)(?=[A-Z])', '_', s).lower()


ITEMS = 'mods/xreliquary/textures/items/'


def itex(t):
    """An original item icon (textures/items/<t>.png) as xreliquary:item/<snake>."""
    dst = snake(t)
    assets.append((ITEMS + t + '.png', f'assets/{NS}/textures/item/{dst}.png'))
    return n('item/' + dst)


def model(m, layers, parent='minecraft:item/generated'):
    write(f'assets/{NS}/models/item/{m}.json', {'parent': parent, 'textures': {f'layer{i}': l for i, l in enumerate(layers)}})
    return n('item/' + m)


def item_def(name_, mdl):
    write(f'assets/{NS}/items/{name_}.json', {'model': mdl})


def plain(m, tints=None):
    r = {'type': 'minecraft:model', 'model': m}
    if tints:
        r['tints'] = tints
    return r


def const(c):
    return {'type': 'minecraft:constant', 'value': c}


WHITE = const(0xFFFFFF)


def flag(on_true, on_false):
    return {'type': 'minecraft:condition', 'property': 'minecraft:custom_model_data', 'index': 0, 'on_true': on_true, 'on_false': on_false}


def tip(item, lines_en, lines_de):
    for i, (e, g) in enumerate(zip(lines_en, lines_de)):
        name(f'item.{NS}.{item}.tooltip.{i}', e, g)


def simple(item, texture, english, german, tips_en=(), tips_de=(), handheld=False):
    item_def(item, plain(model(item, [itex(texture)], 'minecraft:item/handheld' if handheld else 'minecraft:item/generated')))
    name(f'item.{NS}.{item}', english, german)
    tip(item, tips_en, tips_de)


# ---------------------------------------------------------------- shots and magazines (ShotType)
SHOTS = [('empty', 'Empty Casing', 'Leere Hülse', 'An empty shell.', 'Eine leere Hülse.'),
         ('neutral', 'Neutral Shot', 'Neutrale Kugel', 'An ordinary bullet.', 'Eine gewöhnliche Kugel.'),
         ('exorcism', 'Exorcism Shot', 'Exorzismus-Kugel', 'Effective vs. undead.', 'Wirksam gegen Untote.'),
         ('blaze', 'Blaze Shot', 'Lohen-Kugel', 'Deals only fire damage.', 'Macht nur Feuerschaden.'),
         ('ender', 'Ender Shot', 'Ender-Kugel', 'Seek targets, passes through anything.', 'Sucht Ziele, geht durch alles.'),
         ('concussive', 'Concussive Shot', 'Erschütterungs-Kugel', 'Deal damage to a small area.', 'Schaden in kleinem Umkreis.'),
         ('buster', 'Buster Shot', 'Sprengkugel', 'Create a sizable explosion.', 'Erzeugt eine große Explosion.'),
         ('seeker', 'Seeker Shot', 'Sucherkugel', 'Seeks its targets.', 'Sucht ihre Ziele.'),
         ('sand', 'Sand Shot', 'Sandkugel', 'Blinds target. Wrecks creepers.', 'Blendet das Ziel. Zerlegt Creeper.'),
         ('storm', 'Storm Shot', 'Sturmkugel', 'Causes atmospheric weirdness.', 'Bringt das Wetter durcheinander.')]
MAG_TIPS = [('An empty magazine.', 'Ein leeres Magazin.'), ('Ordinary bullets.', 'Gewöhnliche Kugeln.'),
            ('Effective vs. undead.', 'Wirksam gegen Untote.'), ('Deal bonus fire damage.', 'Macht zusätzlichen Feuerschaden.'),
            ('Pierce enemies.', 'Durchschlägt Gegner.'), ('Deal damage to a small area.', 'Schaden in kleinem Umkreis.'),
            ('Create a sizable explosion.', 'Erzeugt eine große Explosion.'), ('Seek targets.', 'Sucht Ziele.'),
            ('Blind targets. Wreck creepers.', 'Blendet Ziele. Zerlegt Creeper.'), ('Causes atmospheric weirdness.', 'Bringt das Wetter durcheinander.')]
SHOT_COLORS = [0xFFFFFF, 0x888888, 0xDDDD00, 0xDD2222, 0x9900EE, 0x00BB00, 0x444444, 0x00BB99, 0xDDDD77, 0x0000DD]
MAG_DE = ['Leeres Magazin', 'Neutrales Magazin', 'Exorzismus-Magazin', 'Lohen-Magazin', 'Ender-Magazin', 'Erschütterungs-Magazin',
          'Sprengmagazin', 'Sucher-Magazin', 'Sand-Magazin', 'Sturm-Magazin']
bullet, bullet_ov = itex('reliquaryBullet'), itex('reliquaryBulletOverlay')
mag, mag_ov = itex('reliquaryMagazine'), itex('reliquaryMagazineOverlay')
model('bullet', [bullet])
model('bullet_loaded', [bullet, bullet_ov])
model('magazine', [mag])
model('magazine_loaded', [mag, mag_ov])
for i, (sid, e_name, d_name, e_tip, d_tip) in enumerate(SHOTS):
    item_def(f'{sid}_shot', plain(n('item/bullet')) if i == 0 else plain(n('item/bullet_loaded'), [WHITE, const(SHOT_COLORS[i])]))
    name(f'item.{NS}.{sid}_shot', e_name, d_name)
    tip(f'{sid}_shot', [e_tip], [d_tip])
    item_def(f'{sid}_magazine', plain(n('item/magazine'), [const(0x333333)]) if i == 0
             else plain(n('item/magazine_loaded'), [const(0x666666), const(SHOT_COLORS[i])]))
    name(f'item.{NS}.{sid}_magazine', e_name.replace('Shot', 'Magazine').replace('Empty Casing', 'Empty Magazine'), MAG_DE[i])
    tip(f'{sid}_magazine', [MAG_TIPS[i][0]], [MAG_TIPS[i][1]])

# ---------------------------------------------------------------- the handgun (overlay coloured by the loaded shot)
gun, gun_ov = itex('reliquaryHandgun'), itex('reliquaryHandgunOverlay')
model('handgun', [gun], 'minecraft:item/handheld')
model('handgun_loaded', [gun, gun_ov], 'minecraft:item/handheld')
item_def('handgun', {'type': 'minecraft:condition', 'property': 'minecraft:damaged',
                     'on_true': plain(n('item/handgun_loaded'), [WHITE, {'type': 'minecraft:custom_model_data', 'index': 0, 'default': 0xFFFFFF}]),
                     'on_false': plain(n('item/handgun'))})
name(f'item.{NS}.handgun', "Hunter's Handgun", 'Pistole des Jägers')
tip('handgun', ['For great justice.', 'Right click fires, hold to reload.'], ['Für die große Gerechtigkeit.', 'Rechtsklick schießt, halten lädt nach.'])

# ---------------------------------------------------------------- condensed potions (PotionKind)
POTIONS = [  # id, en, de, tip en (2), tip de (2), tint (int or 'time')
    ('splash_base', 'Condensed Splash Serum', 'Kondensiertes Wurfserum', 'A base potion for creating', 'condensed splash vials.', 'Grundlage für', 'kondensierte Wurffläschchen.', 0x0055DD),
    ('aphrodite', 'Love Potion #9', 'Liebestrank Nr. 9', 'Makes animals', 'want to mate.', 'Macht Tiere', 'paarungswillig.', 0xCC3366),
    ('poison', 'Splash Poison Serum', 'Wurfserum der Vergiftung', 'Poisons mobs', 'for 60 seconds.', 'Vergiftet Kreaturen', 'für 60 Sekunden.', 0x44BB44),
    ('acid', 'Splash Acid Serum', 'Säure-Wurfserum', 'Deals 12 damage,', 'even vs. Undead.', 'Macht 12 Schaden,', 'auch gegen Untote.', 0xDD9900),
    ('confusion', 'Splash Confusion Serum', 'Wurfserum der Verwirrung', 'Causes confusion', 'for 60 seconds.', 'Verwirrt', 'für 60 Sekunden.', 0xDD22DD),
    ('slowing', 'Splash Slowing Serum', 'Wurfserum der Langsamkeit', 'Causes slowness', 'for 60 seconds.', 'Verlangsamt', 'für 60 Sekunden.', 0x0022FF),
    ('weakness', 'Splash Weakness Serum', 'Wurfserum der Schwäche', 'Causes weakness', 'for 60 seconds.', 'Schwächt', 'für 60 Sekunden.', 0xDDDD22),
    ('wither', 'Splash Wither Serum', 'Wurfserum der Verdorrung', 'Causes wither effect', 'for 60 seconds.', 'Lässt verdorren', 'für 60 Sekunden.', 0x777777),
    ('blinding', 'Splash Blinding Serum', 'Wurfserum der Blindheit', 'Causes blindness', 'for 60 seconds.', 'Blendet', 'für 60 Sekunden.', 0x333333),
    ('ruination', 'Splash Ruination Serum', 'Wurfserum des Verderbens', 'Slows, weakens and', 'poisons for 60 seconds.', 'Verlangsamt, schwächt und', 'vergiftet für 60 Sekunden.', 'time'),
    ('fertilizer', 'Splash Fertilizer Serum', 'Dünger-Wurfserum', 'Grows crops in a', 'wide square pattern.', 'Lässt Pflanzen in einem', 'weiten Quadrat wachsen.', 0xDDDDDD),
    ('empty_vial', 'Empty Vial', 'Leeres Fläschchen', 'An empty vial for', 'condensed potions.', 'Ein leeres Fläschchen für', 'kondensierte Tränke.', None),
    ('solvent', 'Condensed Solvent', 'Kondensiertes Lösungsmittel', 'A base potion for', 'condensed potions.', 'Grundlage für', 'kondensierte Tränke.', 0x0055DD),
    ('speed', 'Elixer of Speed', 'Elixier der Schnelligkeit', 'Movement increased', 'for 5 minutes.', 'Schneller', 'für 5 Minuten.', 0x0022FF),
    ('digging', 'Elixer of Digging', 'Elixier des Grabens', 'Dig and break faster', 'for 5 minutes.', 'Schneller graben und abbauen', 'für 5 Minuten.', 0xDDDD22),
    ('strength', 'Elixer of Strength', 'Elixier der Stärke', 'Damage increased by 3', 'for 5 minutes.', 'Schaden um 3 erhöht', 'für 5 Minuten.', 0x440000),
    ('healing', 'Elixer of Healing', 'Elixier der Heilung', 'Heals 6 hearts.', '(12 damage)', 'Heilt 6 Herzen.', '(12 Schaden)', 0xEE5566),
    ('bounding', 'Elixer of Bounding', 'Elixier der Sprungkraft', 'Higher jumping', 'for 5 minutes.', 'Höher springen', 'für 5 Minuten.', 0x44BB44),
    ('regeneration', 'Elixer of Regeneration', 'Elixier der Regeneration', 'Health regeneration', 'for 1 minute.', 'Regeneration', 'für 1 Minute.', 0xDD22DD),
    ('resistance', 'Elixer of Resistance', 'Elixier der Resistenz', 'Damage resistance', 'for 5 minutes.', 'Schadensresistenz', 'für 5 Minuten.', 0x999999),
    ('fire_warding', 'Elixer of Fire Warding', 'Elixier der Feuerabwehr', 'Fire resistance', 'for 5 minutes.', 'Feuerresistenz', 'für 5 Minuten.', 0xDD9900),
    ('breathing', 'Elixer of Breathing', 'Elixier der Atmung', 'Water breathing', 'for 5 minutes.', 'Unterwasseratmung', 'für 5 Minuten.', 0x0088FF),
    ('invisibility', 'Elixer of Invisibility', 'Elixier der Unsichtbarkeit', 'Invisibility', 'for 5 minutes.', 'Unsichtbarkeit', 'für 5 Minuten.', 'time'),
    ('infravision', 'Elixer of Infravision', 'Elixier der Infrasicht', 'See in the dark', 'for 5 minutes.', 'Im Dunkeln sehen', 'für 5 Minuten.', 0x000044),
    ('protection', 'Ablative Protection', 'Ablativer Schutz', 'Resist fire and', 'damage for 3 minutes.', 'Widersteht Feuer und', 'Schaden für 3 Minuten.', 'time'),
    ('potence', 'Alchemical Potence', 'Alchemische Kraft', 'Strength and dig boost', 'for 3 minutes.', 'Stärke und Eile', 'für 3 Minuten.', 'time'),
    ('celerity', 'Alchemical Celerity', 'Alchemische Behändigkeit', 'Speed and jump boost', 'for 3 minutes.', 'Tempo und Sprungkraft', 'für 3 Minuten.', 'time'),
    ('panacea', 'Grand Panacea', 'Großes Allheilmittel', '30 second regen, heals 6 hearts.', 'Cures any ailment.', '30 s Regeneration, heilt 6 Herzen.', 'Heilt jedes Leiden.', 'time'),
    ('water', 'Vial of Ordinary Water', 'Fläschchen gewöhnliches Wasser', "It's a vial of ", "plain ol' water.", 'Ein Fläschchen mit', 'ganz normalem Wasser.', 0x0055DD),
]
vial, vial_ov = itex('reliquaryPotion'), itex('reliquaryPotionOverlay')
splash, splash_ov = itex('reliquaryPotionSplash'), itex('reliquaryPotionSplashOverlay')
model('potion', [vial])
model('potion_filled', [vial, vial_ov])
model('potion_splash', [splash, splash_ov])
for i, (pid, e_name, d_name, t1, t2, g1, g2, tint) in enumerate(POTIONS):
    item = f'potion_{pid}'
    if tint is None:
        item_def(item, plain(n('item/potion')))
    else:
        t = {'type': f'{NS}:time', 'kind': pid} if tint == 'time' else const(tint)
        item_def(item, plain(n('item/potion_splash' if i <= 10 else 'item/potion_filled'), [WHITE, t]))
    name(f'item.{NS}.{item}', e_name, d_name)
    tip(item, [t1, t2], [g1, g2])

# ---------------------------------------------------------------- relics
simple('grip_assembly', 'gunPart0', 'Grip Assembly', 'Griffstück', ["It's one of three gun parts."], ['Eines von drei Pistolenteilen.'])
simple('barrel_assembly', 'gunPart1', 'Barrel Assembly', 'Laufstück', ["It's one of three gun parts."], ['Eines von drei Pistolenteilen.'])
simple('hammer_assembly', 'gunPart2', 'Hammer Assembly', 'Hahnstück', ["It's one of three gun parts."], ['Eines von drei Pistolenteilen.'])
simple('mercy_cross', 'mercyCross', 'Cross of Mercy', 'Kreuz der Gnade', ["'Damn, that thing's heavy!'", 'That, my friend, is because', "it's so full of mercy."],
       ['„Verdammt, ist das Ding schwer!“', 'Das, mein Freund, liegt daran,', 'dass es so voller Gnade ist.'], handheld=True)
simple('holy_hand_grenade', 'holyHandGrenade', 'Holy Hand Grenade', 'Heilige Handgranate', ['Kills mobs, not blocks', 'or whoever threw it.'],
       ['Tötet Kreaturen, keine Blöcke', 'und nicht den Werfer.'])
coin, coin_ov = itex('fortuneCoin'), itex('fortuneCoinOverlay')
item_def('fortune_coin', flag(plain(model('fortune_coin_active', [coin, coin_ov])), plain(model('fortune_coin', [coin]))))
name(f'item.{NS}.fortune_coin', 'Coin of Fortune', 'Münze des Glücks')
tip('fortune_coin', ['Draws in nearby items and XP.', 'Right click to toggle.'], ['Zieht Gegenstände und Erfahrung heran.', 'Rechtsklick schaltet um.'])
simple('glowing_water', 'glowWater', 'Glowing Water', 'Leuchtendes Wasser', ['Destroys undead. Glowifies stuff.', 'Used in many recipes.'],
       ['Vernichtet Untote. Bringt Dinge zum Leuchten.', 'In vielen Rezepten gebraucht.'])
simple('glowing_bread', 'glowBread', 'Glowing Bread', 'Leuchtendes Brot', ['Glowy and delicious.'], ['Leuchtend und lecker.'])
simple('sojourner_staff', 'sojournerStaff', "Sojourner's Staff", 'Stab des Reisenden', ['Consumes torches, can place them', 'long distance, but costs extra.'],
       ['Verbraucht Fackeln, setzt sie auch', 'weit weg, das kostet aber mehr.'], handheld=True)
simple('alkahestry_tome', 'alkahestTome', 'Tome of Alkahest', 'Foliant der Alkahestrie', ['It says: perform basic, intermediate or', 'advanced Alkahestry, whatever that is.'],
       ['Da steht: einfache, mittlere oder höhere', 'Alkahestrie betreiben, was immer das ist.'])
simple('midas_touchstone', 'midasTouchstone', 'Touchstone of Midas', 'Prüfstein des Midas', ['Repairs items made of gold;', 'consumes glowstone dust.'],
       ['Repariert Gegenstände aus Gold;', 'verbraucht Glowstonestaub.'])
simple('alkahest', 'alkahest', 'Alkahest', 'Alkahest', ['The universal solvent; this will destroy', 'anything crafted with it, but to what end?'],
       ['Das universelle Lösungsmittel; es zerstört', 'alles, womit es verarbeitet wird, aber wozu?'])
cha, cha_ov = itex('emperorChalice'), itex('emperorChaliceOverlay')
item_def('emperor_chalice', flag(plain(model('emperor_chalice_filled', [cha, cha_ov])), plain(model('emperor_chalice', [cha]))))
name(f'item.{NS}.emperor_chalice', "Emperor's Chalice", 'Kelch des Kaisers')
tip('emperor_chalice', ['An infinite water bucket', 'unless you drink from it.', 'Trades life for hunger...'],
    ['Ein unendlicher Wassereimer,', 'solange man nicht daraus trinkt.', 'Tauscht Leben gegen Hunger …'])
for t in range(4):
    itex(f'salamanderEyeOverlay{t}')
    itex(f'wraithEyeOverlay{t}')
simple('salamander_eye', 'salamanderEye', "Salamander's Eye", 'Auge des Salamanders',
       ['Dispels blaze fireballs and reflects', 'ghast fireballs while held.', 'Puts out fires on and around you.'],
       ['Löscht Lohenfeuerbälle und wehrt', 'Ghastfeuerbälle ab, wenn gehalten.', 'Löscht Feuer an und um dich.'])
simple('ice_rod', 'iceRod', 'Ice Magus Rod', 'Eismagierstab', ['Consumes snowballs in inventory.', 'Makes snowballs a bit colder...'],
       ['Verbraucht Schneebälle im Inventar.', 'Macht Schneebälle etwas kälter …'], handheld=True)
simple('magicbane', 'reliquaryMagicbane', 'Magicbane', 'Magiebann', ['A rustproof, ensorcelled artifact.', 'Unfortunately not unbreakable.'],
       ['Ein rostfreies, verzaubertes Artefakt.', 'Leider nicht unzerbrechlich.'], handheld=True)
simple('witherless_rose', 'witherlessRose', 'Witherless Rose', 'Unverdorrbare Rose', ['With the Nether Stars enchanting this rose', 'the holder is protected from Withering.'],
       ['Die Netherstern-Verzauberung dieser Rose', 'schützt ihren Träger vor dem Verdorren.'])
cl, cl_ov = itex('distortionCloak'), itex('distortionCloakOverlay')
item_def('distortion_cloak', plain(model('distortion_cloak', [cl, cl_ov]), [const(0x333333), {'type': f'{NS}:time', 'kind': 'cloak'}]))
name(f'item.{NS}.distortion_cloak', 'Cloak of Distortion', 'Umhang der Verzerrung')
tip('distortion_cloak', ['Right click: invisibility, sneak to teleport.', 'Consumes ender pearls for its charge.'],
    ['Rechtsklick: unsichtbar, schleichend teleportieren.', 'Verbraucht Enderperlen als Ladung.'])
name(f'item.{NS}.distortion_cloak.tooltip.remaining', 'Currently %sm%ss remaining.', 'Noch %s min %s s übrig.')
simple('empty_void_tear', 'emptyVoidTear', 'Empty Void Tear', 'Leere Leerenträne', ['An empty void tear; right clicking', 'absorbs the dominant inventory item.'],
       ['Eine leere Leerenträne; Rechtsklick', 'saugt den häufigsten Gegenstand auf.'])
simple('void_tear', 'voidTear', 'Void Tear', 'Leerenträne')
name(f'item.{NS}.void_tear.tooltip.0', 'This Void Tear currently ', 'Diese Leerenträne enthält derzeit')
name(f'item.{NS}.void_tear.tooltip.holds', 'holds %sx %s', '%s× %s')
name(f'item.{NS}.void_tear.tooltip.empty', 'holds nothing.', 'nichts.')
simple('wraith_eye', 'wraithEye', "Wraith's Eye", 'Auge des Gespensts', ['Right clicking a Wraith Node binds to it', 'allowing you to return to it at will.'],
       ['Rechtsklick auf einen Gespensterknoten bindet', 'das Auge, damit du jederzeit zurückkehrst.'])
name(f'item.{NS}.wraith_eye.tooltip.nowhere', 'Currently bound to nowhere.', 'Derzeit an nichts gebunden.')
name(f'item.{NS}.wraith_eye.tooltip.bound', 'Currently bound to X: %s Y: %s Z: %s', 'Derzeit gebunden an X: %s Y: %s Z: %s')
simple('void_satchel', 'voidSatchel', 'Void Satchel', 'Leerentasche')
name(f'item.{NS}.void_satchel.tooltip.0', 'This Void Satchel currently ', 'Diese Leerentasche enthält derzeit')
name(f'item.{NS}.void_satchel.tooltip.capacity', 'It has a capacity of %s', 'Sie fasst %s')
simple('destruction_catalyst', 'destructionCatalyst', 'Destruction Catalyst', 'Zerstörungskatalysator',
       ['Consumes 3 Gunpowder on right click, no less.', 'Blows up a 3x3x3 Cube behind target cube.', 'Destroys mundane vanilla blocks only.'],
       ['Verbraucht pro Rechtsklick 3 Schwarzpulver.', 'Sprengt einen 3×3×3-Würfel hinter dem Block.', 'Zerstört nur gewöhnliche Vanilla-Blöcke.'])
name(f'itemGroup.{NS}', "Xeno's Reliquary", "Xeno's Reliquary")

# ---------------------------------------------------------------- blocks
BLOCKS = 'mods/xreliquary/textures/blocks/'


def btex(t):
    dst = snake(t)
    assets.append((BLOCKS + t + '.png', f'assets/{NS}/textures/block/{dst}.png'))
    return n('block/' + dst)


def block_model(m, obj):
    write(f'assets/{NS}/models/block/{m}.json', obj)
    return n('block/' + m)


idle = block_model('altar_idle', {'parent': 'minecraft:block/cube_all', 'textures': {'all': btex('altarIdle')}})
active = block_model('altar_active', {'parent': 'minecraft:block/cube_all', 'textures': {'all': btex('altarActive')}})
write(f'assets/{NS}/blockstates/altar.json', {'variants': {'lit=false': {'model': idle}, 'lit=true': {'model': active}}})
item_def('altar', plain(idle))
name(f'block.{NS}.altar', 'Altar of Light', 'Altar des Lichts')
node = block_model('wraith_node', {'parent': 'minecraft:block/block', 'textures': {'all': btex('wraithNode'), 'particle': n('block/wraith_node')},
                                   'elements': [{'from': [2, 2, 2], 'to': [14, 14, 14], 'faces': {f: {'texture': '#all'} for f in
                                                                                                   ('down', 'up', 'north', 'south', 'west', 'east')}}]})
write(f'assets/{NS}/blockstates/wraith_node.json', {'variants': {'': {'model': node}}})
item_def('wraith_node', plain(node))
name(f'block.{NS}.wraith_node', 'Wraith Node', 'Gespensterknoten')
lily = block_model('fertile_lilypad', {'parent': 'minecraft:block/lily_pad', 'textures': {'texture': btex('lilypad'), 'particle': n('block/lilypad')}})
write(f'assets/{NS}/blockstates/fertile_lilypad.json', {'variants': {'': {'model': lily}}})
item_def('fertile_lilypad', plain(model('fertile_lilypad', [itex('lilypad')])))
name(f'block.{NS}.fertile_lilypad', 'Lilypad of Fertility', 'Seerose der Fruchtbarkeit')
for b in ('altar', 'wraith_node', 'fertile_lilypad'):
    write(f'data/{NS}/loot_table/blocks/{b}.json', {'type': 'minecraft:block', 'pools': [
        {'rolls': 1, 'entries': [{'type': 'minecraft:item', 'name': n(b)}], 'condition': {'type': 'minecraft:survives_explosion'}}]})
write('data/minecraft/tags/block/mineable/pickaxe.json', {'replace': False, 'values': [n('altar'), n('wraith_node')]})

# ---------------------------------------------------------------- entities, sounds
for s in ('neutral', 'exorcism', 'blaze', 'ender', 'concussive', 'buster', 'seeker', 'sand', 'storm'):
    assets.append((f'mods/xreliquary/textures/entities/{s}Shot.png', f'assets/{NS}/textures/entity/{s}_shot.png'))
assets.append(('mods/xreliquary/textures/entities/thrownItemsSheet.png', f'assets/{NS}/textures/entity/thrown_items_sheet.png'))
name(f'entity.{NS}.shot', 'Shot', 'Kugel')
name(f'entity.{NS}.thrown', 'Thrown Vial', 'Geworfenes Fläschchen')
sounds = {}
for s in ('load', 'shot'):
    assets.append((f'mods/xreliquary/sound/{s}.ogg', f'assets/{NS}/sounds/{s}.ogg'))
    sounds[s] = {'sounds': [n(s)], 'subtitle': f'subtitles.{NS}.{s}'}
name(f'subtitles.{NS}.load', 'Handgun reloads', 'Pistole wird geladen')
name(f'subtitles.{NS}.shot', 'Handgun fires', 'Pistole feuert')
write(f'assets/{NS}/sounds.json', sounds)
write(f'data/{NS}/tags/item/repairs/gold.json', {'values': [M + 'gold_ingot']})


# ---------------------------------------------------------------- recipes (XRItems, XRBlocks, XRAlkahestry)
def result(item_, count=1):
    r = {'id': n(item_)}
    if count != 1:
        r['count'] = count
    return r


def shaped(file, pattern, key, out, count=1):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shaped', 'category': 'misc', 'pattern': pattern,
                                            'key': {k: n(v) if isinstance(v, str) else v for k, v in key.items()}, 'result': result(out, count)})


def shapeless(file, ingredients, out, count=1):
    write(f'data/{NS}/recipe/{file}.json', {'type': 'minecraft:crafting_shapeless', 'category': 'misc',
                                            'ingredients': [n(i) if isinstance(i, str) else i for i in ingredients], 'result': result(out, count)})


def p(kind):
    return f'potion_{kind}'


GW = 'glowing_water'
shapeless('glowing_bread', [M + 'bread'] * 3 + [GW], 'glowing_bread', 3)
shapeless('gold_nugget_from_casing', ['empty_shot'], M + 'gold_nugget')
shaped('neutral_shot', ['sis', 'ngn', 'ngn'], {'s': M + 'stone', 'i': M + 'iron_ingot', 'n': M + 'gold_nugget', 'g': M + 'gunpowder'}, 'neutral_shot', 4)
shaped('exorcism_shot', ['bbb', 'bhb', 'bbb'], {'b': 'neutral_shot', 'h': GW}, 'exorcism_shot', 8)
shaped('blaze_shot', ['prp', 'npn', 'ngn'], {'p': M + 'blaze_powder', 'r': M + 'blaze_rod', 'n': M + 'gold_nugget', 'g': M + 'gunpowder'}, 'blaze_shot', 4)
shaped('ender_shot', ['bbb', 'beb', 'bbb'], {'b': 'seeker_shot', 'e': M + 'ender_eye'}, 'ender_shot', 8)
shaped('concussive_shot', ['gfg', 'ngn', 'ngn'], {'f': M + 'slime_ball', 'n': M + 'gold_nugget', 'g': M + 'gunpowder'}, 'concussive_shot', 4)
shapeless('buster_shot', ['concussive_shot'] * 4 + [M + 'gunpowder'] * 4 + [M + 'tnt'], 'buster_shot', 4)
shaped('seeker_shot', ['sls', 'nbn', 'ngn'], {'s': M + 'string', 'l': M + 'lapis_lazuli', 'b': M + 'slime_ball', 'n': M + 'gold_nugget',
                                               'g': M + 'gunpowder'}, 'seeker_shot', 4)
shapeless('sand_shot', ['neutral_shot'] * 4 + [M + 'sand'] * 4 + [M + 'sandstone'], 'sand_shot', 4)
shaped('storm_shot', ['bbb', 'bhb', 'bbb'], {'b': 'neutral_shot', 'h': M + 'lapis_lazuli'}, 'storm_shot', 8)
shaped('empty_magazine', ['i i', 'igi', 'sis'], {'s': M + 'stone', 'i': M + 'iron_ingot', 'g': M + 'glass'}, 'empty_magazine', 5)
for sid, *_ in SHOTS[1:]:
    shapeless(f'{sid}_magazine', [f'{sid}_shot'] * 8 + ['empty_magazine'], f'{sid}_magazine')
shaped('fortune_coin', ['ege', 'ghg', 'ege'], {'e': M + 'ender_eye', 'g': M + 'gold_ingot', 'h': GW}, 'fortune_coin')
shaped('mercy_cross', ['wgw', 'glg', 'wgw'], {'w': GW, 'g': M + 'gold_ingot', 'l': M + 'leather'}, 'mercy_cross')
shaped('holy_hand_grenade', ['wgw', 'gtg', 'wgw'], {'w': GW, 'g': M + 'gold_nugget', 't': M + 'tnt'}, 'holy_hand_grenade', 4)
shaped('sojourner_staff', ['cgc', 'gbg', 'wgw'], {'w': GW, 'g': M + 'gold_ingot', 'b': M + 'blaze_rod', 'c': M + 'magma_cream'}, 'sojourner_staff')
shaped('grip_assembly', ['iii', 'imi', 'ici'], {'i': M + 'iron_ingot', 'c': 'empty_magazine', 'm': M + 'magma_cream'}, 'grip_assembly')
shaped('barrel_assembly', ['iii', 'eme', 'iii'], {'i': M + 'iron_ingot', 'e': M + 'ender_pearl', 'm': M + 'magma_cream'}, 'barrel_assembly')
shaped('hammer_assembly', ['iib', 'rmi', 'iii'], {'i': M + 'iron_ingot', 'b': M + 'stone_button', 'r': M + 'blaze_rod', 'm': M + 'magma_cream'},
       'hammer_assembly')
shaped('handgun', ['bim', 'isi', 'igi'], {'i': M + 'iron_ingot', 'b': 'barrel_assembly', 'm': 'hammer_assembly', 'g': 'grip_assembly',
                                          's': M + 'slime_ball'}, 'handgun')
shapeless('alkahestry_tome', [M + 'book', M + 'blaze_rod', M + 'magma_cream', M + 'gold_ingot', M + 'glowstone', M + 'nether_wart',
                              M + 'wither_skeleton_skull', M + 'ghast_tear', M + 'lava_bucket'], 'alkahestry_tome')
shaped('midas_touchstone', ['bbb', 'rtr', 'ggg'], {'b': M + 'gold_block', 'r': M + 'blaze_rod', 'g': M + 'gold_ingot', 't': M + 'ghast_tear'},
       'midas_touchstone')
shaped('emperor_chalice', ['gtg', 'ege', 'tgt'], {'t': M + 'ghast_tear', 'e': M + 'emerald', 'g': M + 'gold_ingot'}, 'emperor_chalice')
shapeless('alkahest', ['alkahestry_tome', M + 'nether_star', M + 'nether_star', M + 'dragon_egg'], 'alkahest')
shaped('salamander_eye', ['bcb', 'tet', 'bcb'], {'b': M + 'blaze_rod', 'c': M + 'magma_cream', 't': M + 'ghast_tear', 'e': M + 'ender_eye'},
       'salamander_eye')
shaped('wraith_eye', ['eee', 'bsb', 'eee'], {'e': 'empty_void_tear', 's': 'salamander_eye', 'b': M + 'emerald_block'}, 'wraith_eye')
shaped('ice_rod', ['dtd', 'tit', 'tit'], {'d': M + 'diamond', 't': M + 'ghast_tear', 'i': M + 'iron_ingot'}, 'ice_rod')
shaped('magicbane', ['eee', 'ege', 'iee'], {'e': M + 'ender_eye', 'g': M + 'gold_ingot', 'i': M + 'iron_ingot'}, 'magicbane')
shaped('witherless_rose', ['hnh', 'nrn', 'hnh'], {'h': GW, 'n': M + 'nether_star', 'r': M + 'poppy'}, 'witherless_rose')
shaped('distortion_cloak', ['eee', 'ewe', 'eee'], {'e': M + 'ender_eye', 'w': M + 'blue_wool'}, 'distortion_cloak')
shaped('empty_void_tear', ['et', 'te'], {'e': M + 'ender_eye', 't': M + 'ghast_tear'}, 'empty_void_tear')
write(f'data/{NS}/recipe/void_satchel.json', {'type': n('void_satchel')})
shaped('destruction_catalyst', ['rrr', 'rtr', 'rrf'], {'f': M + 'flint', 't': 'midas_touchstone', 'r': M + 'blaze_rod'}, 'destruction_catalyst')
shaped('empty_vial', ['g g', 'g g', ' g '], {'g': M + 'glass_pane'}, p('empty_vial'), 5)
shapeless('solvent', [M + 'nether_wart', M + 'redstone', M + 'glowstone_dust', p('water')], p('solvent'))
shapeless('splash_base', [M + 'nether_wart', M + 'gunpowder', M + 'glowstone_dust', p('water')], p('splash_base'))
shapeless('glowing_water', [p('splash_base')] + [M + 'glowstone_dust'] * 3, GW)
for out, a, b, c in (('speed', 'sugar', 'redstone', 'glowstone_dust'), ('digging', 'bone', 'redstone', 'glowstone_dust'),
                     ('strength', 'blaze_powder', 'redstone', 'glowstone_dust'), ('healing', 'glistering_melon_slice', 'glowstone_dust', 'glowstone_dust'),
                     ('bounding', 'feather', 'redstone', 'glowstone_dust'), ('regeneration', 'ghast_tear', 'redstone', 'redstone'),
                     ('resistance', 'leather', 'redstone', 'redstone'), ('fire_warding', 'magma_cream', 'redstone', 'redstone'),
                     ('breathing', 'ink_sac', 'redstone', 'redstone'), ('infravision', 'golden_carrot', 'redstone', 'redstone')):
    shapeless(out, [p('solvent'), M + a, M + b, M + c], p(out))
shapeless('invisibility', [p('infravision'), M + 'fermented_spider_eye', M + 'redstone', M + 'redstone'], p('invisibility'))
for out, a, b, extra in (('protection', 'fire_warding', 'resistance', M + 'glowstone_dust'), ('potence', 'strength', 'bounding', M + 'glowstone_dust'),
                         ('celerity', 'digging', 'speed', M + 'glowstone_dust'), ('panacea', 'healing', 'regeneration', M + 'milk_bucket')):
    shapeless(out, [p(a)] * 4 + [extra] + [p(b)] * 4, p(out), 8)
for out, a, b, c in (('aphrodite', 'cocoa_beans', 'red_dye', 'redstone'), ('poison', 'spider_eye', 'fermented_spider_eye', 'redstone'),
                     ('acid', 'glistering_melon_slice', 'fermented_spider_eye', 'glowstone_dust'),
                     ('confusion', 'golden_carrot', 'fermented_spider_eye', 'redstone'), ('slowing', 'sugar', 'fermented_spider_eye', 'glowstone_dust'),
                     ('weakness', 'blaze_powder', 'fermented_spider_eye', 'glowstone_dust'), ('wither', 'wither_skeleton_skull', 'glowstone_dust', 'glowstone_dust'),
                     ('blinding', 'golden_carrot', 'fermented_spider_eye', 'golden_carrot'), ('fertilizer', 'bone_meal', 'bone_meal', 'bone_meal')):
    shapeless(out, [p('splash_base'), M + a, M + b, M + c], p(out))
shapeless('ruination', [p('slowing'), p('weakness'), p('poison'), M + 'glowstone_dust'], p('ruination'), 3)
shaped('altar', ['olo', 'lel', 'olo'], {'o': M + 'obsidian', 'l': M + 'redstone_lamp', 'e': M + 'emerald'}, 'altar')
shaped('fertile_lilypad', ['www', 'wlw', 'www'], {'w': p('fertilizer'), 'l': M + 'lily_pad'}, 'fertile_lilypad')
shaped('wraith_node', ['vv', 'vv'], {'v': 'empty_void_tear'}, 'wraith_node')

# XRAlkahestry: tome + item + cost reagents -> yield + 1 of the item; the tome stays in the grid
REAGENT = {'r': M + 'redstone', 'l': M + 'lapis_lazuli', 'b': M + 'lapis_block'}
ALKAHESTRY = [('dirt', 16, 'r', 1), ('cobblestone', 16, 'r', 1), ('sand', 16, 'r', 1), ('gravel', 8, 'r', 1), ('oak_log', 2, 'r', 1),
              ('spruce_log', 2, 'r', 1), ('birch_log', 2, 'r', 1), ('jungle_log', 2, 'r', 1), ('sandstone', 4, 'r', 1), ('clay', 2, 'r', 1),
              ('obsidian', 2, 'r', 1), ('netherrack', 16, 'r', 1), ('soul_sand', 4, 'r', 1), ('nether_bricks', 16, 'r', 1), ('end_stone', 16, 'r', 1),
              ('charcoal', 2, 'r', 1), ('diamond', 1, 'b', 4), ('gold_ingot', 1, 'b', 1), ('iron_ingot', 1, 'l', 4), ('gunpowder', 1, 'r', 4),
              ('flint', 8, 'r', 1), ('glowstone_dust', 1, 'r', 1), ('lapis_lazuli', 1, 'r', 4), ('emerald', 1, 'b', 1)]
for item_, yield_, reagent, cost in ALKAHESTRY:
    shapeless(f'alkahestry/{item_}', ['alkahestry_tome', M + item_] + [REAGENT[reagent]] * cost, M + item_, yield_ + 1)
shaped('alkahestry/dragon_egg', ['ddd', 'dtd', 'ddd'], {'d': M + 'diamond_block', 't': 'alkahestry_tome'}, M + 'dragon_egg')
shaped('alkahestry/nether_star', ['tds', 'www', 'hhh'], {'d': M + 'diamond', 'w': M + 'wither_skeleton_skull', 't': 'alkahestry_tome',
                                                         's': M + 'nether_star', 'h': GW}, M + 'nether_star', 2)
# the reverse recipes: alkahest + an item -> alkahest (AlkahestHandler pays for the whole stack, see XREvents)
REVERSE = ['dirt', 'cobblestone', 'sand', 'gravel', ['oak_log', 'spruce_log', 'birch_log', 'jungle_log'], 'sandstone', 'clay', 'obsidian',
           'netherrack', 'soul_sand', 'nether_bricks', 'end_stone', 'white_wool', 'pumpkin', 'melon', 'oak_sapling', 'charcoal', 'diamond',
           'gold_ingot', 'iron_ingot', 'gunpowder', 'flint', 'lapis_lazuli', 'glowstone_dust', 'nether_wart', 'pumpkin_seeds', 'melon_seeds',
           'carrot', 'potato', 'wheat', 'wheat_seeds', 'sugar_cane', 'sugar', 'blaze_rod', 'blaze_powder', 'slime_ball', 'string', 'spider_eye',
           'magma_cream', 'rotten_flesh', 'porkchop', 'cooked_porkchop', 'beef', 'cooked_beef', 'cod', 'cooked_cod', 'chicken', 'cooked_chicken',
           'bone', 'egg', 'feather', 'leather']
for r in REVERSE:
    ing = [M + x for x in r] if isinstance(r, list) else M + r
    shapeless(f'alkahest/{"log" if isinstance(r, list) else r}', ['alkahest', ing], 'alkahest')

# JEI info pages
name(f'jei.{NS}.alkahest', 'Alkahest dissolves the whole stack of the item crafted with it and pays out 64 points per redstone dust, 256 per '
     'lapis lazuli and 2304 per lapis block. Points per item: dirt, cobblestone, sand, netherrack, nether bricks, end stone, string, seeds, sugar '
     'cane, sugar 1; saplings, nether wart, pumpkin and melon seeds, wheat 2; gravel, sandstone, soul sand, wool, spider eye, flint, carrot, '
     'potato, rotten flesh, egg 4; logs, clay, obsidian, melon, pumpkin, coal, blaze powder, slime ball, raw and cooked meat and fish, bone, '
     'feather, leather 8; blaze rod, magma cream 16; gunpowder 128; iron 512; gold, emerald 1152; diamond 4608.',
     'Alkahest löst den ganzen Stapel des Gegenstands auf, mit dem es verarbeitet wird, und zahlt 64 Punkte als Redstone-Staub, 256 als '
     'Lapislazuli und 2304 als Lapislazuliblock aus. Punkte je Gegenstand: Erde, Bruchstein, Sand, Netherrack, Netherziegel, Endstein, Faden, '
     'Weizenkörner, Zuckerrohr, Zucker 1; Setzlinge, Netherwarze, Kürbis- und Melonenkerne, Weizen 2; Kies, Sandstein, Seelensand, Wolle, '
     'Spinnenauge, Feuerstein, Karotte, Kartoffel, verrottetes Fleisch, Ei 4; Stämme, Ton, Obsidian, Melone, Kürbis, Kohle, Lohenstaub, Schleimball, '
     'rohes und gebratenes Fleisch und Fisch, Knochen, Feder, Leder 8; Lohenrute, Magmacreme 16; Schwarzpulver 128; Eisen 512; Gold, Smaragd 1152; '
     'Diamant 4608.')
name(f'jei.{NS}.alkahestry_tome', 'The Tome of Alkahest stays in the crafting grid: with redstone, lapis lazuli or lapis blocks it multiplies '
     'common materials.', 'Der Foliant der Alkahestrie bleibt im Werkbankraster: mit Redstone, Lapislazuli oder Lapislazuliblöcken vervielfacht er '
     'gewöhnliche Materialien.')
name(f'jei.{NS}.glowing_water', 'Every Glowing Water used in a recipe gives back an empty vial.',
     'Jedes in einem Rezept verbrauchte Leuchtende Wasser gibt ein leeres Fläschchen zurück.')
name(f'jei.{NS}.void_satchel', 'Leather around string and a filled Void Tear make a Void Satchel holding up to 64 of the tear\'s item (the rest '
     'spills out). Three Empty Void Tears with a satchel raise its capacity by 192.',
     'Leder um Faden und eine gefüllte Leerenträne ergeben eine Leerentasche mit bis zu 64 des Gegenstands der Träne (der Rest fällt heraus). '
     'Drei leere Leerentränen mit einer Tasche erhöhen ihr Fassungsvermögen um 192.')

# ---------------------------------------------------------------- language files, original-assets.txt
for lang, d in (('en_us', en), ('de_de', de)):
    write(f'assets/{NS}/lang/{lang}.json', dict(sorted(d.items())))
with open(ROOT + '/original-assets.txt', 'w') as f:
    f.write('# path in xreliquary-v1.0.6d.zip -> path in the ported jar (generated by tools/gen.py)\n')
    seen = set()
    for src, dst in assets:
        if dst not in seen:
            seen.add(dst)
            f.write(f'{src} -> {dst}\n')
