package com.heima.schedule.service.impl;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
class TaskServiceImplTest {
    @Autowired
    private TaskService taskService;

    @Test
    void addTask() {
        for (int i = 0; i < 5; i++) {
            Task task = new Task();
            task.setTaskType(100+i);
            task.setPriority(50);
            task.setExecuteTime(new Date().getTime()+500*i);
            task.setParameters("task test".getBytes());
            long taskId = taskService.addTask(task);
            System.out.println(taskId);
        }
    }

    @Test
    public void cancelTask() {
        taskService.cancelTask(1846815334930153473l);
    }

    @Test
    public void pull() {
        Task task = taskService.pull(100, 50);
        System.out.println(task);
    }
}