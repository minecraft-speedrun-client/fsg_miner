package com.mcspeedrun.filter.filters;


import com.google.gson.JsonElement;
import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.WorkerContext;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.loot.item.ItemStack;
import kaptainwutax.featureutils.structure.generator.structure.RuinedPortalGenerator;
import kaptainwutax.mcutils.block.Block;
import kaptainwutax.mcutils.block.Blocks;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.ChunkGenerator;
import kaptainwutax.terrainutils.terrain.OverworldChunkGenerator;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class PortalCryingFilter extends FilterBuilder {

    private static final List<HashSet<BPos>> criticalObbyPositions = new ArrayList<>(Arrays.asList(
            new HashSet<>(Arrays.asList(
                    new BPos(3,2,2),
                    new BPos(3,2,3),
                    new BPos(3,3,1),
                    new BPos(3,4,1),
                    new BPos(3,5,1),
                    new BPos(3,3,4),
                    new BPos(3,6,2),
                    new BPos(3,6,3))),

            new HashSet<>(Arrays.asList(
                    new BPos(5,5,2),
                    new BPos(5,6,2),
                    new BPos(5,7,2),
                    new BPos(5,7,5),
                    new BPos(5,8,3),
                    new BPos(5,8,4))),

            new HashSet<>(Arrays.asList(
                    new BPos(4,3,3),
                    new BPos(4,3,4),
                    new BPos(4,4,5),
                    new BPos(4,5,5),
                    new BPos(4,6,5),
                    new BPos(4,7,4))),

            new HashSet<>(Arrays.asList(
                    new BPos(4,3,3),
                    new BPos(4,3,4),
                    new BPos(4,4,2),
                    new BPos(4,5,2),
                    new BPos(4,6,2),
                    new BPos(4,4,5),
                    new BPos(4,5,5))),

            new HashSet<>(Arrays.asList(
                    new BPos(2,3,2),
                    new BPos(2,3,3),
                    new BPos(2,4,4),
                    new BPos(2,5,4),
                    new BPos(2,6,4))),

            new HashSet<>(Arrays.asList(

                    new BPos(2,1,1),
                    new BPos(2,1,2),
                    new BPos(2,1,3),
                    new BPos(2,2,0),
                    new BPos(2,2,4),
                    new BPos(2,3,0),
                    new BPos(2,3,4),
                    new BPos(2,4,0),
                    new BPos(2,4,4),
                    new BPos(2,5,1),
                    new BPos(2,5,3))),


            new HashSet<>(Arrays.asList(
                    new BPos(3,0,3),
                    new BPos(3,0,4),
                    new BPos(3,1,2),
                    new BPos(3,2,2),
                    new BPos(3,3,2),
                    new BPos(3,1,5),
                    new BPos(3,2,5),
                    new BPos(3,4,3),
                    new BPos(3,4,4))),


            new HashSet<>(Arrays.asList(
                    new BPos(5,3,3),
                    new BPos(5,3,4),
                    new BPos(5,3,5),
                    new BPos(5,4,2),
                    new BPos(5,4,6),
                    new BPos(5,5,2),
                    new BPos(5,5,6),
                    new BPos(5,6,2),
                    new BPos(5,6,6))),


            new HashSet<>(Arrays.asList(
                    new BPos(4,1,4),
                    new BPos(4,1,5),
                    new BPos(4,2,3),
                    new BPos(4,2,6),
                    new BPos(4,3,6),
                    new BPos(4,4,6),
                    new BPos(4,5,4),
                    new BPos(4,5,5))),


            new HashSet<>(Arrays.asList(
                    new BPos(3,1,4),
                    new BPos(3,1,5),
                    new BPos(3,2,3))),


            new HashSet<>(Arrays.asList(
                    new BPos(5,3,9),
                    new BPos(5,3,10),
                    new BPos(5,4,11),
                    new BPos(5,5,11),
                    new BPos(5,6,11))),


            new HashSet<>(Arrays.asList(
                    new BPos(5,3,9),
                    new BPos(5,3,10),
                    new BPos(5,4,11),
                    new BPos(5,5,11),
                    new BPos(5,6,11))),


            new HashSet<>(Arrays.asList(
                    new BPos(5,3,9),
                    new BPos(5,3,10),
                    new BPos(5,4,11),
                    new BPos(5,5,11),
                    new BPos(5,6,11)))
    ));
    private static final List<String> portalNameIndex = new ArrayList<>(Arrays.asList("portal_1", "portal_2", "portal_3", "portal_4", "portal_5", "portal_6", "portal_7", "portal_8", "portal_9", "portal_10","giant_portal_1", "giant_portal_2", "giant_portal_3"));

    private static final int[] chestObbyArray = new int[]{2, 4, 4, 3, 5, 1, 1, 3, 2, 7, 5, 5, 5};
    public static String id = "ruin-portal-crying";

    public PortalCryingFilter(){
        super(id);
    }

    @Override
    public Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> getFilter(FilterType type) {
        switch(type){
            case BIOME:
                return (a) -> {
                    SeedInfo info = a.getValue2();

                    //Honestly, I dont know here. So I made this a boolean, if it's false, just ignore it
                    boolean doCheck = (boolean) a.getValue3();
                    if(!doCheck){
                        return a.removeFrom3();
                    }

                    //Get the context for the chunkRand later
                    WorkerContext context = a.getValue0();

                    //Ready the necessary objects
                    OverworldBiomeSource overworldBiomeSource = info.overworldBiomeSource;
                    ChunkGenerator chunkGenerator = new OverworldChunkGenerator(overworldBiomeSource);
                    RuinedPortalGenerator rpg = new RuinedPortalGenerator(MCVersion.v1_16_1);

                    //If it fails to generate, return null
                    if(!rpg.generate(chunkGenerator,info.portalLocation,context.chunkRand)){
                        return null;
                    }
                    //Check the portal type
                    String portalType = rpg.getType();
                    if(portalType==null){
                        return null;
                    }


                    //See which portal it is, and get the relevant obbyPositions and chestObbyRequired
                    int portalIndex = portalNameIndex.indexOf(portalType);
                    HashSet<BPos> criticalObby = criticalObbyPositions.get(portalIndex);
                    int chestObbyRequired = chestObbyArray[portalIndex];


                    //Get the original position of the portal
                    BPos portalBPos = rpg.getPos();
                    //Get the obsidian blocks / their positions
                    List<Pair<Block, BPos>> obsidianBlocks = rpg.getPortal();
                    for (Pair<Block, BPos> obsidianBlock : obsidianBlocks) {
                        if(obsidianBlock.getFirst().equals(Blocks.CRYING_OBSIDIAN)){
                            if(criticalObby.contains(obsidianBlock.getSecond().subtract(portalBPos))) {
                                //If a crying obsidian block is in the set of critical positions, it's a bad seed
                                return null;
                            }
                        }
                    }


                    int obby = 0;
                    for(ItemStack itemStack : info.portalLoot) {
                        if(itemStack.getItem().getName().equals("obsidian")){
                            obby += itemStack.getCount();
                            // if we found all the obby then we can exit without checking the rest
                            if(obby >= chestObbyRequired){
                                return a.removeFrom3();
                            }
                        }
                    }

                    //If it arrives here, there's not enough obsidian in the chest
                    return null;
                };
            case STRUCTURE:
            default:
                return super.getFilter(type);
        }
    }

    @Override
    public Object buildParameters(FilterType type, JsonElement parameters) {
        switch(type){
            case BIOME:
                // if we didn't get a parameter just use the default
                if(parameters == null){
                    return false;
                }
                // if we got bad parameters just use the default
                try {
                    return parameters.getAsBoolean();
                }
                catch(ClassCastException ignored){
                    return false;
                }
            default:
                return super.buildParameters(type, parameters);
        }
    }

    // TODO: calculate power
    @Override
    public Double getPower(FilterType type, Object parameters){
        switch(type) {
            case BIOME:
                return 0.5;
            case STRUCTURE:
                return 1.0;
            default:
                return super.getPower(type, parameters);
        }
    }

    // TODO: Calculate cost
    @Override
    public Double getCost(FilterType type, Object parameters){
        switch(type) {
            case BIOME:
                return 3.0;
            default:
                return super.getCost(type, parameters);
        }
    }
    @Override
    public List<String> getDependencies(FilterType type) {
        switch(type) {
            case BIOME:
                List<String> list = new ArrayList<>();
                list.add(PortalLootBuilder.id);
                list.add(OverworldBiomeBuilder.id);
                return list;
            default:
                return super.getDependencies(type);
        }
    }
}