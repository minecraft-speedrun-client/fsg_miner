package com.mcspeedrun.filter;

import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mcspeedrun.filter.filters.*;
import org.javatuples.Pair;

public class SeedFilterer {

    private static final Object seedFindLock = new Object();

    public static final int DEFAULT_BLOCK_HEIGHT = 100000;
    // TODO: dynamic tolerance based on settings
    static final int biomeTolerance = 300;

    static PipelineBuilder settings;

    // TODO: move this over to a test module
    public static void main(String[] args) {
        init();

        JsonArray settings = new JsonArray();

        JsonObject setting = new JsonObject();
        setting.add("id", new JsonPrimitive(PortalLootingFilter.id));
        setting.add("value", new JsonPrimitive(1));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(PortalIronFilter.id));
        setting.add("value", new JsonPrimitive(1));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(PortalCryingFilter.id));
        setting.add("value", new JsonPrimitive(false));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(PortalLocationFilter.id));
        setting.add("value", new JsonPrimitive(96));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(VillageLocationFilter.id));
        setting.add("value", new JsonPrimitive(96));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(BastionLocationFilter.id));
        setting.add("value", new JsonPrimitive(96));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(FortressLocationFilter.id));
        setting.add("value", new JsonPrimitive(96));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(SpawnLocationFilter.id));
        setting.add("value", new JsonPrimitive(96));
        settings.add(setting);

        setting = new JsonObject();
        setting.add("id", new JsonPrimitive(StrongholdLocationFilter.id));
        setting.add("value", new JsonPrimitive(300));
        settings.add(setting);

        setFilters(settings);
        long seed = findSeed(1L<<31);
        System.out.println(seed);
        System.out.println(checkSeed(seed));
    }

    public static void init(){
        synchronized(seedFindLock) {
            new OverworldBiomeBuilder();
            new NetherBiomeBuilder();
            new PortalLocationFilter();
            new PortalLootBuilder();
            new PortalIronFilter();
            new PortalLootingFilter();
            new PortalCryingFilter();
            new VillageLocationFilter();
            new BastionLocationFilter();
            new FortressLocationFilter();
            new SpawnLocationFilter();
            new StrongholdLocationFilter();
        }
    }

    public static void setFilters(JsonArray filters){
        synchronized(seedFindLock){
            settings = new PipelineBuilder(filters);
        }
    }

    public static boolean checkSeed(long seed){
        synchronized(seedFindLock){
            // check the structure seed
            SeedInfo info = new StructureWorker(settings.buildStructurePipe()).checkSeed(seed);
            if (info == null) {
                return false;
            }

            // check the rest of the seed
            info = new BiomesWorker(seed & 0xffffffffffffL, settings.buildBiomePipe()).checkSeed(seed >> 48, info);
            return info != null;
        }
    }

    public static long findSeed(long initial){
        return findSeed(initial, DEFAULT_BLOCK_HEIGHT);
    }

    public static long findSeed(long initial, int blockHeight){
        return findSeed(initial, blockHeight, Runtime.getRuntime().availableProcessors()*2);
    }

    public static long findSeed(long initial, int blockHeight, int threadCount){
        SeedFilterer filterer;

        synchronized(seedFindLock) {
            filterer = new SeedFilterer(settings, initial, blockHeight, threadCount);
        }

        return filterer.findSeed();
    }

    PipelineBuilder pipelineBuilder;

    int threadCount;
    int blockHeight;
    long initialSeed;

    // state 0 is finding structure seed
    // state 1 if finding biomes
    // state 2 is done
    int state;

    long structureSeedID;
    long biomeSeedID;

    long seed;

    Random seedGen;
    SeedInfo seedInfo;

    private SeedFilterer(PipelineBuilder pipelineBuilder, long initialSeed, int blockHeight, int threadCount){
        this.pipelineBuilder = pipelineBuilder;
        this.initialSeed = initialSeed;
        this.blockHeight = blockHeight;
        this.threadCount = threadCount;

        // reset all of the state vars
        this.state = 0;
        this.structureSeedID = -1;
        this.biomeSeedID = -1;
        this.seedInfo = null;
    }

    private long findSeed(){
        this.seedGen = new Random(this.initialSeed);

        Thread[] workers = new Thread[this.threadCount];

        // state machine for walking though the structure and biome seeds
        while(this.state != 2){
            switch(this.state){
                case 0:
                    for(int i = 0; i < this.threadCount; i++) {
                        workers[i] = new StructureWorker(this, this.blockHeight, this.pipelineBuilder.buildStructurePipe());
                        workers[i].start();
                    }
                    for (Thread worker : workers) {
                        try {
                            worker.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1:
                    for(int i = 0; i < this.threadCount; i++) {
                        workers[i] = new BiomesWorker(this, seed, this.seedInfo, this.pipelineBuilder.buildBiomePipe());
                        workers[i].start();
                    }
                    for(Thread worker : workers) {
                        try {
                            worker.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }

        return this.seed;
    }

    public synchronized Pair<Long, Long> nextStructureSeed(){
        if(state != 0){
            return null;
        }
        structureSeedID++;
        return new Pair<>(structureSeedID, seedGen.nextLong());
    }

    public synchronized Long nextBiomeSeed(){
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
        return biomeSeedID;
    }

    public synchronized void cancelBiomeSeed(){
        state = 0;
        biomeSeedID = -1;
    }

    public synchronized void foundStructureSeed(long id, long structureSeed, SeedInfo info){
        // switch to biome search mode
        state = 1;
        if(structureSeedID == 0 || id < structureSeedID){
            seedInfo = info;
            structureSeedID = id;
            seed = structureSeed & 0xffffffffffffL;
        }
    }

    public synchronized void foundSeed(Long foundSeed){
        state = 2;
        seed = foundSeed;
    }
}