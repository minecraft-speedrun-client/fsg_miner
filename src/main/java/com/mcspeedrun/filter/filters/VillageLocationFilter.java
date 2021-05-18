package com.mcspeedrun.filter.filters;

import com.google.gson.JsonElement;
import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.WorkerContext;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.terrainutils.ChunkGenerator;
import kaptainwutax.terrainutils.terrain.OverworldChunkGenerator;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class VillageLocationFilter extends FilterBuilder {

    private static final int DEFAULT_DISTANCE = 96;

    public static String id = "village-location";

    public VillageLocationFilter(){
        super(id);
    }

    @Override
    public Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> getFilter(FilterType type) {
        switch(type){
            case STRUCTURE:
                return (a) -> {
                    WorkerContext context = a.getValue0();
                    long seed = a.getValue1();
                    SeedInfo info = a.getValue2();
                    int distance = (int) a.getValue3();

                    // get the cords of the village
                    info.villageLocation = context.village.getInRegion(seed, 0, 0, context.chunkRand);

                    // if one didnt spawn then return null
                    if (info.villageLocation == null) return null;

                    // if the village portal is outside of our box then return null
                    if(info.villageLocation.toBlockPos().getX() >= distance || info.villageLocation.toBlockPos().getZ() >= distance){
                        return null;
                    }

                    // send back the input without the args
                    return a.removeFrom3();
                };
            case BIOME:
                return (a)-> {
                    //Initialize the parameters
                    WorkerContext context = a.getValue0();
                    SeedInfo info = a.getValue2();

                    //Check if the village can spawn
                    OverworldBiomeSource obs = info.overworldBiomeSource;
                    if(!context.village.canSpawn(info.villageLocation,obs)) return null;

                    //Check if the village can generate
                    ChunkGenerator cg = new OverworldChunkGenerator(obs);
                    if(!context.village.canGenerate(info.villageLocation,cg)) return null;

                    //Send back the import without the args
                    return a.removeFrom3();
                };
            default:
                return super.getFilter(type);
        }
    }

    @Override
    public Object buildParameters(FilterType type, JsonElement parameters) {
        switch(type){
            case STRUCTURE:
                // if we didn't get a parameter just use the default
                if(parameters == null){
                    return DEFAULT_DISTANCE;
                }
                // if we got bad parameters just use the default
                try {
                    return parameters.getAsInt();
                }
                catch(ClassCastException ignored){
                    return DEFAULT_DISTANCE;
                }
            default:
                return super.buildParameters(type, parameters);
        }
    }

    // what % of the possible areas are we filtering for
    @Override
    public Double getPower(FilterType type, Object parameters){
        switch(type) {
            case STRUCTURE:
                int distance = (int) parameters;
                return (distance * distance) / (640.0 * 640.0);
            default:
                return super.getPower(type, parameters);
        }
    }

    // 2 random calls + some change to get the location
    @Override
    public Double getCost(FilterType type, Object parameters){
        switch(type) {
            case STRUCTURE:
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
                list.add(OverworldBiomeBuilder.id);
                return list;
            default:
                return super.getDependencies(type);
        }
    }
}