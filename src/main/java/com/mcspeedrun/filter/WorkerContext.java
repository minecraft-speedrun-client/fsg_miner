package com.mcspeedrun.filter;

import kaptainwutax.featureutils.structure.*;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;
import org.javatuples.Triplet;

import java.util.function.Function;

public abstract class WorkerContext extends Thread {

    public final Village village = new Village(MCVersion.v1_16_1);
    public final Fortress fortress = new Fortress(MCVersion.v1_16_1);
    public final Stronghold stronghold = new Stronghold(MCVersion.v1_16_1);
    public final BastionRemnant bastion = new BastionRemnant(MCVersion.v1_16_1);
    public final RuinedPortal portal = new RuinedPortal(Dimension.OVERWORLD, MCVersion.v1_16_1);
    public final ChunkRand chunkRand = new ChunkRand();

    private final Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline;
    protected final SeedFilterer filterer;

    WorkerContext(Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline){
        this(null, pipeline);
    }

    WorkerContext(SeedFilterer filterer, Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> pipeline){
        this.pipeline = pipeline;
        this.filterer = filterer;
    }

    public SeedInfo checkSeed(long seed, SeedInfo info) {
        Triplet<WorkerContext, Long, SeedInfo> out = pipeline.apply(new Triplet<>(this, seed, info));
        if(out == null){
            return null;
        }
        return out.getValue2();
    }
}
