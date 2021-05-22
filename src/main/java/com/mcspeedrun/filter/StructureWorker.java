package com.mcspeedrun.filter;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.Random;
import java.util.function.Function;

class StructureWorker extends WorkerContext {

    private final int blockHeight;

    public StructureWorker(Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline){
        super(pipeline);
        this.blockHeight = 0;
    }

    public StructureWorker(SeedFilterer filterer, int blockHeight, Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline){
        super(filterer, pipeline);
        this.blockHeight = blockHeight;
    }

    private Pair<Long, Long> nextSeed(){
        return this.filterer.nextStructureSeed();
    }

    public void run(){
        // long 1 is the block id, long 2 is the block seed
        Pair<Long, Long> block = this.nextSeed();
        Random random;
        while(block != null) {
            random = new Random(block.getValue1());

            // walk up the height of this block trying to find a seed
            for(int i = 0; i < this.blockHeight; i++){
                long seed = random.nextLong();
                SeedInfo info = this.checkSeed(seed);
                if(info != null){
                    this.filterer.foundStructureSeed(block.getValue0(), seed, info);
                    return;
                }
            }

            // get the next block
            block = this.nextSeed();
        }
    }

    public SeedInfo checkSeed(long seed){
        return this.checkSeed(seed, new SeedInfo());
    }
}