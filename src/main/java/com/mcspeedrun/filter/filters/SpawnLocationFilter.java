package com.mcspeedrun.filter.filters;

import com.google.gson.JsonElement;
import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.WorkerContext;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SpawnLocationFilter extends FilterBuilder {

    public static final int DEFAULT_DISTANCE = 192;
    public static String id = "close-spawn-distance";

    public SpawnLocationFilter() {
        super(id);
    }

    @Override
    public Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> getFilter(FilterType type) {
        switch(type){
            case BIOME:
                return (a) -> {
                    WorkerContext context = a.getValue0();
                    long seed = a.getValue1();
                    SeedInfo info = a.getValue2();
                    int distance = (int) a.getValue3();

                    int minDist = (int) (distance * -0.25);
                    int maxDist = (int) (distance * 0.75);

                    OverworldBiomeSource overworldBiomeSource = info.overworldBiomeSource;
                    info.spawnLocation = overworldBiomeSource.getSpawnPoint();
                    int x = info.spawnLocation.getX();
                    int z = info.spawnLocation.getZ();

                    if(x <= minDist || z <= minDist){
                        return null;
                    }
                    if(x >= maxDist || z >= maxDist){
                        return null;
                    }

                    // send back the input without the args
                    return a.removeFrom3();
                };
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
            case BIOME:
                // TODO: distribution based on size
                return 0.5;
            default:
                return super.getPower(type, parameters);
        }
    }

    @Override
    public Double getCost(FilterType type, Object parameters){
        switch(type) {
            case BIOME:
                return 1.0;
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
