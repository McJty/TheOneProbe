package mcjty.theoneprobe.apiimpl.providers;

import static mcjty.theoneprobe.api.TextStyleClass.INFO;
import static mcjty.theoneprobe.api.TextStyleClass.LABEL;
import static mcjty.theoneprobe.api.TextStyleClass.MODNAME;
import static mcjty.theoneprobe.api.TextStyleClass.NAME;
import static mcjty.theoneprobe.api.TextStyleClass.OK;
import static mcjty.theoneprobe.api.TextStyleClass.PROGRESS;
import static mcjty.theoneprobe.api.TextStyleClass.WARNING;
import static net.minecraftforge.fluids.FluidAttributes.BUCKET_VOLUME;

import java.util.Collections;
import java.util.EnumMap;

import com.mojang.authlib.GameProfile;

import mcjty.lib.api.power.IBigPower;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.Tools;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IBlockDisplayOverride.DisplayFlag;
import mcjty.theoneprobe.api.IProbeConfig;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.ProbeConfig;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import mcjty.theoneprobe.compat.TeslaTools;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BrewingStandBlock;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.Property;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

public class DefaultProbeInfoProvider implements IProbeInfoProvider {

    @Override
    public String getID() {
        return TheOneProbe.MODID + ":default";
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        Block block = blockState.getBlock();
        BlockPos pos = data.getPos();

        IProbeConfig config = Config.getRealConfig();
        
        boolean handled = false;
        EnumMap<DisplayFlag, Runnable> overrides = new EnumMap<>(DisplayFlag.class);
        for (IBlockDisplayOverride override : TheOneProbe.theOneProbeImp.getBlockOverrides()) {
            if (override.overrideStandardInfo(mode, probeInfo, player, world, blockState, data, overrides)) {
                handled = true;
                break;
            }
        }
        if (!handled) {
            showStandardBlockInfo(config, mode, probeInfo, blockState, block, world, pos, player, data);
        }

        if (Tools.show(mode, config.getShowCropPercentage())) {
        	Runnable override = overrides.get(DisplayFlag.GROWTH_INFO);
        	if(override != null) {
        		override.run();
        	} else {
                showGrowthLevel(probeInfo, blockState);        		
        	}
        }
        
    	Runnable override = overrides.get(DisplayFlag.HARVEST_INFO);
    	if(override != null) {
    		override.run();
    	} else {
            boolean showHarvestLevel = Tools.show(mode, config.getShowHarvestLevel());
            boolean showHarvested = Tools.show(mode, config.getShowCanBeHarvested());
            if (showHarvested && showHarvestLevel) {
                HarvestInfoTools.showHarvestInfo(probeInfo, world, pos, block, blockState, player);
            } else if (showHarvestLevel) {
                HarvestInfoTools.showHarvestLevel(probeInfo, blockState, block);
            } else if (showHarvested) {
                HarvestInfoTools.showCanBeHarvested(probeInfo, world, pos, block, player);
            }
    	}

        if (Tools.show(mode, config.getShowRedstone())) {
        	override = overrides.get(DisplayFlag.REDSTONE_INFO);
        	if(override != null) {
        		override.run();
        	} else {
        		showRedstonePower(probeInfo, world, blockState, data, block, Tools.show(mode, config.getShowLeverSetting()));
        	}
        }
        if (Tools.show(mode, config.getShowLeverSetting())) {
        	override = overrides.get(DisplayFlag.LEVER_INFO);
        	if(override != null) {
        		override.run();
        	} else {
        		showLeverSetting(probeInfo, world, blockState, data, block);
        	}
        }
        
    	override = overrides.get(DisplayFlag.CHEST_INFO);
    	if(override != null) {
    		override.run();
    	} else {
    		ChestInfoTools.showChestInfo(mode, probeInfo, world, pos, config);
    	}

        if (config.getRFMode() > 0) {
        	override = overrides.get(DisplayFlag.RF_INFO);
        	if(override != null) {
        		override.run();
        	} else {
        		showEnergy(probeInfo, world, pos);
        	}
        }
        if (Tools.show(mode, config.getShowTankSetting())) {
        	override = overrides.get(DisplayFlag.TANK_INFO);
        	if(override != null) {
        		override.run();
        	} else {
        		if (config.getTankMode() > 0) {
        			showTankInfo(probeInfo, world, pos);
        		}
        	}
        }

        if (Tools.show(mode, config.getShowBrewStandSetting())) {
        	override = overrides.get(DisplayFlag.BREW_INFO);
        	if(override != null) {
        		override.run();
        	} else {
        		showBrewingStandInfo(probeInfo, world, data, block);
        	}
        }

        if (Tools.show(mode, config.getShowMobSpawnerSetting())) {
        	override = overrides.get(DisplayFlag.MOB_INFO);
        	if(override != null) {
        		override.run();
        	} else {
        		showMobSpawnerInfo(probeInfo, world, data, block);
        	}
        }

        if (Tools.show(mode, config.getShowNoteblockInfo())) {
        	override = overrides.get(DisplayFlag.NOTE_BLOCK_INFO);
        	if(override != null) {
        		override.run();
        	} else {
        		showNoteblockInfo(probeInfo, world, data, blockState);
        	}
        }

        if (Tools.show(mode, config.getShowSkullInfo())) {
        	override = overrides.get(DisplayFlag.SKULL_INFO);
        	if(override != null) {
        		override.run();
        	} else {
                showSkullInfo(probeInfo, world, data, blockState);        		
        	}
        }
    }

