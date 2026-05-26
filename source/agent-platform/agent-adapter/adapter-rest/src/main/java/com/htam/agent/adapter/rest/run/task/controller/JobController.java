package com.htam.agent.adapter.rest.run.task.controller;

import com.htam.agent.common.config.auth.RoleNeed;
import com.htam.agent.common.enums.Role;
import com.htam.agent.common.r.R;
import com.htam.agent.common.entity.JobInfo;
import com.htam.agent.run.task.core.cluster.JobMessagePublisher;
import com.htam.agent.run.task.service.QuartzInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 描述：web api
 *
 * @author huxuehao
 **/

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class JobController {
    private final QuartzInfoService quartzInfoService;
    private final JobMessagePublisher jobMessagePublisher;

    @GetMapping("list")
    public R<List<JobInfo>> list() {
        return R.data(quartzInfoService.list());
    }

    @PostMapping("/add")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> add(@RequestBody JobInfo jobInfo) throws ClassNotFoundException {
        quartzInfoService.addJob(jobInfo);
        // 广播消息通知其他节点
        jobMessagePublisher.publishAdd(jobInfo);
        return R.data(true);
    }

    @PostMapping("/update")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> update(@RequestBody JobInfo jobInfo) throws ClassNotFoundException {
        quartzInfoService.updateJob(jobInfo);
        // 广播消息通知其他节点
        jobMessagePublisher.publishUpdate(jobInfo);
        return R.data(true);
    }

    @PatchMapping("/updateCron")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> updateCron(@RequestParam("id") String id, @RequestParam("cron") String cron) throws ClassNotFoundException {
        quartzInfoService.updateJobCron(id, cron);
        // 广播消息通知其他节点
        jobMessagePublisher.publishUpdateCron(id, cron);
        return R.data(true);
    }

    @DeleteMapping("/delete")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> delete(@RequestParam("id") String id) throws ClassNotFoundException {
        boolean result = quartzInfoService.deleteJob(id);
        // 广播消息通知其他节点
        jobMessagePublisher.publishDelete(id);
        return R.data(result);
    }

    @PostMapping("/start")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> start(@RequestParam("id") String id) throws ClassNotFoundException {
        quartzInfoService.startJob(id);
        // 广播消息通知其他节点
        jobMessagePublisher.publishStart(id);
        return R.data(true);
    }

    @PostMapping("stop")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> stop(@RequestParam("id") String id) throws ClassNotFoundException {
        quartzInfoService.stopJob(id);
        // 广播消息通知其他节点
        jobMessagePublisher.publishStop(id);
        return R.data(true);
    }

    /**
     * 根据业务ID查询定时任务
     * GET /job/getByBizId
     *
     * @param bizId 业务ID（即agentId）
     * @return 定时任务信息
     */
    @GetMapping("/getByBizId")
    public R<JobInfo> getByBizId(@RequestParam("bizId") String bizId) {
        return R.data(quartzInfoService.getAgentJobByBizId(bizId));
    }

    /**
     * 根据业务ID删除定时任务（用于解绑）
     * GET /job/deleteByBizId
     *
     * @param bizId 业务ID（即agentId）
     * @return 是否删除成功
     */
    @DeleteMapping("/deleteByBizId")
    @RoleNeed({Role.ADMIN, Role.EDIT})
    public R<Boolean> deleteByBizId(@RequestParam("bizId") String bizId) throws ClassNotFoundException {
        JobInfo jobInfo = quartzInfoService.getAgentJobByBizId(bizId);
        if (jobInfo != null) {
            String jobId = jobInfo.getId();
            // 如果任务正在运行，先停止
            if (jobInfo.isEnabled()) {
                quartzInfoService.stopJob(jobId);
            }
            boolean result = quartzInfoService.deleteJob(jobId);
            // 广播消息通知其他节点
            jobMessagePublisher.publishDelete(jobId);
            return R.data(result);
        }
        return R.data(true);
    }
}
