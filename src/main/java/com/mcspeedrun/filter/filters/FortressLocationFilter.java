package com.mcspeedrun.filter.filters;

import com.google.gson.JsonElement;
import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.WorkerContext;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FortressLocationFilter extends FilterBuilder {

    private static final int DEFAULT_DISTANCE = 96;

    public static String id = "fortress-location";

    public FortressLocationFilter(){
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

                    // get the cords of the ruined portal
                    info.fortressLocation = context.fortress.getInRegion(seed, 0, -1, context.chunkRand);

                    // if one didnt spawn:
                    if (info.fortressLocation == null){
                        info.fortressLocation = context.fortress.getInRegion(seed, -1, 0, context.chunkRand);
                        // if the second one doesn't spawn either:
                        if(info.fortressLocation == null) return null;
                    }

                    // if the fortress is outside of our box then return null
                    if(Math.abs(info.fortressLocation.toBlockPos().getX()) >= distance || Math.abs(info.fortressLocation.toBlockPos().getX()) >= distance){
                        return null;
                    }

                    // send back the input without the args
                    return a.removeFrom3();
                };
            case BIOME:
                return (a)->{
                    WorkerContext context = a.getValue0();
                    SeedInfo info = a.getValue2();

                    //Check if the fortress can spawn
                    if(!context.fortress.canSpawn(info.fortressLocation,info.netherBiomeSource)) return null;
                    else return a.removeFrom3();
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
                list.add(NetherBiomeBuilder.id);
                return list;
            default:
                return super.getDependencies(type);
        }
    }
}
