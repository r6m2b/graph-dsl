package model;

import lombok.Value;

@Value(staticConstructor = "of")
public class Edge {
    Node start;
    Node end;
    Double weight;
}
