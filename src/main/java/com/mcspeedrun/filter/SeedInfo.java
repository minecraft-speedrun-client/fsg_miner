package com.mcspeedrun.filter;

import kaptainwutax.biomeutils.source.NetherBiomeSource;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.featureutils.loot.item.ItemStack;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.util.pos.CPos;

import java.util.List;

public class SeedInfo {
    public CPos portalLocation;
    public CPos villageLocation;
    public CPos bastionLocation;
    public CPos fortressLocation;
    public CPos strongholdLocation;
    public List<ItemStack> portalLoot;
    public BPos spawnLocation;

    public OverworldBiomeSource overworldBiomeSource;
    public NetherBiomeSource netherBiomeSource;

    public SeedInfo(){};

    private SeedInfo(CPos portalLocation, List<ItemStack> portalLoot,CPos villageLocation, BPos spawnLocation, OverworldBiomeSource overworldBiomeSource, NetherBiomeSource netherBiomeSource){
        this.portalLocation = portalLocation;
        this.portalLoot = portalLoot;
        this.villageLocation = villageLocation;
        this.spawnLocation = spawnLocation;
        this.overworldBiomeSource = overworldBiomeSource;
        this.netherBiomeSource = netherBiomeSource;
    }

    public SeedInfo clone(){
        return new SeedInfo(this.portalLocation, this.portalLoot, this.villageLocation, this.spawnLocation, this.overworldBiomeSource, this.netherBiomeSource);
    }
}