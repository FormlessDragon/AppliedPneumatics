package com.wintercogs.appliedpneumatics.common.block;

import ae2.block.AEBaseTileBlock;
import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.gui.APGuiIds;
import com.wintercogs.appliedpneumatics.common.tile.METemperatureInterfaceTile;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Locale;

public class METemperatureInterfaceBlock extends AEBaseTileBlock<METemperatureInterfaceTile> {
    public static final PropertyEnum<TemperatureState> TEMPERATURE_STATE =
        PropertyEnum.create("temperature_state", TemperatureState.class);

    public METemperatureInterfaceBlock() {
        super(Material.IRON);
        setHardness(2.0F);
        setResistance(10.0F);
        setTileEntity(METemperatureInterfaceTile.class);
        setDefaultState(getDefaultState().withProperty(TEMPERATURE_STATE, TemperatureState.ROOM_TEMPERATURE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new net.minecraft.block.properties.IProperty<?>[]{TEMPERATURE_STATE},
            new IUnlistedProperty<?>[]{FORWARD, UP});
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(TEMPERATURE_STATE, TemperatureState.fromMeta(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TEMPERATURE_STATE).ordinal();
    }

    @Override
    protected IBlockState updateBlockStateFromTileEntity(IBlockState currentState, METemperatureInterfaceTile tileEntity) {
        return currentState.withProperty(TEMPERATURE_STATE,
            TemperatureState.fromTemperature(tileEntity.getHeatExchangerLogic(null).getTemperature()));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)) {
            return true;
        }
        if (!world.isRemote) {
            player.openGui(AppliedPneumatics.INSTANCE, APGuiIds.ME_TEMPERATURE_INTERFACE, world,
                pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    public enum TemperatureState implements IStringSerializable {
        ROOM_TEMPERATURE,
        HIGH_TEMPERATURE,
        LOW_TEMPERATURE;

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        private static TemperatureState fromMeta(int meta) {
            TemperatureState[] values = values();
            if (meta < 0 || meta >= values.length) {
                return ROOM_TEMPERATURE;
            }
            return values[meta];
        }

        public static TemperatureState fromTemperature(double temperature) {
            if (temperature > 473.0D) {
                return HIGH_TEMPERATURE;
            }
            if (temperature < 173.0D) {
                return LOW_TEMPERATURE;
            }
            return ROOM_TEMPERATURE;
        }
    }
}

