package interpreter;

import lombok.Data;
import model.Demo;
import model.Edge;
import model.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: IF edge from a->b represents an undirected edge a<->b then treat it that way
// Easiest way to is to just make b->a for each a->b in the ctor

@Data
public class KruskalSnapshot implements Snapshot {
    /*
        Kruskal's algorithm:
            tl;dr Keep adding the lowest cost edge that connects vertices until all vertices are connected
    */
    // Each node is mapped to its own tree, i.e. the treeOfNode
    // Joining two trees sets them to have the same int
    Map<Node, Integer> nodeToTree = new HashMap<>(); // Each node starts as its own tree, numbered from 0
    List<List<Node>> treeToNodes = new ArrayList<>(); // Mapping of tree index back to a list of nodes. List index is tree number.
    List<Edge> chosenEdges = new ArrayList<>(); // What eventually becomes the minimum spanning tree of the graph
    List<Edge> remainingEdges = new ArrayList<>();
    int numTrees = 0;
    boolean canContinue = true;

    public KruskalSnapshot(Demo demo) {
        demo.getGraph().getNodes().forEach(node -> {
            this.nodeToTree.put(node, numTrees++);
            List<Node> tree = new ArrayList<>();
            tree.add(node);
            this.trees.add(tree);
        });
        demo.getGraph().getEdges().forEach(edge -> {
            this.remainingEdges.add(edge);
        });
    }

    public boolean isOver() {
        return this.numTrees <= 1 || this.remainingEdges.size() == 0 || !this.canContinue;
    }

    /**
     * Returns whether the edge joins two trees (start and end node are in different trees)
     */
    private boolean joinsTwoTrees(Edge edge) {
        Node start = edge.getStart();
        Node end = edge.getEnd();
        int startTree = nodeToTree(start);
        int endTree = nodeToTree(end);
        return startTree != endTree;
    }


    /**
     * Take a step in Kruskal's algorithm, greedily joining two trees
     */
    public void step() {
        Edge nextEdge = getNextEdge();
        if (nextEdge == null) {
            this.canContinue = false;
        } else {
            joinTreesByEdge(nextEdge);
        }
    }

    /**
     * Modify internal state to reflect joining of two trees
     */
    private void joinTreesByEdge(Edge edge){
        // Add the edge to the chosenEdges
        this.chosenEdges.add(edge);
        // Transplant the end node's tree into the start node's tree
        Node start = edge.getStart();
        Node end = edge.getEnd();
        int startTree = this.nodeToTree(start);
        int endTree = this.nodeToTree(end);
        List<Node> endTreeNodes = treeToNodes.get(endTree); // the nodes to transplant
        // Update the node -> tree mapping for the end nodes
        for (Node node : endTreeNodes) {
            this.nodeToTree.update(node, startTree);
        }
        // Update the tree -> nodes mapping by dumping the end nodes into the start node's tree
        List<Node> startTreeNodes = treeToNodes.get(startTree);
        for (Node node : endTreeNodes) {
            startTreeNodes.add(node);
        }
        // Dump the list containing the old end node's tree
        this.treeToNodes.set(endTree, new ArrayList<>());
        // Update tree counter
        --this.numTrees;
    }

    private Edge getNextEdge() {
        Edge minEdge = null;
        double minLength = Double.POSITIVE_INFINITY;
        for (Edge edge : this.remainingEdges) {
            if (!joinsTwoTrees(edge)) {
                continue;
            }
            if (edge.getWeight() < minLength) {
                minEdge = edge;
                minLength = edge.getWeight();
            }
        }
    }
}