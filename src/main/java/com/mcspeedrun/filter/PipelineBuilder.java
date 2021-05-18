package com.mcspeedrun.filter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.javatuples.Triplet;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PipelineBuilder {

    private static final Map<String, FilterBuilder> filters = new HashMap<>();

    public static void addFilter(String id, FilterBuilder filter){
        filters.put(id, filter);
    }

    public static boolean settingsSupported(JsonArray args){
        for(JsonElement arg : args){
            JsonObject obj = arg.getAsJsonObject();
            String id = obj.getAsJsonPrimitive("id").getAsString();
            FilterBuilder filter = filters.get(id);
            if(filter == null){
                return false;
            }
        }
        return true;
    }

    static List<FilterBuilder> buildPipeline(List<FilterBuilder> requirements, Map<String, JsonElement> params, FilterType type){
        // make a copy of the requirements that we can manipulate
        List<FilterBuilder> pipeline = new ArrayList<>(requirements);

        // remove filters that have no filter power from the initial pipeline
        pipeline = pipeline.stream().filter((a) -> a.getPower(type, a.buildParameters(type, params.get(a.id))) != -1).collect(Collectors.toList());

        // add in all dependency filters
        for(int i = 0; i < pipeline.size(); i++){
            List<String> dependencies = pipeline.get(i).getDependencies(type);
            if(dependencies == null){
                continue;
            }
            // try and find each dependency
            dependency_loop: for(String dependency : dependencies){
                for(FilterBuilder subRequirement : pipeline){
                    if(subRequirement.id.equals(dependency)){
                        continue dependency_loop;
                    }
                }
                pipeline.add(filters.get(dependency));
            }
        }

        // convert builders into nodes
        Map<String, Node<FilterBuilder>> nodes = new HashMap<>();
        pipeline.forEach((a) -> {
            nodes.put(a.id, new Node<>(a));
        });

        // construct the nodes into a tree
        Node<FilterBuilder> root = new Node<>(null);
        for(Map.Entry<String, Node<FilterBuilder>> filter : nodes.entrySet()){
            Node<FilterBuilder> node = filter.getValue();
            List<String> dependencies = node.value.getDependencies(type);
            // If we arnt dependent on anything then its a root node
            if(dependencies == null || dependencies.size() == 0){
                root.addChild(node);
            }
            // If we are dependent then add it to the tree
            else {
                for(String dependency : dependencies){
                    nodes.get(dependency).addChild(node);
                }
            }
        }

        // sort by smallest weight first, if we get a tie then sort by the cheapest filter first, if we get a tie then sort by the strongest filter first
        // This looks messy but java is a bitch with types if you don't do it this way so just leave it :)
        Comparator<Node<FilterBuilder>> sorter = Comparator.comparingDouble((node) -> node.getWeight(type, params));
        sorter = sorter.thenComparingDouble((node) -> node.getCost(type, params));
        sorter = sorter.thenComparingDouble((node) -> node.getPower(type, params));

        // Flatten the tree back down into a pipeline
        List<FilterBuilder> pipe = Node.flatten(root, sorter);
        return pipe;
    }

    List<FilterBuilder> structurePipe;
    List<FilterBuilder> biomePipe;
    Map<String, JsonElement> params = new HashMap<>();

    PipelineBuilder(JsonArray args) throws NoSuchElementException {

        // get all of the filters defined by the user along with their parameters
        List<FilterBuilder> pipeline = new ArrayList<>(args.size());
        for(JsonElement arg : args){
            JsonObject obj = arg.getAsJsonObject();
            String id = obj.getAsJsonPrimitive("id").getAsString();
            FilterBuilder filter = filters.get(id);
            if(filter == null){
                throw new NoSuchElementException();
            }
            pipeline.add(filter);
            params.put(id, obj.get("value"));
        }

        structurePipe = buildPipeline(pipeline, params, FilterType.STRUCTURE);
        biomePipe = buildPipeline(pipeline, params, FilterType.BIOME);
    }

    public Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> buildPipe(List<FilterBuilder> pipe, FilterType type){
        // if the pip is empty then just return
        if(pipe.size() == 0){
            return (args) -> args;
        }

        // create the pipeline array
        ArrayList<FilterBuilder.Filter> pipeBuilder = new ArrayList<>(pipe.size());

        // populate the pipeline array
        for(FilterBuilder builder : pipe){
            pipeBuilder.add(builder.build(this.params.get(builder.id), type));
        }

        // remove any null values that build returned
        pipeBuilder.removeAll(Collections.singleton(null));

        // convert it into a faster accessible array
        FilterBuilder.Filter[] pipeline = pipeBuilder.toArray(new FilterBuilder.Filter[0]);

        // return the function for running the array
        return (Triplet<WorkerContext, Long, SeedInfo> last) -> {
            if(last == null){
                return null;
            }
            for(FilterBuilder.Filter filter : pipeline){
                last = filter.apply(last);
                if(last == null){
                    return null;
                }
            }
            return last;
        };
    }

    public Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> buildStructurePipe(){
        return buildPipe(this.structurePipe, FilterType.STRUCTURE);
    }

    public Function<Triplet<WorkerContext, Long, SeedInfo>, Triplet<WorkerContext, Long, SeedInfo>> buildBiomePipe(){
        return buildPipe(this.biomePipe, FilterType.BIOME);
    }
}
