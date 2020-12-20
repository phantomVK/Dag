package com.phantomvk.dag.library.utility;

import com.phantomvk.dag.library.meta.Task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DagSolver {

    /**
     * Sort all elements in list to new order using directed acyclic graph.
     *
     * @param tasks list to sort
     */
    public static void solve(List<Task> tasks) {
        int size = tasks.size();
        Deque<Task> queue = new ArrayDeque<>();
        List<Task> sorted = new ArrayList<>(size);
        Map<Class<? extends Task>, Task> map = new HashMap<>();

        for (Task task : tasks) {
            map.put(task.getClass(), task);
        }

        for (Task task : tasks) {
            List<Class<? extends Task>> parentList = task.dependsOn();
            // This task depends on no task, so add to queue.
            if (parentList == null || parentList.isEmpty()) {
                queue.addLast(task);
                continue;
            }

            // This task depends on at least one parent task.
            for (Class<? extends Task> parentTask : parentList) {
                Task parent = map.get(parentTask);
                if (parent == null) {
                    // Remind developer to add parent class in android.Application.
                    throw new RuntimeException("The class named \""
                            + parentTask.getName()
                            + "\" should be added before starting Dag.");
                } else {
                    // Add this task to parents' children task list.
                    parent.getChildren().add(task);
                }
            }
        }

        while (!queue.isEmpty()) {
            Task task = queue.removeFirst();
            sorted.add(task);
            size--;

            for (Task child : task.getChildren()) {
                if (child.decrementAndGetDegree() == 0) {
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
