package com.mcspeedrun.filter;

import com.google.gson.JsonElement;

import java.util.*;
import java.util.stream.Collectors;

public class Node<T extends FilterBuilder> {
    final T value;
    final List<Node<T>> parents = new ArrayList<>();
    final List<Node<T>> children = new ArrayList<>();

    Node(T value){
        this.value = value;
    }

    public static <T2 extends FilterBuilder> List<T2> flatten(Node<T2> start, Comparator<Node<T2>> sorter){
        List<Node<T2>> list = new ArrayList<>();
        Queue<Node<T2>> queue = new PriorityQueue<>(sorter);

        queue.addAll(start.children);

        while(!queue.isEmpty()){
            Node<T2> next = queue.poll();
            list.add(next);
            for(Node<T2> child : next.children){
                child.removeParent(next);
                if(child.parents.size() == 0){
                    queue.add(child);
                }
            }
        }

        return list.stream().map(a -> a.value).collect(Collectors.toList());
    }

    public void addChild(Node<T> child){
        this.children.add(child);
        child.parents.add(this);
    }

    private void removeParent(Node<T> parent){
        this.parents.remove(parent);
    }

    private Double power = null;
    private Double cost = null;

    public Double getWeight(FilterType type, Map<String, JsonElement> params){
        return this.getCost(type, params) / (1 - this.getPower(type, params));
    }

    public Double getPower(FilterType type, Map<String, JsonElement> params){
        if(this.power == null){
            this.power = children.stream().map((a) -> a.getPower(type, params)).reduce(1.0, (a, b) -> a * b) * Math.abs(this.value.getPower(type, this.value.buildParameters(type, params.get(this.value.id))));
        }
        return this.power;
    }

    // gets the cost to run this filter on its own
    public Double getCost(FilterType type, Map<String, JsonElement> params){
        if(this.cost == null){
            this.cost = children.stream().map((a) -> a.getRelativeCost(type, params)).reduce(0.0, Double::sum) + this.value.getCost(type, params.get(this.value.id));
        }
        return this.cost;
    }

    // gets the cost to run this filter given the cost is split amongst all of its parents
    private Double getRelativeCost(FilterType type, Map<String, JsonElement> params){
        return this.getCost(type, params) / this.parents.size();
    }
}