package com.spring.api.API.services;
import java.util.List;

class HashtagNode {
    public String name;
    public List<HashtagNode> secHashtags;

    public HashtagNode(String name){
        this.name = name;
    }
}

public class HashtagsGraph {

    public HashtagsGraph(){}

    public List<?> buildGraph (){
        return null;
    }
}
