"""Shared helpers of the AsgardShield generators (tools/gen.py here and in mods/asgardshield-addons): item models with
the shield colour tint, crafting recipes, tags and the language file. Each generator overwrites resources/assets and
resources/data of its project."""
import json, os, shutil

CORE = 'asgardshield_core'
HS = 'harkenscythe_core'
OVERLAY_TINT = {'type': 'minecraft:constant', 'value': -986896}  # 0xF0F0F0, render pass 1 of ItemAsgardShield


class Gen:
    def __init__(self, root, ns):
        self.root = root
        self.res = os.path.join(root, 'src/main/resources')
        self.ns = ns
        self.recipes = {}
        self.tags = {}
        self.lang = {}
        for d in ('assets', 'data'):
            shutil.rmtree(os.path.join(self.res, d), ignore_errors=True)

    def write(self, path, obj):
        full = os.path.join(self.res, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, 'w') as f:
            json.dump(obj, f, indent=2)
            f.write('\n')

    def n(self, x):
        return f'{self.ns}:{x}'

    def tag(self, t, *values):
        lst = self.tags.setdefault(t, [])
        for v in values:
            if v not in lst:
                lst.append(v)

    # ---------------------------------------------------------------- items
    def shield(self, name, title, color, texture):
        """A shield: base layer tinted with the shield colour (dye or colour API), overlay layer slightly grey.
        Textures live in asgardshield_core (they ship in the core archive)."""
        self.write(f'assets/{self.ns}/models/item/{name}.json', {'parent': 'minecraft:item/handheld', 'textures': {
            'layer0': f'{CORE}:item/{texture}', 'layer1': f'{CORE}:item/{texture}_overlay'}})
        self.write(f'assets/{self.ns}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': self.n(f'item/{name}'), 'tints': [
            {'type': f'{CORE}:shield_color', 'shield': color}, OVERLAY_TINT]}})
        self.lang[f'item.{self.ns}.{name}'] = title
        self.tag(f'{CORE}:shields', self.n(name))
        self.tag('minecraft:cauldron_can_remove_dye', self.n(name))
        # ItemArmor (chest plate) of 1.5.2: armour enchantments at the table, unbreaking on the anvil
        self.tag('minecraft:enchantable/chest_armor', self.n(name))
        self.tag('minecraft:enchantable/durability', self.n(name))
        self.recipes[f'dye_{name}'] = {'type': 'minecraft:crafting_dye', 'category': 'equipment', 'group': 'asgardshield_dyed',
                                       'dye': '#minecraft:dyes', 'target': self.n(name), 'result': {'id': self.n(name)}}

    def sword(self, name, title, texture):
        self.write(f'assets/{self.ns}/models/item/{name}.json', {'parent': 'minecraft:item/handheld', 'textures': {
            'layer0': f'{CORE}:item/{texture}'}})
        self.write(f'assets/{self.ns}/items/{name}.json', {'model': {'type': 'minecraft:model', 'model': self.n(f'item/{name}')}})
        self.lang[f'item.{self.ns}.{name}'] = title
        self.tag(f'{CORE}:giant_swords', self.n(name))
        self.tag('minecraft:enchantable/melee_weapon', self.n(name))
        self.tag('minecraft:enchantable/durability', self.n(name))

    # ---------------------------------------------------------------- recipes
    def shaped(self, name, pattern, key, result, conditions=None):
        assert name not in self.recipes, name
        r = {'type': 'minecraft:crafting_shaped', 'category': 'equipment', 'key': key, 'pattern': pattern,
             'result': {'count': 1, 'id': result}}
        if conditions:
            r = {'neoforge:conditions': conditions, **r}
        self.recipes[name] = r

    def shield_recipe(self, name, material, top=None, conditions=None):
        """XXX / X*X / " X " with leather; the ender and skull shields have an extra item on top (X^X)."""
        key = {'X': material, '*': 'minecraft:leather'}
        first = 'XXX'
        if top:
            key['^'] = top
            first = 'X^X'
        self.shaped(name, [first, 'X*X', ' X '], key, self.n(name), conditions)

    def sword_recipe(self, name, material, handle, middle=None, conditions=None):
        """" XX" / "XXX" / "*X " (the ender and skull swords have an extra item in the middle)."""
        key = {'X': material, '*': handle}
        second = 'XXX'
        if middle:
            key['^'] = middle
            second = 'X^X'
        self.shaped(name, [' XX', second, '*X '], key, self.n(name), conditions)

    def finish(self):
        for name, r in self.recipes.items():
            self.write(f'data/{self.ns}/recipe/{name}.json', r)
        for t, values in self.tags.items():
            ns, path = t.split(':')
            self.write(f'data/{ns}/tags/item/{path}.json', {'values': values})
        self.write(f'assets/{self.ns}/lang/en_us.json', dict(sorted(self.lang.items())))
        print(self.ns, 'recipes', len(self.recipes), 'tags', len(self.tags), 'lang', len(self.lang))
