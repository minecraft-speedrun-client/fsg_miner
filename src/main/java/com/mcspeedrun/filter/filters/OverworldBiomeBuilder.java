package com.mcspeedrun.filter.filters;

import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.WorkerContext;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.mcutils.version.MCVersion;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.function.Function;

public class OverworldBiomeBuilder extends FilterBuilder {

    public static String id = "overworld-biome-builder";

    public OverworldBiomeBuilder() {
        super(id);
    }

    @Override
    public Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> getFilter(FilterType type) {
        switch(type){
            case BIOME:
                return (a)-> {
                    //Initialize the parameters
                    long seed = a.getValue1();
                    SeedInfo info = a.getValue2();

                    // set up the overworld biome source
                    info.overworldBiomeSource = new OverworldBiomeSource(MCVersion.v1_16_1,seed);

                    //Send back the import without the args
                    return a.removeFrom3();
                };
            default:
                return super.getFilter(type);
        }
    }
}
