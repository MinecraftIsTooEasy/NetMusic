package com.github.tartaricacid.netmusic.block;

import com.github.tartaricacid.netmusic.creativetab.NetMusicCreativeTab;
import com.github.tartaricacid.netmusic.client.renderer.RenderTypes;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.packet.OpenMenuPacket;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.util.ServerWindowIdHelper;
import net.minecraft.BlockConstants;
import net.minecraft.BlockDirectionalWithTileEntity;
import net.minecraft.Container;
import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.EnumDirection;
import net.minecraft.EnumFace;
import net.minecraft.IBlockAccess;
import net.minecraft.ItemStack;
import net.minecraft.Material;
import net.minecraft.ServerPlayer;
import net.minecraft.StringHelper;
import net.minecraft.TileEntity;
import net.minecraft.World;
import net.xiaoyu233.fml.reload.utils.IdUtil;

public class BlockBigMegaphone extends BlockDirectionalWithTileEntity {
    public BlockBigMegaphone() {
        this(IdUtil.getNextBlockID());
    }

    public BlockBigMegaphone(int id) {
        super(id, Material.wood, new BlockConstants());
        this.setHardness(0.5F);
        this.setStepSound(soundWoodFootstep);
        this.setCreativeTab(NetMusicCreativeTab.TAB);
        this.setBlockBoundsForAllThreads(0.125, 0.0, 0.125, 0.875, 0.75, 0.875);
    }

    @Override
    public TileEntity createNewTileEntity(World world) {
        return new TileEntityBigMegaphone();
    }

    @Override
    public EnumDirection getDirectionFacing(int metadata) {
        return this.getDirectionFacingStandard4(metadata & 3);
    }

    @Override
    public int getMetadataForDirectionFacing(int metadata, EnumDirection direction) {
        if (direction.isSouth()) {
            return 0;
        }
        if (direction.isWest()) {
            return 1;
        }
        if (direction.isNorth()) {
            return 2;
        }
        if (direction.isEast()) {
            return 3;
        }
        return metadata & 3;
    }

    @Override
    public int getMetadataForPlacement(World world, int x, int y, int z, ItemStack itemStack, Entity entity, EnumFace face, float offsetX, float offsetY, float offsetZ) {
        return super.getMetadataForPlacement(world, x, y, z, itemStack, entity, face, offsetX, offsetY, offsetZ) & 3;
    }

    @Override
    public boolean isStandardFormCube(boolean[] is_standard_form_cube, int metadata) {
        return false;
    }

    @Override
    public int getRenderType() {
        return RenderTypes.bigMegaphoneRenderType;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, EnumFace face, float offsetX, float offsetY, float offsetZ) {
        if (world.isRemote) {
            return true;
        }
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        if (!(tile instanceof TileEntityBigMegaphone)) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            int windowId = ServerWindowIdHelper.nextWindowId(serverPlayer);
            Container menu = serverPlayer.inventoryContainer;
            serverPlayer.openContainer = menu;
            serverPlayer.openContainer.windowId = windowId;
            NetworkHandler.sendToClientPlayer(new OpenMenuPacket(OpenMenuPacket.Type.BIG_MEGAPHONE, windowId, x, y, z), serverPlayer);
        }
        return true;
    }

    @Override
    public boolean onNeighborBlockChange(World world, int x, int y, int z, int neighborBlockId) {
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        if (tile instanceof TileEntityBigMegaphone megaphone) {
            megaphone.onRedstoneSignalChanged(world.isBlockIndirectlyGettingPowered(x, y, z));
        }
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, int blockId, int metadata) {
        TileEntity tile = world.getBlockTileEntity(x, y, z);
        if (tile instanceof TileEntityBigMegaphone megaphone) {
            megaphone.onBlockRemoved();
        }
        super.breakBlock(world, x, y, z, blockId, metadata);
    }

    @Override
    public void setBlockBoundsBasedOnStateAndNeighbors(IBlockAccess blockAccess, int x, int y, int z) {
        this.setBlockBoundsForAllThreads(0.125, 0.0, 0.125, 0.875, 0.75, 0.875);
    }

    @Override
    public void setBlockBoundsForItemRender(int itemDamage) {
        this.setBlockBoundsForAllThreads(0.125, 0.0, 0.125, 0.875, 0.75, 0.875);
    }

    @Override
    public String getMetadataNotes() {
        String[] array = new String[4];
        for (int i = 0; i < array.length; ++i) {
            array[i] = i + "=" + this.getDirectionFacing(i).getDescriptor(true);
        }
        return StringHelper.implode(array, ", ", true, false);
    }

    @Override
    public boolean isValidMetadata(int metadata) {
        return metadata >= 0 && metadata < 4;
    }
}
