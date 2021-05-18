package com.mcspeedrun.filter;

import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mcspeedrun.filter.filters.*;
import org.javatuples.Pair;

public class SeedFilterer {

    // state 0 is finding structure seed
    // state 1 if finding biomes
    // state 2 is done
    static int state = 0;

    public static final int blockHeight = 100000;
    static final int biomeTolerance = 300;
    static Random seedGen;

    static long seed;
    static long structureSeedID = -1;
    static long biomeSeedID = -1;

    static SeedInfo seedInfo;

    static PipelineBuilder pipelineBuilder;

    public static void main(String[] args) {
        init();

        JsonArray settings = new JsonArray();

        JsonObject setting = new JsonObject();
        setting.add("id", new JsonPrimitive(PortalLootingFilter.id));
        setting.add("value", new JsonPrimitive(3));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(PortalIronFilter.id));
        setting.add("value", new JsonPrimitive(4));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(PortalCryingFilter.id));
        setting.add("value", new JsonPrimitive(true));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(PortalLocationFilter.id));
        setting.add("value", new JsonPrimitive(96));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(VillageLocationFilter.id));
        setting.add("value", new JsonPrimitive(96));
        settings.add(setting);

        setFilters(settings);
        System.out.println(findSeed(1L));
    }

    public static void init(){
        new OverworldBiomeBuilder();
        new PortalLocationFilter();
        new PortalLootBuilder();
        new PortalIronFilter();
        new PortalLootingFilter();
        new PortalCryingFilter();
        new VillageLocationFilter();
    }

    public static void setFilters(JsonArray filters){
        pipelineBuilder = new PipelineBuilder(filters);
    }

    public static boolean checkSeed(long seed){
        // check the structure seed
        SeedInfo info = new StructureWorker(pipelineBuilder.buildStructurePipe()).checkSeed(seed);
        if(info == null){
            return false;
        }
        // check the rest of the seed
        info = new BiomesWorker(pipelineBuilder.buildBiomePipe()).checkSeed(seed, info);
        return info != null;
    }

    // TODO: block height as parameter and not as final
    public static long findSeed(long initial){
        return findSeed(initial, Runtime.getRuntime().availableProcessors()*2);
    }

    public static long findSeed(long initial, int threadCount){

        // reset all of the state vars
        state = 0;
        structureSeedID = -1;
        biomeSeedID = -1;
        seedInfo = null;

        seedGen = new Random(initial);

        Thread[] workers = new Thread[threadCount];

        // state machine for walking though the structure and biome seeds
        while(state != 2) {
            switch(state){
                case 0:
                    for(int i = 0; i < threadCount; i++){
                        workers[i] = new StructureWorker(blockHeight, pipelineBuilder.buildStructurePipe());
                        workers[i].start();
                    }
                    for(Thread worker:workers){
                        try {
                            worker.join();
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1:
                    for(int i = 0; i < threadCount; i++){
                        workers[i] = new BiomesWorker(seedInfo, pipelineBuilder.buildBiomePipe());
                        workers[i].start();
                    }
                    for(Thread worker:workers){
                        try {
                            worker.join();
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }

        return seed;
    }

    public static synchronized Pair<Long, Long> nextStructureSeed(){
        if(state != 0){
            return null;
        }
        structureSeedID++;
        return new Pair<Long, Long>(structureSeedID, seedGen.nextLong());
    }


    public static synchronized Long nextBiomeSeed(){
        // if something made the state change then exit our search
        if(state != 1){
            return null;
        }
        biomeSeedID++;
        // if we have tried all of the biome seeds then go back to step 1
        if(biomeSeedID == biomeTolerance){
            cancelBiomeSeed();
            return null;
        }
        return (long) biomeSeedID << 48 | seed;
    }

    public static synchronized void cancelBiomeSeed(){
        state = 0;
        biomeSeedID = -1;
    }

    public static synchronized void foundStructureSeed(long id, long structureSeed, SeedInfo info){
        // switch to biome search mode
        state = 1;
        if(structureSeedID == 0 || id < structureSeedID){
            seedInfo = info;
            structureSeedID = id;
            seed = structureSeed & 0xffffffffffffL;
        }
    }

    public static synchronized void foundSeed(Long foundSeed){
        state = 2;
        seed = foundSeed;
    }
}