package com.phantomvk.dag.library.utility;

import com.phantomvk.dag.library.meta.Task;
import com.phantomvk.dag.library.meta.TaskDegree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class DagSolver {

    public static List<Task> solve(
            List<Task> tasks,
            Map<Class<? extends Task>, Task> taskMap,
            Map<Class<? extends Task>, List<Class<? extends Task>>> taskChildren) {

        Map<Class<? extends Task>, TaskDegree> degreeMap = new HashMap<>();
        Deque<Class<? extends Task>> queue = new ArrayDeque<>();
        List<Task> sorted = new ArrayList<>();

        for (Task task : tasks) {
            List<Class<? extends Task>> list = task.dependsOn();
            int inDegree = list == null ? 0 : list.size();
            taskChildren.put(task.getClass(), new ArrayList<>());
            taskMap.put(task.getClass(), task);
            degreeMap.put(task.getClass(), new TaskDegree(inDegree));

            if (inDegree == 0) {
                queue.addLast(task.getClass());
            }
        }

        for (Task child : tasks) {
            List<Class<? extends Task>> list = child.dependsOn();
            if (list == null) continue;

            for (Class<? extends Task> ancestor : list) {
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
}
