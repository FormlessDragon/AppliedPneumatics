package com.wintercogs.appliedpneumatics.datagen;

import com.wintercogs.appliedpneumatics.AppliedPneumatics;
import com.wintercogs.appliedpneumatics.common.init.APBlockStates;
import com.wintercogs.appliedpneumatics.common.init.APBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ModBlockStateProvider extends BlockStateProvider
{

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper)
    {
        super(output, AppliedPneumatics.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels()
    {
        blockWithItem(APBlocks.ME_PRESSURE_INTERFACE_BLOCK);
        cubeAllPerState(APBlocks.ME_TEMPERATURE_INTERFACE, APBlockStates.TEMP_STATE);
        blockWithItem(APBlocks.ME_AMADRON_PROCESS_STATION);
        genCubeAllWithFormedToggle(APBlocks.ME_PRESSURE_CHAMBER_VALVE);
        genWallLike13States(APBlocks.ME_PRESSURE_CHAMBER_WALL);
        blockWithItem(APBlocks.ME_AMADRON_EXTENDED_PROCESS_STATION);
        // APBlocks.ME_PRESSURE_CHAMBER_GLASS手写
        // APBlocks.ME_PRESSURE_CHAMBER_VIBRANT_GLASS手写
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock)
    {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    /**
     * 为某个方块的枚举属性生成：
     * - blockstates/<id>.json（每个取值一个 variant）
     * - models/block/<id>/<state>.json（cube_all）
     * - models/item/<id>.json（指向默认状态对应的 block 模型）
     *
     * 贴图路径：textures/block/<id>/<state>.png
     */
    public <E extends Enum<E> & StringRepresentable>
    void cubeAllPerState(DeferredBlock<? extends Block> defBlock, EnumProperty<E> prop) {
        Block block = defBlock.get();

        var def = block.getStateDefinition();
        if (!def.getProperties().contains(prop)) {
            throw new IllegalArgumentException("Block " + block + " does not contain property " + prop.getName());
        }

        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        VariantBlockStateBuilder builder = getVariantBuilder(block);

        E defaultVal = block.defaultBlockState().getValue(prop);
        ModelFile defaultModel = null;

        for (E value : prop.getPossibleValues()) {
            String state = value.getSerializedName();
            String name  = id.getPath() + "/" + state; // 模型 ID（注意：不带 "block/" 前缀）
            // 方块模型：models/block/<name>.json，纹理：textures/block/<name>.png
            ModelFile model = models().cubeAll(name, modLoc("block/" + name));

            builder.partialState().with(prop, value).addModels(new ConfiguredModel(model));

            if (value == defaultVal) defaultModel = model;         // 记住默认状态的模型
        }

        // 物品模型指向默认方块模型
        simpleBlockItem(block, defaultModel);
    }


    /**
     * 为具有 13 种 wall-like 状态（common/center/xedge/yedge/zedge/8 corners）的方块生成：
     * - 每个变体对应的 6 面模型（纹理从 block/<注册名>/ 后缀取）
     * - 物品模型（优先使用 "common"，没有就用 "center"，再不行用第一个状态）
     *
     * @param block  目标方块
     */
    public void genWallLike13States(DeferredBlock<?> block)
    {
        final ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block.get());
        final String baseName = id.getPath();                  // <注册名>
        final String texBase = "block/" + baseName + "/";      // 纹理前缀
        final EnumProperty<APBlockStates.WallState> prop = APBlockStates.WALL_STATE;

        final ResourceLocation parent = ResourceLocation.tryBuild("minecraft", "block/cube");

        VariantBlockStateBuilder variantBuilder = getVariantBuilder(block.get());

        // 生成每个状态对应的模型
        for (APBlockStates.WallState v : prop.getPossibleValues()) {
            final String stateName = v.getSerializedName();
            ModelFile mf = wallLikeModel(block.get(), stateName, texBase, parent);
            variantBuilder.partialState().with(prop, v).modelForState().modelFile(mf).addModel();
        }

        // 物品模型：优先 common，否则 center，否则第一个
        String forItem = pickItemStateName(prop);
        simpleBlockItem(block.get(), models().getExistingFile(modLoc("block/" + baseName + "_" + forItem)));
    }

    /**
     * 若目标方块含有名为 "formed" 的 BooleanProperty：
     *  - formed=true  使用 block/<registry_path>/formed 作为 cube_all 贴图
     *  - formed=false 使用 block/<registry_path>/common 作为 cube_all 贴图
     * 若不含该属性：
     *  - 退化为使用 block/<registry_path>/common 的简单 cube_all
     *
     * 方块物品模型一律用 common 版本。
     */
    public void genCubeAllWithFormedToggle(DeferredBlock<?> block)
    {
        final ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block.get());
        final String baseName = id.getPath();                 // <注册名>
        final String texBase  = "block/" + baseName + "/";    // 纹理前缀，如 block/<registry_path>/

        // 尝试在 Block 的 StateDefinition 里查找名为 "formed" 的布尔属性
        @SuppressWarnings("unchecked")
        final @Nullable BooleanProperty formedProp = (BooleanProperty)
                block.get().getStateDefinition().getProperties().stream()
                        .filter(p -> p.getName().equals("formed"))
                        .filter(BooleanProperty.class::isInstance)
                        .findFirst().orElse(null);

        if (formedProp != null) {
            // 两个模型：_formed / _common
            ModelFile modelFormed = models().cubeAll(baseName + "_formed", modLoc(texBase + "formed"));
            ModelFile modelCommon = models().cubeAll(baseName + "_common", modLoc(texBase + "common"));

            VariantBlockStateBuilder v = getVariantBuilder(block.get());
            v.partialState().with(formedProp, true ).modelForState().modelFile(modelFormed).addModel();
            v.partialState().with(formedProp, false).modelForState().modelFile(modelCommon).addModel();

            // 物品模型用 common
            simpleBlockItem(block.get(), modelCommon);
        } else {
            // 不含 formed 属性：退化为 common 的简单 cube_all（并生成对应物品模型）
            ModelFile modelCommon = models().cubeAll(baseName, modLoc(texBase + "common"));
            simpleBlockWithItem(block.get(), modelCommon);
        }
    }


    /* 生成单个状态的 6 面模型 */
    private ModelFile wallLikeModel(Block block, String stateName, String texBase, ResourceLocation parent) {
        final String modelName = BuiltInRegistries.BLOCK.getKey(block).getPath() + "_" + stateName;

        return models()
                .withExistingParent(modelName, parent) // 建议传入 minecraft:block/cube
                .texture("up",    faceTex(Direction.UP,    stateName, texBase))
                .texture("down",  faceTex(Direction.DOWN,  stateName, texBase))
                .texture("north", faceTex(Direction.NORTH, stateName, texBase))
                .texture("south", faceTex(Direction.SOUTH, stateName, texBase))
                .texture("east",  faceTex(Direction.EAST,  stateName, texBase))
                .texture("west",  faceTex(Direction.WEST,  stateName, texBase))
                .texture("particle", modTex(texBase + "common")); // 粒子贴图
    }

    /* 正则表达式快速匹配状态 */
    private static final Pattern CORNER = Pattern.compile("^x(min|max)_y(min|max)_z(min|max)$");

    private String faceTex(Direction face, String state, String texBase) {
        final String s = state.toLowerCase(Locale.ROOT);

        switch (s) {
            case "common": return modTex(texBase + "common");
            case "center": return modTex(texBase + "center");
            case "xedge":
                return switch (face) {
                    case EAST, WEST -> modTex(texBase + "center");
                    case UP, DOWN, NORTH, SOUTH -> modTex(texBase + "middle_horizontal");
                };
            case "yedge":
                return switch (face) {
                    case UP, DOWN -> modTex(texBase + "center");
                    case NORTH, SOUTH, EAST, WEST -> modTex(texBase + "middle_vertical");
                };
            case "zedge":
                return switch (face) {
                    case NORTH, SOUTH -> modTex(texBase + "center");
                    case UP, DOWN -> modTex(texBase + "middle_vertical");
                    case EAST, WEST -> modTex(texBase + "middle_horizontal");
                };
        }

        // B) 八个角：直接按面给“left/top”的布尔表达式
        final Matcher m = CORNER.matcher(s);
        if (!m.matches()) return modTex(texBase + "common"); // 兜底

        final boolean xMin = "min".equals(m.group(1));
        final boolean yMin = "min".equals(m.group(2));
        final boolean zMin = "min".equals(m.group(3));

        // 针对每个面，定义“看着这个面时”的 left/top：
        // 对于UP和DOWN，是面对北面时，同时看着此面的结果
        final boolean left, top;
        switch (face) {
            case UP:
                left = xMin;   top = zMin;
                break;
            case DOWN:
                left = xMin;  top = !zMin;
                break;
            case NORTH:
                left = !xMin;   top = !yMin;
                break;
            case SOUTH:
                left = xMin;   top = !yMin;
                break;
            case EAST:
                left = !zMin;   top = !yMin;
                break;
            case WEST:
                left = zMin;  top = !yMin;
                break;
            default:
                return modTex(texBase + "common");
        }

        if (top && left) return modTex(texBase + "top_left");
        if (top) return modTex(texBase + "top_right");
        if (left) return modTex(texBase + "bottom_left");
        return modTex(texBase + "bottom_right");
    }

    /* 物品模型选一个合适的状态，优先选择common */
    private <T extends Enum<T> & StringRepresentable> String pickItemStateName(EnumProperty<T> prop) {
        Optional<String> common = prop.getPossibleValues().stream()
                .map(v -> v.getSerializedName().toLowerCase(Locale.ROOT))
                .filter("common"::equals).findFirst();
        if (common.isPresent()) return "common";

        Optional<String> center = prop.getPossibleValues().stream()
                .map(v -> v.getSerializedName().toLowerCase(Locale.ROOT))
                .filter("center"::equals).findFirst();
        if (center.isPresent()) return "center";

        return prop.getPossibleValues().iterator().next().getSerializedName();
    }

    private String modTex(String path)
    {
        // path 形如 "block/<registry_path>/<suffix>"
        return modLoc(path).toString();
    }

}
