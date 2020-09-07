package com.phantomvk.dag.library.utility;

import com.phantomvk.dag.library.meta.Task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DagSolver {

    @SuppressWarnings("ConstantConditions")
    public static void solve(List<Task> tasks) {
        int size = tasks.size();
        Deque<Task> queue = new ArrayDeque<>();
        List<Task> sorted = new ArrayList<>(size);
        Map<Class<? extends Task>, Task> map = new HashMap<>();

        for (Task task : tasks) {
            map.put(task.getClass(), task);
        }

        for (Task task : tasks) {
            if (task.getDegree() == 0) {
                queue.addLast(task);
                continue;
            }

            for (Class<? extends Task> prevTask : task.dependsOn()) {
                map.get(prevTask).getChildren().add(task);
            }
        }

        while (!queue.isEmpty()) {
            Task task = queue.removeFirst();
            sorted.add(task);
            size--;

            for (Task child : task.getChildren()) {
                if (child.decreaseDegree() == 0) {
                    queue.addLast(child);
                }
            }
        }

        if (size != 0) {
            throw new RuntimeException("A loop was found in the graph.");
        }

        tasks.clear();
        tasks.addAll(sorted);
    }
}
