package com.mcspeedrun.filter.filters;

import com.google.gson.JsonElement;
import com.mcspeedrun.filter.FilterType;
import com.mcspeedrun.filter.SeedInfo;
import com.mcspeedrun.filter.FilterBuilder;
import com.mcspeedrun.filter.WorkerContext;
import kaptainwutax.featureutils.loot.item.ItemStack;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PortalIronFilter extends FilterBuilder {

    private static final int DEFAULT_IRON = 5 * 9;

    public static String id = "ruin-portal-iron";

    public PortalIronFilter(){
        super(id);
    }

    @Override
    public Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> getFilter(FilterType type) {
        switch(type){
            case STRUCTURE:
                return (a) -> {
                    SeedInfo info = a.getValue2();
                    int count = (int) a.getValue3();

                    // get a count of how much iron is in this chest till we have enough or no more items
                    int iron = 0;
                    for(ItemStack itemStack : info.portalLoot) {
                        if(itemStack.getItem().getName().equals("iron_nugget")){
                            iron += itemStack.getCount();
                            // if we found all the iron then we can exit without checking the rest
                            if(iron >= count){
                                return a.removeFrom3();
                            }
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
                    return DEFAULT_IRON;
                }
                // if we got bad parameters just use the default
                try {
                    return parameters.getAsInt() * 9;
                }
                catch(ClassCastException ignored){
                    return DEFAULT_IRON;
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
