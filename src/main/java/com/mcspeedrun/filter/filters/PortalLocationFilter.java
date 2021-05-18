package com.mcspeedrun.filter.filters;

import com.google.gson.JsonElement;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.WorkerContext;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.function.Function;

public class PortalLocationFilter extends FilterBuilder {

    private static final int DEFAULT_DISTANCE = 96;

    public static String id = "ruin-portal-location";

    public PortalLocationFilter(){
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
                    info.portalLocation = context.portal.getInRegion(seed, 0, 0, context.chunkRand);

                    // if one didnt spawn then return null
                    if (info.portalLocation == null) return null;

                    // if the ruined portal is outside of our box then return null
                    if(info.portalLocation.toBlockPos().getX() >= distance || info.portalLocation.toBlockPos().getZ() >= distance){
                        return null;
                    }

                    // send back the input without the args
                    return a.removeFrom3();
                };
            case BIOME:
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
}
