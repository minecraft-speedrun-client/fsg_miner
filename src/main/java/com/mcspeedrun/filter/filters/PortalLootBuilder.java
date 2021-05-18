package com.mcspeedrun.filter.filters;

import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.WorkerContext;
import kaptainwutax.featureutils.loot.LootContext;
import kaptainwutax.featureutils.loot.MCLootTables;
import kaptainwutax.mcutils.version.MCVersion;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PortalLootBuilder extends FilterBuilder {

    public static String id = "ruin-portal-loot-builder";

    public PortalLootBuilder(){
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

                    // build the loot for the portal
                    context.chunkRand.setDecoratorSeed(seed, info.portalLocation.getX() << 4, info.portalLocation.getZ() << 4, 40005, MCVersion.v1_16_1);
                    LootContext lootContext = new LootContext(context.chunkRand.nextLong());
                    info.portalLoot = MCLootTables.RUINED_PORTAL_CHEST.generate(lootContext);

                    // send back the input without the args
                    return a.removeFrom3();
                };
            case BIOME:
            default:
                return super.getFilter(type);
        }
    }

    // TODO: get a real number for this
    @Override
    public Double getCost(FilterType type, Object parameters){
        switch(type) {
            case STRUCTURE:
                return 12.0;
            default:
                return super.getCost(type, parameters);
        }
    }

    @Override
    public List<String> getDependencies(FilterType type) {
        switch(type) {
            case STRUCTURE:
                List<String> list = new ArrayList<>();
                list.add(PortalLocationFilter.id);
                return list;
            default:
                return super.getDependencies(type);
        }
    }
}
