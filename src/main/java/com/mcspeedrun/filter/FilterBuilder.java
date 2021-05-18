package com.mcspeedrun.filter;

import java.util.List;

import com.google.gson.JsonElement;
import org.javatuples.Triplet;
import org.javatuples.Quartet;

import java.util.function.Function;

public abstract class FilterBuilder {

    /**
     * @param type the filter type that is being requested
     * @return return null to skip, return function to execute, return Pair to continue with next function in chain
     */
    public Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> getFilter(FilterType type){
        return Quartet::removeFrom3;
    };

    /**
     * @param type the filter type we are getting the dependencies for
     * @return null or an array of the id of function this filter depends on
     */
    public List<String> getDependencies(FilterType type){
        return null;
    };

    /**
     * @param type the filter type we are getting the power for
     * @param parameters the parameters we are getting the power with
     * @return the strength of this filter. 0 being the strongest 1 being the weakest. (0 filters out 100% of values, 1 filters our 0% of values)
     */
    public Double getPower(FilterType type, Object parameters){
        return -1.0;
    }

    /**
     * @param type the filter type we are getting the cost for
     * @param parameters the parameters we are getting the cost with
     * @return the computational cost of running this filter. larger = more cost
     */
    public Double getCost(FilterType type, Object parameters){
        return 0.0;
    }

    /**
     * @param type the type of filter we are getting the parameters for
     * @param parameters json element that contains the parameters defined by the users
     * @return the Object that will be passed as the parameters to all other filter functions
     */
    public Object buildParameters(FilterType type, JsonElement parameters){
        return null;
    };

    public final String id;

    protected FilterBuilder(String id){
        PipelineBuilder.addFilter(id, this);
        this.id = id;
    }

    public Filter build(JsonElement parameters, FilterType type){
        Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> filter = this.getFilter(type);
        if(filter == null){
            return null;
        }
        return new Filter(filter, this.buildParameters(type, parameters));
    };

    static class Filter implements Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> {
        final Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> filter;
        final Object parameters;

        Filter(
            Function<Quartet<WorkerContext, Long, SeedInfo, Object>, Triplet<WorkerContext, Long, SeedInfo>> filter,
            Object parameters
        ){
            this.filter = filter;
            this.parameters = parameters;
        }

        @Override
        public Triplet<WorkerContext, Long, SeedInfo> apply(Triplet<WorkerContext, Long, SeedInfo> objects) {
            return filter.apply(objects.add(this.parameters));
        }
    }
}