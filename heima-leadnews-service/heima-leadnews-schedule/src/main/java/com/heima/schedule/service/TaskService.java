package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

import java.math.BigInteger;

public interface TaskService {

    public long addTask(Task task);

    public boolean cancelTask(long taskId);

    public Task pull(int type, int priority);
}
