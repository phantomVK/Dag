package com.phantomvk.dag.library.utility;

import com.phantomvk.dag.library.meta.Task;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortUtility {

    @SuppressWarnings("ConstantConditions")
    public static List<Task> sort(
            List<Task> tasks,
            Map<Class<? extends Task>, Task> taskMap,
            Map<Class<? extends Task>, List<Class<? extends Task>>> taskChildren) {

        Map<Task, TaskDegree> degreeMap = new HashMap<>();
        Deque<Class<? extends Task>> queue = new ArrayDeque<>();
        List<Task> sorted = new ArrayList<>();

        for (Task task : tasks) {
            int inDegree = task.dependsOn() == null ? 0 : task.dependsOn().size();
            taskChildren.put(task.getClass(), new ArrayList<>());

            if (inDegree == 0) {
                queue.addLast(task.getClass());
            } else {
                degreeMap.put(task, new TaskDegree(inDegree));
            }
        }

        for (Task child : tasks) {
            for (Class<? extends Task> ancestor : child.dependsOn()) {
                taskChildren.get(ancestor).add(child.getClass());
            }
        }

        while (!queue.isEmpty()) {
            Class<? extends Task> ancestor = queue.removeFirst();
            sorted.add(taskMap.get(ancestor));

            for (Class<? extends Task> child : taskChildren.get(ancestor)) {
                TaskDegree childDegree = degreeMap.get(child);
                childDegree.inDegree--;

                if (childDegree.inDegree == 0) {
                    queue.addLast(child);
                }
            }
        }

        if (sorted.size() != tasks.size()) {
            throw new RuntimeException("Loop has found.");
        }

        return sorted;
    }

    private static class TaskDegree {
        public int inDegree;

        TaskDegree(int degree) {
            inDegree = degree;
        }
    }
}
