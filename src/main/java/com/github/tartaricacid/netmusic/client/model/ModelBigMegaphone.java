package com.github.tartaricacid.netmusic.client.model;

import net.minecraft.ModelBase;
import net.minecraft.ModelRenderer;

public final class ModelBigMegaphone {
    private final ModelBase base;
    private final ModelRenderer root;

    public ModelBigMegaphone() {
        this.base = new ModelBase() {};
        this.base.textureWidth = 128;
        this.base.textureHeight = 128;

        this.root = new ModelRenderer(this.base);
        this.root.setRotationPoint(0.0F, 24.0F, 0.0F);
        addBox(this.root, 92, 85, -1.0F, -8.0F, -1.0F, 2, 6, 2, 0.005F);
        addBox(this.root, 0, 47, -2.0F, -2.0F, -2.0F, 4, 2, 4, 0.0F);
        addBox(this.root, 0, 47, -2.0F, -32.0F, -2.0F, 4, 18, 4, 0.0F);

        ModelRenderer bone = child(this.root, 0.0F, -7.0F, -2.0F, 0.0F, 0.0F, 0.0F);
        addHornBoxes(bone, -6.0F, -8.0F, -11.0F, -12.0F, 9.0F, 12.0F, -2.0F);
        addRotatedHornEdges(bone, -4.8457F, -6.3764F, 10.3764F, 8.8457F, -0.2242F, -6.0803F, -5.0F);

        ModelRenderer bone2 = child(this.root, 0.0F, -23.12F, 0.0F, 0.0F, 1.5708F, 0.0F);
        addHornBoxes(bone2, -1.88F, -3.88F, -13.0F, -14.0F, 7.0F, 10.0F, -4.0F);
        addRotatedHornEdges(bone2, -6.8457F, -8.3764F, 8.3764F, 6.8457F, 3.8958F, -1.9603F, -0.88F);
    }

    public void render(float scale) {
        this.root.render(scale);
    }

    private void addHornBoxes(ModelRenderer parent, float centerY, float bodyY, float rearFrameZ, float rearTubeZ,
                              float frontTubeZ, float frontFrameZ, float bodyZ) {
        addBox(parent, 56, 77, -1.0F, centerY, frontTubeZ, 2, 2, 7, 0.0F);
        addBox(parent, 28, 88, 3.0F, bodyY, frontFrameZ, 3, 6, 3, 0.0F);
        addBox(parent, 56, 86, -6.0F, bodyY, frontFrameZ, 3, 6, 3, 0.0F);
        addBox(parent, 28, 80, -1.0F, centerY + 6.0F, bodyZ + 4.4F, 2, 2, 6, 0.0F);
        addBox(parent, 40, 93, -1.0F, centerY + 4.0F, bodyZ + 10.4F, 2, 4, 2, 0.0F);
        addBox(parent, 0, 0, -4.0F, bodyY - 1.0F, bodyZ, 8, 8, 8, 0.0F);
        addBox(parent, 60, 30, -3.0F, bodyY, bodyZ + 7.4F, 6, 6, 4, 0.0F);
        addBox(parent, 46, 53, -6.0F, bodyY - 3.0F, frontFrameZ, 12, 3, 3, 0.0F);
        addBox(parent, 16, 53, -6.0F, bodyY + 6.0F, frontFrameZ, 12, 3, 3, 0.0F);
        addBox(parent, 16, 47, -6.0F, bodyY + 6.0F, rearFrameZ, 12, 3, 3, 0.0F);
        addBox(parent, 46, 47, -6.0F, bodyY - 3.0F, rearFrameZ, 12, 3, 3, 0.0F);
        addBox(parent, 56, 59, -3.0F, bodyY, bodyZ - 3.4F, 6, 6, 4, 0.0F);
        addBox(parent, 92, 65, -1.0F, centerY + 4.0F, bodyZ - 4.4F, 2, 4, 2, 0.0F);
        addBox(parent, 80, 24, -1.0F, centerY + 6.0F, bodyZ - 2.4F, 2, 2, 6, 0.0F);
        addBox(parent, 44, 80, -6.0F, bodyY, rearFrameZ, 3, 6, 3, 0.0F);
        addBox(parent, 0, 86, 3.0F, bodyY, rearFrameZ, 3, 6, 3, 0.0F);
        addBox(parent, 38, 71, -1.0F, centerY, rearTubeZ, 2, 2, 7, 0.0F);
    }

    private void addRotatedHornEdges(ModelRenderer parent, float rearBottomZ, float rearTopZ, float frontTopZ,
                                     float frontBottomZ, float bottomY, float topY, float sideY) {
        addBox(child(parent, 0.0F, bottomY, rearBottomZ, 0.3927F, 0.0F, 0.0F), 16, 71, -3.0F, -3.0F, -3.0F, 6, 2, 5, 0.0F);
        addBox(child(parent, -1.0803F, sideY, rearTopZ, 0.0F, 0.3927F, 0.0F), 74, 77, -3.0F, -3.0F, -3.0F, 2, 6, 5, 0.0F);
        addBox(child(parent, 1.0803F, sideY, rearTopZ, 0.0F, -0.3927F, 0.0F), 0, 75, 1.0F, -3.0F, -3.0F, 2, 6, 5, 0.0F);
        addBox(child(parent, 0.0F, topY, rearTopZ, -0.3927F, 0.0F, 0.0F), 60, 40, -3.0F, -3.0F, -3.0F, 6, 2, 5, 0.0F);
        addBox(child(parent, 0.0F, topY, frontTopZ, 0.3927F, 0.0F, 0.0F), 76, 47, -3.0F, -3.0F, -2.0F, 6, 2, 5, 0.0F);
        addBox(child(parent, 1.0803F, sideY, frontTopZ, 0.0F, 0.3927F, 0.0F), 14, 78, 1.0F, -3.0F, -2.0F, 2, 6, 5, 0.0F);
        addBox(child(parent, -1.0803F, sideY, frontTopZ, 0.0F, -0.3927F, 0.0F), 78, 61, -3.0F, -3.0F, -2.0F, 2, 6, 5, 0.0F);
        addBox(child(parent, 0.0F, bottomY, frontBottomZ, -0.3927F, 0.0F, 0.0F), 76, 54, -3.0F, -3.0F, -2.0F, 6, 2, 5, 0.0F);
    }

    private ModelRenderer child(ModelRenderer parent, float x, float y, float z, float rotX, float rotY, float rotZ) {
        ModelRenderer child = new ModelRenderer(this.base);
        child.setRotationPoint(x, y, z);
        child.rotateAngleX = rotX;
        child.rotateAngleY = rotY;
        child.rotateAngleZ = rotZ;
        parent.addChild(child);
        return child;
    }

    private static void addBox(ModelRenderer renderer, int textureX, int textureY, float x, float y, float z,
                               int width, int height, int depth, float inflate) {
        renderer.setTextureOffset(textureX, textureY);
        renderer.addBox(x, y, z, width, height, depth, inflate);
    }
}
