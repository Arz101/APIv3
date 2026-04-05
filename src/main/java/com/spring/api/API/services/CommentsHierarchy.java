package com.spring.api.API.services;

import com.spring.api.API.models.DTOs.Comments.CommentResponse;
import org.jspecify.annotations.NonNull;
import java.util.*;

class C_TreeNode {
    public CommentResponse data;
    public List<C_TreeNode> replies = new ArrayList<>();

    public C_TreeNode(){}
    public C_TreeNode(CommentResponse x){
        this.data = x;
    }
}

public class CommentsHierarchy {

    public CommentsHierarchy(){}

    public List<C_TreeNode> buildHierarchy(@NonNull List<CommentResponse> comments){
        Map<Long, C_TreeNode> nodes = new HashMap<>();
        for (var c : comments){
            nodes.put(c.id(), new C_TreeNode(c));
        }

        List<C_TreeNode> root = new ArrayList<>();
        for (var c : comments){
            if (c.parentId() == null){
                root.add(nodes.get(c.id()));
            } else {
                C_TreeNode parent = nodes.get(c.parentId());
                parent.replies.add(nodes.get(c.id()));
            }
        }
        return root;
    }

    public List<CommentResponse> flattenListByDepth(List<C_TreeNode> roots) {
        List<CommentResponse> flatten = new ArrayList<>();
        if (roots == null) return flatten;

        roots.forEach(r -> {
            flatten.add(r.data);
            flatten.addAll(flattenListByDepth(r.replies));
        });

        return flatten;
    }

    public C_TreeNode findById(List<C_TreeNode> roots, Long id) {
        if(roots == null) return null;
        C_TreeNode target;

        for (var r : roots){
            if (Objects.equals(id, r.data.id())){
                return r;
            }

            target = findById(r.replies, id);
            if (target != null){
                return target;
            }
        }
        return null;
    }
}

