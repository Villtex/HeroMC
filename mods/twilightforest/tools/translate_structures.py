# Translates Twilight Forest structure pieces of 1.5.2 into the core (twilightforest.gen.structure): World -> OldWorld,
# Block/TFBlocks constants -> old ids, TFTreasure/TFFeature names, spawner names, setBlock(..., 2) -> 5 arguments.
# The result still needs hand work (tile entities, entities, world.rand, other generators); see the skill twilight-forest-263.
import re, sys, os
SRC = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', '..', '.reference', 'twilightforest', 'src', 'twilightforest', 'structures')
DST = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'java', 'twilightforest', 'gen', 'structure')
BLOCKS = dict(fence=85, stoneBrick=98, stoneSingleSlab=44, planks=5, mushroomCapBrown=99, woodSingleSlab=126, stoneDoubleSlab=43,
  torchWood=50, mushroomCapRed=100, tnt=46, fenceIron=101, vine=106, stairsWoodOak=53, pumpkinLantern=91, stairsWoodBirch=135,
  stairsStoneBrick=109, mushroomBrown=39, chest=54, sapling=6, pressurePlatePlanks=72, mushroomRed=40, mobSpawner=52, gravel=13,
  stairsWoodSpruce=134, sand=12, dirt=3, cloth=35, stone=1, mycelium=110, grass=2, glass=20, cobblestone=4, bookShelf=47,
  waterMoving=8, ladder=65, web=30, waterStill=9, tripWireSource=131, tripWire=132, slowSand=88, silverfish=97, lavaStill=11,
  lavaMoving=10, woodDoubleSlab=125, thinGlass=102, stoneButton=77, netherrack=87, glowStone=89, flowerPot=140, dispenser=23,
  deadBush=32, cobblestoneMossy=48, cactus=81, blockLapis=22, blockIron=42, bedrock=7, stairsWoodJungle=136, stairsCobblestone=67,
  wood=17, leaves=18, brick=45, stairsBrick=108, obsidian=49, torchRedstoneActive=76, redstoneWire=55, ice=79, snow=78,
  blockSnow=80, tallGrass=31, plantRed=38, plantYellow=37, pumpkin=86, lever=69, fenceGate=107, doorWood=64, trapdoor=96,
  sandStone=24, stairsSandStone=128, blockGold=41, blockDiamond=57, blockEmerald=133, oreDiamond=56, waterlily=111,
  anvil=145, blockRedstone=152, brewingStand=117, cauldron=118, cobblestoneWall=139, fire=51, furnaceIdle=61, netherStalk=115,
  pistonBase=33, pistonStickyBase=29, redstoneLampIdle=123, redstoneRepeaterIdle=93, workbench=58)
TF = dict(bossSpawner=1351, firefly=1353, hedge=1356, mazestone=1362, nagastone=1364, plant=1365, towerWood=1372, cicada=1352,
  moonworm=1363, log=1358, leaves=1357, sapling=1368, root=1367, fireJet=1354, towerDevice=1370, towerTranslucent=1371,
  towerAntenna=1369, trophy=1373, uncraftingTable=1374, fireflyJar=1355)
FEAT = dict(darkTower='DARK_TOWER', hedgeMaze='HEDGE_MAZE', hill1='HILL1', hill2='HILL2', hill3='HILL3', hydraLair='HYDRA_LAIR',
  labyrinth='LABYRINTH', nagaLair='NAGA_LAIR', questGrove='QUEST_GROVE', tfStronghold='STRONGHOLD', wizardTower='WIZARD_TOWER')

def conv(text):
    text = re.sub(r'^package .*;', 'package twilightforest.gen.structure;', text, flags=re.M)
    text = re.sub(r'^import (net\.minecraft|twilightforest|cpw)[^;]*;\n', '', text, flags=re.M)
    text = text.replace('package twilightforest.gen.structure;\n', 'package twilightforest.gen.structure;\n\n'
        'import hexxit.worldgen152.core.populate.OldWorld;\nimport hexxit.worldgen152.core.populate.OldTile;\nimport hexxit.worldgen152.core.structure.*;\nimport twilightforest.gen.*;\n', 1)
    text = re.sub(r'\bWorld\b', 'OldWorld', text)
    def blk(m):
        n = m.group(1)
        if n not in BLOCKS: return m.group(0) + '/*TODO*/'
        return str(BLOCKS[n])
    text = re.sub(r'\bBlock\.([a-zA-Z]+)\.blockID', blk, text)
    def tfb(m):
        n = m.group(1)
        if n not in TF: return m.group(0) + '/*TODO*/'
        return str(TF[n])
    text = re.sub(r'\bTFBlocks\.([a-zA-Z]+)\.blockID', tfb, text)
    text = re.sub(r'world\.getSeed\(\)', 'world.seed()', text)
    text = re.sub(r'TFTreasure\.([a-z_0-9]+)', lambda m: 'TFTreasure.' + m.group(1).upper(), text)
    text = re.sub(r'TFCreatures\.getSpawnerNameFor\("([^"]+)"\)', r'"TwilightForest.\1"', text)
    text = re.sub(r'TFFeature\.([a-zA-Z0-9]+)\b(?!\()', lambda m: 'TFFeature.' + FEAT.get(m.group(1), m.group(1)), text)
    text = text.replace('TFWorld.SEALEVEL', 'TFTerrain.SEALEVEL')
    text = text.replace('TileEntityMobSpawner', 'OldTile.Spawner')
    # setBlock(..., 2) -> 5 args
    text = re.sub(r'(\.setBlock\((?:[^()]|\([^()]*\))*?), 2\)', r'\1)', text)
    return text

# usage: cd .reference/twilightforest/src/twilightforest/structures && python3 <this> Component*.java (overwrites DST!)
files = sys.argv[1:]
os.makedirs(DST, exist_ok=True)
for f in files:
    t = conv(open(os.path.join(SRC, f)).read())
    open(os.path.join(DST, os.path.basename(f)), 'w').write(t)
    print(f, t.count('TODO'), len(re.findall(r'setBlock\([^;]*, 3\)', t)))
