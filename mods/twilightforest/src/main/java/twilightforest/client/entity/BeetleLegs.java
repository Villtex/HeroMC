package twilightforest.client.entity;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** The leg animation the three beetle models share (ModelTFFireBeetle.setRotationAngles, like ModelSpider). */
final class BeetleLegs {
    private BeetleLegs() {
    }

    static void animate(float par1, float par2, ModelPart leg1, ModelPart leg2, ModelPart leg3, ModelPart leg4, ModelPart leg5, ModelPart leg6) {
        float legZ = 0.28559935F;
        leg1.zRot = -legZ;
        leg2.zRot = legZ;
        leg3.zRot = -legZ * 0.74F;
        leg4.zRot = legZ * 0.74F;
        leg5.zRot = -legZ;
        leg6.zRot = legZ;
        float var10 = Mth.PI / 8.0F;
        leg1.yRot = var10 * 2.0F;
        leg2.yRot = -var10 * 2.0F;
        leg3.yRot = var10;
        leg4.yRot = -var10;
        leg5.yRot = -var10 * 2.0F;
        leg6.yRot = var10 * 2.0F;
        float var11 = -(Mth.cos(par1 * 0.6662F * 2.0F) * 0.4F) * par2;
        float var12 = -(Mth.cos(par1 * 0.6662F * 2.0F + Mth.PI) * 0.4F) * par2;
        float var14 = -(Mth.cos(par1 * 0.6662F * 2.0F + Mth.PI * 3.0F / 2.0F) * 0.4F) * par2;
        float var15 = Math.abs(Mth.sin(par1 * 0.6662F) * 0.4F) * par2;
        float var16 = Math.abs(Mth.sin(par1 * 0.6662F + Mth.PI) * 0.4F) * par2;
        float var18 = Math.abs(Mth.sin(par1 * 0.6662F + Mth.PI * 3.0F / 2.0F) * 0.4F) * par2;
        leg1.yRot += var11;
        leg2.yRot += -var11;
        leg3.yRot += var12;
        leg4.yRot += -var12;
        leg5.yRot += var14;
        leg6.yRot += -var14;
        leg1.zRot += var15;
        leg2.zRot += -var15;
        leg3.zRot += var16;
        leg4.zRot += -var16;
        leg5.zRot += var18;
        leg6.zRot += -var18;
    }
}
