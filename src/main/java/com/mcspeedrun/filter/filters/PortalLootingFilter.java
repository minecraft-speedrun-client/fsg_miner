package com.mcspeedrun.filter.filters;

import com.google.gson.JsonElement;
import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.WorkerContext;
import kaptainwutax.featureutils.loot.item.ItemStack;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PortalLootingFilter extends FilterBuilder {

    private static final int DEFAULT_LEVEL = 3;

    public static String id = "ruin-portal-looting";

    public PortalLootingFilter(){
        super(id);
    }

    @Override
    public Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> getFilter(FilterType type) {
        switch(type){
            case STRUCTURE:
                return (a) -> {
                    SeedInfo info = a.getValue2();
                    int level = (int) a.getValue3();

                    // loop though all of the items looking for a looting sword that is at least our target level
                    for(ItemStack itemStack : info.portalLoot) {
                        if(itemStack.getItem().getName().equals("golden_sword") && itemStack.getItem().getEnchantments().get(0).getFirst().equals("looting") && itemStack.getItem().getEnchantments().get(0).getSecond() >= level){
                            return a.removeFrom3();
                        }
                    }

                    // if we didn't find anything then this filter failed
                    return null;
                };
            case BIOME:
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
                    return DEFAULT_LEVEL;
                }
                // if we got bad parameters just use the default
                try {
                    return parameters.getAsInt();
                }
                catch(ClassCastException ignored){
                    return DEFAULT_LEVEL;
                }
            default:
                return super.buildParameters(type, parameters);
        }
    }

    // TODO: get real number for chance of iron amount
    @Override
    public Double getPower(FilterType type, Object parameters){
        switch(type) {
            case STRUCTURE:
                return 0.5;
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
            case STRUCTURE:
                List<String> list = new ArrayList<>();
                list.add(PortalLootBuilder.id);
                return list;
            default:
                return super.getDependencies(type);
        }
    }
}
