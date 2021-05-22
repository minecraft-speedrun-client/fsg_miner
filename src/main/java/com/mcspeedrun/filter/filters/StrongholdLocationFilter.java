package com.mcspeedrun.filter.filters;

import com.google.gson.JsonElement;
import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.WorkerContext;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.structure.generator.structure.RuinedPortalGenerator;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.terrainutils.ChunkGenerator;
import kaptainwutax.terrainutils.terrain.NetherChunkGenerator;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

public class StrongholdLocationFilter extends FilterBuilder {

    private static final int DEFAULT_DISTANCE = 300;

    public static String id = "stronghold-location";

    public StrongholdLocationFilter(){
        super(id);
    }

    private long l2norm(long x1, long z1, long x2, long z2){
        return (x1-x2)*(x1-x2) + (z1-z2)*(z1-z2);
    }

    private final HashSet<Integer> allowedStrongholdBiomes = new HashSet<>(Arrays.asList(Biomes.DEEP_OCEAN.getId(),Biomes.DEEP_WARM_OCEAN.getId(),Biomes.DEEP_LUKEWARM_OCEAN.getId(),Biomes.DEEP_COLD_OCEAN.getId(),Biomes.DEEP_FROZEN_OCEAN.getId()));

    @Override
    public Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> getFilter(FilterType type) {
        switch(type){
            case STRUCTURE:
                return (a) -> {
                    WorkerContext context = a.getValue0();
                    long seed = a.getValue1();
                    SeedInfo info = a.getValue2();
                    int distance = (int) a.getValue3();
                    long distanceSquared = (long) distance *distance;

                    context.chunkRand.setSeed(seed);

                    double angle = context.chunkRand.nextDouble() * Math.PI * 2.0D;
                    double distanceRing = 88D + context.chunkRand.nextDouble()*80D;

                    int chunkX = (int) Math.round(Math.cos(angle) * distanceRing);
                    int chunkZ = (int) Math.round(Math.sin(angle) * distanceRing);

                    info.strongholdLocation = new CPos(chunkX,chunkZ);
                    //Check stronghold angle
                    long temp1, temp2, temp3;
                    BPos strongholdPos = info.strongholdLocation.toBlockPos();
                    //Check negpos fortress to stronghold
                    if(info.fortressLocation.getX()<0) {
                        temp1 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), -1200L, 1200L);
                        temp2 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), 1639L, 439L);
                        temp3 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), -439L, -1639L);
                    } else {
                        //Check posneg fortress to stronghold
                        temp1 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), 1200L, -1200L);
                        temp2 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), -1639L, -439L);
                        temp3 = l2norm(strongholdPos.getX(), strongholdPos.getZ(), 439L, 1639L);
                    }
                    if ((temp1 > distanceSquared) && (temp2 > distanceSquared) && (temp3 > distanceSquared)){
                        return null;
                    }

                    // send back the input without the args
                    return a.removeFrom3();
                };
            case BIOME:
                return (a)-> {
                    //Initialize the parameters
                    WorkerContext context = a.getValue0();
                    long seed = a.getValue1();
                    SeedInfo info = a.getValue2();
                    int distance = (int) a.getValue3();
                    long distanceSquared = (long) distance *distance;

                    //Stronghold distance check
                    CPos[] starts = context.stronghold.getStarts(info.overworldBiomeSource,3,context.chunkRand);
                    long shortest_distance = Long.MAX_VALUE;
                    BPos shortest_position = new BPos(1000000,0,10000000);
                    long temp;
                    for (CPos startCPos : starts){
                        BPos start = startCPos.toBlockPos();
                        //Case of negpos
                        if(info.fortressLocation.getX()<0){
                            temp = l2norm(start.getX(),start.getZ(),-1200,1200);
                        } else {
                            temp = l2norm(start.getX(),start.getZ(),1200,-1200);
                        }
                        if(temp < shortest_distance){
                            shortest_distance = temp;
                            shortest_position = start;
                        }
                    }

                    if (shortest_distance > distanceSquared) return null;

                    Biome strongholdBiome = info.overworldBiomeSource.getBiome(shortest_position);

                    if(!(allowedStrongholdBiomes.contains(strongholdBiome.getId()))) return null;

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
