package com.github.sinakarimi81.espresso.routing;

import com.github.sinakarimi81.espresso.handler.Handler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MethodTree {

    private String method;
    private PathNode root;

    public void addRoute(String path, Handler handler) {
        if (path.equals("/")) {
            root.setHandler(handler);
            return;
        }

        if (root.getChildren().isEmpty()) {
            var node = new PathNode(path.substring(root.getFullPath().length() - 1), path, new ArrayList<>(), handler);
            root.getChildren().add(node);
            return;
        }

        var curr = root;
        walk:
        while (true) {
            List<PathNode> children = curr.getChildren();
            for (PathNode child : children) {
                int prefixIndex = longestCommonPrefix(path, child.getFullPath());

                if (prefixIndex == 1) continue;

                if (prefixIndex >= child.getFullPath().length() && prefixIndex < path.length()) {
                    curr = child;
                    continue walk;
                }
            }

            var node = new PathNode(path.substring(curr.getFullPath().length() - 1), path, new ArrayList<>(), handler);
            curr.getChildren().add(node);
            break;
        }

    }

    private int longestCommonPrefix(String a, String b) {
        int result = 0;

        int max = Math.min(a.length(), b.length());
        while (result < max && a.charAt(result) == b.charAt(result)) {
            result = result + 1;
        }

        return result;
    }

}