    private void showBrewingStandInfo(IProbeInfo probeInfo, World world, IProbeHitData data, Block block) {
        if (block instanceof BrewingStandBlock) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof BrewingStandTileEntity) {
                int brewtime = ((BrewingStandTileEntity) te).brewTime;
                int fuel = ((BrewingStandTileEntity) te).fuel;
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                        .item(new ItemStack(Items.BLAZE_POWDER), probeInfo.defaultItemStyle().width(16).height(16))
                        .text(CompoundText.createLabelInfo("Fuel: ", fuel));
                if (brewtime > 0) {
                    probeInfo.text(CompoundText.createLabelInfo("Time: ", brewtime + " ticks"));
                }

            }
        }
    }

    private static final String[] NOTE_TABLE = {
            "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#"
    };

    private void showNoteblockInfo(IProbeInfo probeInfo, World world, IProbeHitData data, BlockState blockState) {
        if (blockState.getBlock() instanceof NoteBlock) {
            int note = blockState.get(NoteBlock.NOTE);
            NoteBlockInstrument instrument = blockState.get(NoteBlock.INSTRUMENT);
            if (note < 0) {
                note = 0;
            } else if (note > 24) {
                note = 24;
            }
            probeInfo.horizontal(probeInfo.defaultLayoutStyle()
                    .alignment(ElementAlignment.ALIGN_CENTER))
                    .text(CompoundText.create().style(LABEL).text("Note: ")
                            .info(instrument.name().toLowerCase() + " " + NOTE_TABLE[note] + " (" + note + ")"));
        }
    }

    private void showSkullInfo(IProbeInfo probeInfo, World world, IProbeHitData data, BlockState blockState) {
        if (blockState.getBlock() instanceof SkullBlock) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof SkullTileEntity) {
                GameProfile profile = ((SkullTileEntity) te).getPlayerProfile();
                if (profile != null) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle()
                            .alignment(ElementAlignment.ALIGN_CENTER))
                            .text(CompoundText.create().style(LABEL).text("Player: ")
                                    .info(profile.getName()));
                }
            }
        }
    }

    private void showMobSpawnerInfo(IProbeInfo probeInfo, World world, IProbeHitData data, Block block) {
        if (block instanceof SpawnerBlock) {
            TileEntity te = world.getTileEntity(data.getPos());
            if (te instanceof MobSpawnerTileEntity) {
                AbstractSpawner logic = ((MobSpawnerTileEntity) te).getSpawnerBaseLogic();
                EntityType<?> type = ForgeRegistries.ENTITIES.getValue(logic.getEntityId());
                if (type != null) {
                    probeInfo.horizontal(probeInfo.defaultLayoutStyle()
                            .alignment(ElementAlignment.ALIGN_CENTER))
                            .text(CompoundText.create().style(LABEL).text("Mob: ").info(type.getTranslationKey()));
                }
            }
        }
    }

    private void showRedstonePower(IProbeInfo probeInfo, World world, BlockState blockState, IProbeHitData data, Block block,
                                   boolean showLever) {
        if (showLever && block instanceof LeverBlock) {
            // We are showing the lever setting so we don't show redstone in that case
            return;
        }
        int redstonePower;
        if (block instanceof RedstoneWireBlock) {
            redstonePower = blockState.get(RedstoneWireBlock.POWER);
        } else {
            redstonePower = world.getRedstonePower(data.getPos(), data.getSideHit().getOpposite());
        }
        if (redstonePower > 0) {
            probeInfo.horizontal()
                    .item(new ItemStack(Items.REDSTONE), probeInfo.defaultItemStyle().width(14).height(14))
                    .text(CompoundText.createLabelInfo("Power: ", redstonePower));
        }
    }

    private void showLeverSetting(IProbeInfo probeInfo, World world, BlockState blockState, IProbeHitData data, Block block) {
        if (block instanceof LeverBlock) {
            Boolean powered = blockState.get(LeverBlock.POWERED);
            probeInfo.horizontal().item(new ItemStack(Items.REDSTONE), probeInfo.defaultItemStyle().width(14).height(14))
                    .text(CompoundText.createLabelInfo("State: ", (powered ? "On" : "Off")));
        } else if (block instanceof ComparatorBlock) {
            ComparatorMode mode = blockState.get(ComparatorBlock.MODE);
            probeInfo.text(CompoundText.createLabelInfo("Mode: ", mode.getString()));
        } else if (block instanceof RepeaterBlock) {
            Boolean locked = blockState.get(RepeaterBlock.LOCKED);
            Integer delay = blockState.get(RepeaterBlock.DELAY);
            probeInfo.text(CompoundText.createLabelInfo("Delay: ", delay + " ticks"));
            if (locked) {
                probeInfo.text(CompoundText.create().style(INFO).text("Locked"));
            }
        }
    }

    private void showTankInfo(IProbeInfo probeInfo, World world, BlockPos pos) {
        ProbeConfig config = Config.getDefaultConfig();
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()) {
            te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(handler -> {
                for (int i = 0 ; i < handler.getTanks() ; i++) {
                    FluidStack fluidStack = handler.getFluidInTank(i);
                    int maxContents = handler.getTankCapacity(i);
                    if (!fluidStack.isEmpty()) {
                        addFluidInfo(probeInfo, config, fluidStack, maxContents);
                    }
                }
            });
        }
    }

    private void addFluidInfo(IProbeInfo probeInfo, ProbeConfig config, FluidStack fluidStack, int maxContents) {
        int contents = fluidStack.getAmount();
        if (!fluidStack.isEmpty()) {
            probeInfo.text(CompoundText.create().style(NAME).text("Liquid:").info(fluidStack.getTranslationKey()));
        }
        if (config.getTankMode() == 1) {
            probeInfo.progress(contents, maxContents,
                    probeInfo.defaultProgressStyle()
                            .suffix("mB")
                            .filledColor(Config.tankbarFilledColor)
                            .alternateFilledColor(Config.tankbarAlternateFilledColor)
                            .borderColor(Config.tankbarBorderColor)
                            .numberFormat(Config.tankFormat.get()));
        } else {
            probeInfo.text(CompoundText.create().style(PROGRESS).text(ElementProgress.format(contents, Config.tankFormat.get(), new StringTextComponent("mB"))));
        }
    }

    private void showEnergy(IProbeInfo probeInfo, World world, BlockPos pos) {
        ProbeConfig config = Config.getDefaultConfig();
        TileEntity te = world.getTileEntity(pos);
        if (TheOneProbe.tesla && TeslaTools.isEnergyHandler(te)) {
            long energy = TeslaTools.getEnergy(te);
            long maxEnergy = TeslaTools.getMaxEnergy(te);
            addEnergyInfo(probeInfo, config, energy, maxEnergy);
        } else if (te instanceof IBigPower) {
            long energy = ((IBigPower) te).getStoredPower();
            long maxEnergy = ((IBigPower) te).getCapacity();
            addEnergyInfo(probeInfo, config, energy, maxEnergy);
        } else if (te != null && te.getCapability(CapabilityEnergy.ENERGY).isPresent()) {
            te.getCapability(CapabilityEnergy.ENERGY).ifPresent(handler -> {
                addEnergyInfo(probeInfo, config, handler.getEnergyStored(), handler.getMaxEnergyStored());
            });
        }
    }

    private void addEnergyInfo(IProbeInfo probeInfo, ProbeConfig config, long energy, long maxEnergy) {
        if (config.getRFMode() == 1) {
            probeInfo.progress(energy, maxEnergy,
                    probeInfo.defaultProgressStyle()
                            .suffix("FE")
                            .filledColor(Config.rfbarFilledColor)
                            .alternateFilledColor(Config.rfbarAlternateFilledColor)
                            .borderColor(Config.rfbarBorderColor)
                            .numberFormat(Config.rfFormat.get()));
        } else {
            probeInfo.text(CompoundText.create().style(PROGRESS).text("FE: " + ElementProgress.format(energy, Config.rfFormat.get(), new StringTextComponent("FE"))));
        }
    }

    private void showGrowthLevel(IProbeInfo probeInfo, BlockState blockState) {
        for (Property<?> property : blockState.getProperties()) {
            if (!"age".equals(property.getName())) {
                continue;
            }
            if(property.getValueClass() == Integer.class) {
                Property<Integer> integerProperty = (Property<Integer>)property;
                int age = blockState.get(integerProperty);
                int maxAge = Collections.max(integerProperty.getAllowedValues());
                if (age == maxAge) {
                    probeInfo.text(CompoundText.create().style(OK).text("Fully grown"));
                } else {
                    probeInfo.text(CompoundText.create().style(LABEL).text("Growth: ").style(WARNING).text((age * 100) / maxAge + "%"));
                }
            }
            return;
        }
    }

    public static void showStandardBlockInfo(IProbeConfig config, ProbeMode mode, IProbeInfo probeInfo, BlockState blockState, Block block, World world,
                                             BlockPos pos, PlayerEntity player, IProbeHitData data) {
        String modName = Tools.getModName(block);

        ItemStack pickBlock = data.getPickBlock();

        if (block instanceof SilverfishBlock && mode != ProbeMode.DEBUG && !Tools.show(mode,config.getShowSilverfish())) {
            block = ((SilverfishBlock) block).getMimickedBlock();
            pickBlock = new ItemStack(block, 1);
        }

        if (block instanceof FlowingFluidBlock) {
            FluidState fluidState = block.getFluidState(blockState);
            Fluid fluid = fluidState.getFluid();
            if (fluid != Fluids.EMPTY) {
                FluidStack fluidStack = new FluidStack(fluid.getFluid(), BUCKET_VOLUME);
                ItemStack bucketStack = FluidUtil.getFilledBucket(fluidStack);

                IProbeInfo horizontal = probeInfo.horizontal();
                FluidUtil.getFluidContained(bucketStack).ifPresent(fc -> {
                    if (fluidStack.isFluidEqual(fc)) {
                        horizontal.item(bucketStack);
                    } else {
                        horizontal.icon(fluid.getAttributes().getStillTexture(), -1, -1, 16, 16, probeInfo.defaultIconStyle().width(20));
                    }
                });

                horizontal.vertical()
                        .text(CompoundText.create().name(fluidStack.getTranslationKey()))
                        .text(CompoundText.create().style(MODNAME).text(modName));
                return;
            }
        }

        if (!pickBlock.isEmpty()) {
            if (Tools.show(mode, config.getShowModName())) {
                probeInfo.horizontal()
                        .item(pickBlock)
                        .vertical()
                        .itemLabel(pickBlock)
                        .text(CompoundText.create().style(MODNAME).text(modName));
            } else {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                        .item(pickBlock)
                        .itemLabel(pickBlock);
            }
        } else {
            if (Tools.show(mode, config.getShowModName())) {
                probeInfo.vertical()
                        .text(CompoundText.create().name(block.getTranslationKey()))
                        .text(CompoundText.create().style(MODNAME).text(modName));
            } else {
                probeInfo.vertical()
                        .text(CompoundText.create().name(block.getTranslationKey()));
            }
        }
    }
}
