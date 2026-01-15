package lu.apwbd.storecoords.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.LinkedHashSet;
import java.util.Set;

public final class MultiBlockResolver {

    private MultiBlockResolver() {}

    /**
     * Identifies and resolves all connected block positions based on the type of block present at the specified position.
     * This method handles specific multi-block structures such as doors, double plants, beds, and chests,
     * ensuring related block positions are included in the resulting*/
    public static Set<BlockPos> resolve(Level level, BlockPos pos) {
        Set<BlockPos> out = new LinkedHashSet<>();
        out.add(pos);

        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof DoorBlock && state.hasProperty(DoorBlock.HALF)) {
            DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
            out.add(half == DoubleBlockHalf.LOWER ? pos.above() : pos.below());
            return out;
        }

        if (state.getBlock() instanceof DoublePlantBlock && state.hasProperty(DoublePlantBlock.HALF)) {
            DoubleBlockHalf half = state.getValue(DoublePlantBlock.HALF);
            out.add(half == DoubleBlockHalf.LOWER ? pos.above() : pos.below());
            return out;
        }

        if (state.getBlock() instanceof BedBlock && state.hasProperty(BedBlock.PART)) {
            BedPart part = state.getValue(BedBlock.PART);
            BlockPos other = (part == BedPart.FOOT)
                    ? pos.relative(state.getValue(BedBlock.FACING))
                    : pos.relative(state.getValue(BedBlock.FACING).getOpposite());
            out.add(other);
            return out;
        }

        if (state.getBlock() instanceof ChestBlock && state.hasProperty(ChestBlock.TYPE)) {
            ChestType type = state.getValue(ChestBlock.TYPE);
            if (type != ChestType.SINGLE) {
                BlockPos other = pos.relative(
                        type == ChestType.LEFT
                                ? state.getValue(ChestBlock.FACING).getClockWise()
                                : state.getValue(ChestBlock.FACING).getCounterClockWise()
                );
                out.add(other);
            }
        }

        return out;
    }
}
