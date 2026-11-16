package harkenscythe.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.AbstractVillager;

/**
 * EntityHSSoul.getFallenSoulName: what a soul was, for the spectral pet it can be raised as. 1.5.2 told the tamed cats
 * apart by their skin (Cat_Black, Cat_Red, Cat_Siamese); cats are their own mob today and give the black cat.
 */
public final class FallenSoul {
    private FallenSoul() {
    }

    public static String nameOf(LivingEntity e) {
        if (e instanceof Chicken) {
            return "Chicken";
        }
        if (e instanceof Cow) {
            return "Cow";
        }
        if (e instanceof Sheep) {
            return "Sheep";
        }
        if (e instanceof Bat) {
            return "Bat";
        }
        if (e instanceof Pig) {
            return "Pig";
        }
        if (e instanceof Wolf) {
            return "Wolf";
        }
        if (e instanceof Cat) {
            return "Cat_Black";
        }
        if (e instanceof Ocelot) {
            return "Ocelot";
        }
        if (e instanceof Squid) {
            return "Squid";
        }
        if (e instanceof AbstractVillager) {
            return "Villager";
        }
        if (e instanceof Creeper) {
            return "Creeper";
        }
        if (e instanceof Zombie) {
            return e instanceof ZombifiedPiglin ? "PigZombie" : "Zombie";
        }
        if (e instanceof AbstractSkeleton) {
            return e instanceof WitherSkeleton ? "Skeleton_Wither" : "Skeleton";
        }
        if (e instanceof Witch) {
            return "Witch";
        }
        if (e instanceof Spider) {
            return e instanceof CaveSpider ? "CaveSpider" : "Spider";
        }
        if (e instanceof Silverfish) {
            return "Silverfish";
        }
        if (e instanceof Enderman) {
            return "Enderman";
        }
        if (e instanceof Blaze) {
            return "Blaze";
        }
        if (e instanceof Ghast) {
            return "Ghast";
        }
        return "null";
    }
}
