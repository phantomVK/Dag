package com.phantomvk.dag.library.utility;

import com.phantomvk.dag.library.meta.CommonTask;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortUtility {

    @SuppressWarnings("ConstantConditions")
    public static List<CommonTask> sort(List<CommonTask> tasks,
                                        Map<CommonTask, List<CommonTask>> taskChildren) {

        int size = tasks.size();
        Map<CommonTask, TaskDegree> degreeMap = new HashMap<>();
        Deque<CommonTask> queue = new ArrayDeque<>();
        List<CommonTask> sorted = new ArrayList<>();

        for (CommonTask task : tasks) {
            int inDegree = task.dependsOn() == null ? 0 : task.dependsOn().size();
            taskChildren.put(task, new ArrayList<>());

            if (inDegree == 0) {
                queue.addLast(task);
            } else {
                degreeMap.put(task, new TaskDegree(inDegree));
            }
        }

        for (CommonTask child : tasks) {
            for (CommonTask ancestor : child.dependsOn()) {
                taskChildren.get(ancestor).add(child);
            }
        }

        while (!queue.isEmpty()) {
            CommonTask ancestor = queue.removeFirst();
            sorted.add(ancestor);

            for (CommonTask child : taskChildren.get(ancestor)) {
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
