package projectzulu;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Project Zulu Complete 1.0.3.8 for NeoForge. Part A (Teil 10) are the creatures: entities, models, renderers,
 * spawns per biome from the pack configuration (config/Project Zulu). Blocks, items, world generation follow in
 * part B (Teil 11).
 */
@Mod(ProjectZulu.MOD_ID)
public final class ProjectZulu {
    public static final String MOD_ID = "projectzulu";

    public ProjectZulu(IEventBus modBus) {
        projectzulu.gen.ZuluWorldGen.register();
        ZuluItems.register(modBus);
        ZuluEntities.register(modBus);
        ZuluBigAnimals.register(modBus);
        ZuluMonsters.register(modBus);
        ZuluHorses.register(modBus);
        ZuluPlants.register(modBus);
        ZuluGear.register(modBus);
        ZuluWorld.register(modBus);
        ZuluPotions.register(modBus);
        ZuluCreativeTabs.register(modBus);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            projectzulu.client.ZuluClient.register(modBus);
        }
    }
}
