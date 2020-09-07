package com.phantomvk.dag.library.utility;

import com.phantomvk.dag.library.meta.Task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class DagSolver {

    @SuppressWarnings("ConstantConditions")
    public static void solve(List<Task> tasks, Map<Class<? extends Task>, List<Task>> childrenMap) {
        Deque<Task> queue = new ArrayDeque<>();
        List<Task> sorted = new ArrayList<>();

        for (Task task : tasks) {
            if (task.getDegree() == 0) {
                queue.addLast(task);
                continue;
            }

            for (Class<? extends Task> superTask : task.dependsOn()) {
                if (childrenMap.get(superTask) == null) {
                    childrenMap.put(superTask, new ArrayList<>());
                }

                childrenMap.get(superTask).add(task);
            }
        }

        while (!queue.isEmpty()) {
            Task task = queue.removeFirst();
            sorted.add(task);

            List<Task> children = childrenMap.get(task.getClass());
            if (children == null) continue;

            for (Task child : children) {
                if (child.decreaseDegree() == 0) {
                    queue.addLast(child);
                }
            }
        }

        if (sorted.size() != tasks.size()) {
            throw new RuntimeException("A loop was found in the graph.");
        }

        tasks.clear();
        tasks.addAll(sorted);
    }
}
