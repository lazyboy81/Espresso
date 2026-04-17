package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PathNode {

    private String segment;
    private String pathAtThisPoint;
    private PathNode parent;
    private List<PathNode> childSegments;
    private Handler handler;

    @Override
    public String toString() {
        return "PathNode{" +
                "segment='" + segment + '\'' +
                ", pathAtThisPoint='" + pathAtThisPoint + '\'' +
                '}';
    }
}
