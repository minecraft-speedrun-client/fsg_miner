package com.mcspeedrun.filter;

import org.javatuples.Triplet;

import java.util.function.Function;

public class BiomesWorker extends WorkerContext {

    private final SeedInfo baseInfo;
    private final long structureSeed;

    BiomesWorker(long structureSeed, Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline) {
        super(pipeline);
        this.baseInfo = null;
        this.structureSeed = structureSeed;
    }

    BiomesWorker(SeedFilterer filterer, long structureSeed, SeedInfo baseInfo, Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline) {
        super(filterer, pipeline);
        this.baseInfo = baseInfo;
        this.structureSeed = structureSeed;
    }

    private Long nextSeed(){
        return this.filterer.nextBiomeSeed();
    }

    public SeedInfo checkSeed(long seed, SeedInfo info){
        return super.checkSeed(seed << 48 | structureSeed, info);
    }

    @Override
    public void run(){
        Long biomeSeed = nextSeed();
        while(biomeSeed != null){
            SeedInfo info = this.checkSeed(biomeSeed, this.baseInfo.clone());
            if(info != null){
                this.filterer.foundSeed(biomeSeed << 48 | structureSeed);
                return;
            }
            biomeSeed = nextSeed();
        }
    }
}