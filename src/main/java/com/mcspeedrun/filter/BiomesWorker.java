package com.mcspeedrun.filter;

import org.javatuples.Triplet;

import java.util.function.Function;

public class BiomesWorker extends WorkerContext {

    private final SeedInfo baseInfo;

    BiomesWorker(Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline) {
        super(pipeline);
        this.baseInfo = null;
    }

    BiomesWorker(SeedInfo baseInfo, Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline) {
        super(pipeline);
        this.baseInfo = baseInfo;
    }

    private Long nextSeed(){
        return SeedFilterer.nextBiomeSeed();
    }

    @Override
    public void run(){
        Long seed = nextSeed();
        while(seed != null){
            SeedInfo info = this.checkSeed(seed, this.baseInfo.clone());
            if(info != null){
                SeedFilterer.foundSeed(seed);
            }
            seed = nextSeed();
        }
    }
}