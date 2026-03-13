package com.gdmu.service.impl;

import com.gdmu.mapper.ActivityMapper;
import com.gdmu.mapper.ActivityReportMapper;
import com.gdmu.mapper.ChatGroupMapper;
import com.gdmu.mapper.ParticipantMapper;
import com.gdmu.mapper.UserMapper;
import com.gdmu.pojo.Activity;
import com.gdmu.pojo.Participant;
import com.gdmu.service.ActivityService;
import com.gdmu.service.AIService;
import com.gdmu.service.ChatService;
import com.gdmu.service.SystemNotificationService;
import com.gdmu.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ParticipantMapper participantMapper;

    @Autowired
    private ChatService chatService;

    @Autowired
    private com.gdmu.mapper.ChatGroupMemberMapper chatGroupMemberMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ActivityReportMapper activityReportMapper;

    @Autowired
    private SystemNotificationService systemNotificationService;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private ChatGroupMapper chatGroupMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Activity createActivity(Long creatorId, Integer type, String title, String description, String location, String campus, Double longitude, Double latitude, java.util.Date eventTime, Integer maxPeople) {
        log.info("创建新活动，creatorId: {}, type: {}, title: {}, location: {}, campus: {}, longitude: {}, latitude: {}, eventTime: {}, maxPeople: {}",
                creatorId, type, title, location, campus, longitude, latitude, eventTime, maxPeople);

        try {
            // 验证 creatorId 不为 null
            if (creatorId == null) {
                throw new RuntimeException("用户ID不能为空");
            }

            // 查询创建者信息，获取highCredit值
            var creator = userMapper.selectById(creatorId);
            Integer creatorHighCredit = creator != null && creator.getHighCredit() != null ? creator.getHighCredit() : 0;
            
            Activity activity = new Activity();
            activity.setTitle(title);
            activity.setDescription(description);
            activity.setLocation(location);
            activity.setCampus(campus);
            activity.setLongitude(longitude);
            activity.setLatitude(latitude);
            activity.setEventTime(eventTime);
            activity.setMaxPeople(maxPeople);
            activity.setCurrentPeople(1); // 初始参与人数为1（创建者）
            activity.setStatus(1); // 初始状态为招募中
            activity.setType(type != null ? type : 0); // 默认类型为其他
            activity.setHighCredit(creatorHighCredit); // 设置活动的高信用标识
            activity.setCreatorId(creatorId);

            int rows = activityMapper.insert(activity);
            if (rows <= 0) {
                throw new RuntimeException("创建活动失败");
            }

            // 将创建者添加为参与者
            Participant participant = new Participant();
            participant.setActivityId(activity.getId());
            participant.setUserId(creatorId);
            participant.setStatus(1); // 1-已报名
            
            int participantRows = participantMapper.insert(participant);
            if (participantRows <= 0) {
                throw new RuntimeException("添加参与者失败");
            }

            // 创建活动对应的聊天群
            try {
                var group = chatService.createGroup(activity.getId(), activity.getTitle() + "-聊天群", creatorId);
                // 将创建者添加到聊天群
                chatService.addGroupMember(group.getId(), creatorId);
                log.info("活动聊天群创建成功，activityId: {}, groupId: {}", activity.getId(), group.getId());
            } catch (Exception e) {
                log.warn("创建聊天群失败，不影响活动创建: {}", e.getMessage());
            }

            // 发送活动创建通知
            systemNotificationService.sendActivityCreateNotification(activity.getId(), creatorId, activity.getTitle());
            
            log.info("活动创建成功，eventId: {}，创建者已自动加入", activity.getId());
            return activity;

        } catch (Exception e) {
            log.error("创建活动失败: {}", e.getMessage());
            throw new RuntimeException("创建活动失败: " + e.getMessage());
        }
    }

    @Override
    public Activity getActivityById(Long id) {
        log.info("查询活动信息，eventId: {}", id);
        return activityMapper.selectById(id);
    }

    @Override
    public List<Activity> getAllActivities(int page, int pageSize, String sortBy, String order) {
        log.info("查询所有活动（分页），page: {}, pageSize: {}, sortBy: {}, order: {}", page, pageSize, sortBy, order);
        int offset = (page - 1) * pageSize;
        return activityMapper.selectAllWithPagination(offset, pageSize, sortBy, order);
    }

    @Override
    public List<Activity> getActivitiesByType(Integer type, int page, int pageSize, String sortBy, String order) {
        log.info("查询活动类型（分页）: {}, page: {}, pageSize: {}, sortBy: {}, order: {}", type, page, pageSize, sortBy, order);
        int offset = (page - 1) * pageSize;
        return activityMapper.selectByTypeWithPagination(type, offset, pageSize, sortBy, order);
    }

    @Override
    public int getActivityCount(Integer type) {
        log.info("查询活动总数，type: {}", type);
        return activityMapper.countActivities(type);
    }

    @Override
    public List<Activity> getActivitiesByCreatorId(Long creatorId) {
        log.info("查询用户创建的活动，creatorId: {}", creatorId);
        return activityMapper.selectByCreatorId(creatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivity(Long eventId, String title, String description, String location, String campus, Double longitude, Double latitude, java.util.Date eventTime, Integer maxPeople, Integer type, Integer status) {
        log.info("更新活动信息，eventId: {}", eventId);

        try {
            Activity activity = activityMapper.selectById(eventId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            if (title != null) {
                activity.setTitle(title);
            }
            if (description != null) {
                activity.setDescription(description);
            }
            if (location != null) {
                activity.setLocation(location);
            }
            if (campus != null) {
                activity.setCampus(campus);
            }
            if (longitude != null) {
                activity.setLongitude(longitude);
            }
            if (latitude != null) {
                activity.setLatitude(latitude);
            }
            if (eventTime != null) {
                activity.setEventTime(eventTime);
            }
            if (maxPeople != null) {
                activity.setMaxPeople(maxPeople);
            }
            if (type != null) {
                activity.setType(type);
            }
            if (status != null) {
                activity.setStatus(status);
            }

            int rows = activityMapper.update(activity);
            if (rows <= 0) {
                throw new RuntimeException("更新活动失败");
            }

            log.info("活动更新成功，eventId: {}", eventId);

        } catch (Exception e) {
            log.error("更新活动失败: {}", e.getMessage());
            throw new RuntimeException("更新活动失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteActivity(Long eventId) {
        log.info("删除活动，eventId: {}", eventId);

        try {
            Activity activity = activityMapper.selectById(eventId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            int rows = activityMapper.deleteById(eventId);
            if (rows <= 0) {
                throw new RuntimeException("删除活动失败");
            }

            log.info("活动删除成功，eventId: {}", eventId);

        } catch (Exception e) {
            log.error("删除活动失败: {}", e.getMessage());
            throw new RuntimeException("删除活动失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinActivity(Long activityId, Long userId) {
        log.info("用户加入活动，activityId: {}, userId: {}", activityId, userId);

        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 检查活动是否已满
            if (activity.getCurrentPeople() >= activity.getMaxPeople()) {
                throw new RuntimeException("活动人数已满");
            }

            // 检查用户是否已经参与
            Participant existingParticipant = participantMapper.selectByActivityIdAndUserId(activityId, userId);
            if (existingParticipant != null) {
                throw new RuntimeException("您已参与该活动");
            }

            // 添加用户到参与者列表
            Participant participant = new Participant();
            participant.setActivityId(activityId);
            participant.setUserId(userId);
            participant.setStatus(1); // 1-已报名

            int participantRows = participantMapper.insert(participant);
            if (participantRows <= 0) {
                throw new RuntimeException("加入活动失败");
            }

            // 更新活动的当前参与人数
            int newCurrentPeople = activity.getCurrentPeople() + 1;
            activityMapper.updateCurrentPeople(activityId, newCurrentPeople);

            // 检查是否达到最大人数，如果是，将活动状态改为进行中
            if (newCurrentPeople >= activity.getMaxPeople()) {
                activityMapper.updateStatus(activityId, 2); // 2-进行中
                log.info("活动人数已满，状态变更为进行中，activityId: {}", activityId);
            }

            // 将用户添加到活动的聊天群
            try {
                var group = chatService.getGroupByActivityId(activityId);
                if (group != null) {
                    chatService.addGroupMember(group.getId(), userId);
                    log.info("用户加入活动聊天群成功，groupId: {}, userId: {}", group.getId(), userId);
                }
            } catch (Exception e) {
                log.warn("加入聊天群失败，不影响活动加入: {}", e.getMessage());
            }

            // 发送新成员加入通知
            User newMember = userMapper.selectById(userId);
            if (newMember != null) {
                systemNotificationService.sendNewMemberJoinNotification(activityId, activity.getTitle(), userId, newMember.getNickname());
            }

            log.info("用户加入活动成功，activityId: {}, userId: {}", activityId, userId);

        } catch (Exception e) {
            log.error("用户加入活动失败: {}", e.getMessage());
            throw new RuntimeException("加入活动失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus(Long activityId, Integer status) {
        log.info("更新活动状态，activityId: {}, status: {}", activityId, status);

        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 验证状态值是否合法
            if (status == null || (status != 1 && status != 2 && status != 3 && status != 4)) {
                throw new RuntimeException("无效的活动状态值");
            }

            int rows = activityMapper.updateStatus(activityId, status);
            if (rows <= 0) {
                throw new RuntimeException("更新活动状态失败");
            }

            // 发送活动状态变更通知
            if (status == 3) { // 已结束
                systemNotificationService.sendActivityEndNotification(activityId, activity.getTitle());
                // 更新活动群聊状态为解散
                chatGroupMapper.updateStatusByActivityId(activityId, 2);
                log.info("活动已结束，活动群聊已解散，activityId: {}", activityId);
                
                // 检查参与人数，如果大于2人则创建兴趣群
                List<Participant> participants = participantMapper.selectByActivityId(activityId);
                if (participants.size() > 2) {
                    String interestGroupName = activity.getTitle() + "兴趣群";
                    com.gdmu.pojo.ChatGroup interestGroup = chatService.createInterestGroup(activityId, interestGroupName, activity.getCreatorId());
                    log.info("活动结束，创建兴趣群成功，groupId: {}, 参与人数: {}", interestGroup.getId(), participants.size());
                    
                    // 将所有参与者加入兴趣群
                    for (Participant participant : participants) {
                        chatService.addGroupMember(interestGroup.getId(), participant.getUserId());
                    }
                    log.info("已将{}名参与者加入兴趣群", participants.size());
                } else {
                    log.info("参与人数不足3人，不创建兴趣群，当前人数: {}", participants.size());
                }
            } else if (status == 4) { // 已取消
                systemNotificationService.sendActivityCancelNotification(activityId, activity.getTitle());
                // 更新群聊状态为解散
                chatGroupMapper.updateStatusByActivityId(activityId, 2);
                log.info("活动已取消，群聊已解散，activityId: {}", activityId);
            }

            log.info("活动状态更新成功，activityId: {}, status: {}", activityId, status);

        } catch (Exception e) {
            log.error("更新活动状态失败: {}", e.getMessage());
            throw new RuntimeException("更新活动状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exitActivity(Long activityId, Long userId) {
        log.info("用户退出活动，activityId: {}, userId: {}", activityId, userId);

        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 检查用户是否参与了活动
            Participant existingParticipant = participantMapper.selectByActivityIdAndUserId(activityId, userId);
            if (existingParticipant == null) {
                throw new RuntimeException("您未参与该活动");
            }

            // 从参与者列表中移除用户
            int deleteRows = participantMapper.deleteByActivityIdAndUserId(activityId, userId);
            if (deleteRows <= 0) {
                throw new RuntimeException("退出活动失败");
            }

            // 更新活动的当前参与人数
            int newCurrentPeople = activity.getCurrentPeople() - 1;
            if (newCurrentPeople < 0) newCurrentPeople = 0;
            activityMapper.updateCurrentPeople(activityId, newCurrentPeople);

            // 将用户从活动的聊天群中移除
            log.info("开始处理用户退出聊天群，activityId: {}", activityId);
            var group = chatService.getGroupByActivityId(activityId);
            if (group != null) {
                log.info("找到聊天群，groupId: {}, 准备移除用户: {}", group.getId(), userId);
                try {
                    // 直接使用mapper删除，模仿参与者表的退出逻辑
                    int removeRows = chatGroupMemberMapper.deleteByGroupIdAndUserId(group.getId(), userId);
                    if (removeRows > 0) {
                        log.info("用户退出活动聊天群成功，groupId: {}, userId: {}", group.getId(), userId);
                    } else {
                        log.warn("用户未在聊天群中，groupId: {}, userId: {}", group.getId(), userId);
                    }
                } catch (Exception e) {
                    log.error("退出聊天群失败，错误信息: {}", e.getMessage(), e);
                }
            } else {
                log.warn("未找到活动对应的聊天群，activityId: {}", activityId);
            }

            log.info("用户退出活动成功，activityId: {}, userId: {}", activityId, userId);

        } catch (Exception e) {
            log.error("用户退出活动失败: {}", e.getMessage());
            throw new RuntimeException("退出活动失败: " + e.getMessage());
        }
    }

    @Override
    public List<Activity> getActivitiesByParticipantId(Long userId) {
        log.info("查询用户参与的活动，userId: {}", userId);

        try {
            // 查询用户参与的所有活动ID
            List<Participant> participants = participantMapper.selectByUserId(userId);
            if (participants.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            // 提取活动ID列表
            java.util.List<Long> activityIds = participants.stream()
                    .map(Participant::getActivityId)
                    .collect(java.util.stream.Collectors.toList());

            // 查询活动详情
            java.util.List<Activity> activities = new java.util.ArrayList<>();
            for (Long activityId : activityIds) {
                Activity activity = activityMapper.selectById(activityId);
                if (activity != null) {
                    activities.add(activity);
                }
            }

            return activities;

        } catch (Exception e) {
            log.error("查询用户参与的活动失败: {}", e.getMessage());
            throw new RuntimeException("查询活动失败: " + e.getMessage());
        }
    }

    @Override
    public List<Long> getActivityParticipants(Long activityId) {
        log.info("查询活动的参与者，activityId: {}", activityId);

        try {
            // 查询活动的所有参与者
            List<Participant> participants = participantMapper.selectByActivityId(activityId);
            if (participants.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            // 提取参与者ID列表
            java.util.List<Long> participantIds = participants.stream()
                    .map(Participant::getUserId)
                    .collect(java.util.stream.Collectors.toList());

            return participantIds;

        } catch (Exception e) {
            log.error("查询活动参与者失败: {}", e.getMessage());
            throw new RuntimeException("查询参与者失败: " + e.getMessage());
        }
    }

    @Override
    public List<com.gdmu.pojo.User> getActivityParticipantsInfo(Long activityId) {
        log.info("查询活动的参与者详细信息，activityId: {}", activityId);

        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 查询活动的所有参与者
            List<Participant> participants = participantMapper.selectByActivityId(activityId);
            if (participants.isEmpty()) {
                return java.util.Collections.emptyList();
            }

            // 提取参与者ID列表
            java.util.List<Long> participantIds = participants.stream()
                    .map(Participant::getUserId)
                    .collect(java.util.stream.Collectors.toList());

            // 查询每个参与者的详细信息
            java.util.List<com.gdmu.pojo.User> users = new java.util.ArrayList<>();
            for (Long userId : participantIds) {
                com.gdmu.pojo.User user = userMapper.selectById(userId);
                if (user != null) {
                    users.add(user);
                }
            }

            return users;

        } catch (Exception e) {
            log.error("查询活动参与者详细信息失败: {}", e.getMessage());
            throw new RuntimeException("查询参与者详细信息失败: " + e.getMessage());
        }
    }

    @Override
    public List<Activity> getActivitiesByDistanceAsc(Double longitude, Double latitude, Integer type, int page, int pageSize) {
        log.info("根据距离查询活动（由近及远），longitude: {}, latitude: {}, type: {}, page: {}, pageSize: {}", longitude, latitude, type, page, pageSize);
        int offset = (page - 1) * pageSize;
        return activityMapper.selectByDistanceAsc(longitude, latitude, type, offset, pageSize);
    }

    @Override
    public List<Activity> getActivitiesByDistanceDesc(Double longitude, Double latitude, Integer type, int page, int pageSize) {
        log.info("根据距离查询活动（由远及近），longitude: {}, latitude: {}, type: {}, page: {}, pageSize: {}", longitude, latitude, type, page, pageSize);
        int offset = (page - 1) * pageSize;
        return activityMapper.selectByDistanceDesc(longitude, latitude, type, offset, pageSize);
    }

    @Override
    public int getActivityCountByDistance(Integer type) {
        log.info("查询符合距离条件的活动总数，type: {}", type);
        return activityMapper.countActivitiesByDistance(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportActivity(Long activityId, Long reporterUserId, String reportReason) {
        log.info("举报活动，activityId: {}, reporterUserId: {}, reportReason: {}", activityId, reporterUserId, reportReason);

        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 检查举报理由是否为空
            if (reportReason == null || reportReason.trim().isEmpty()) {
                throw new RuntimeException("举报理由不能为空");
            }

            // 创建举报记录
            com.gdmu.pojo.ActivityReport report = new com.gdmu.pojo.ActivityReport();
            report.setActivityId(activityId);
            report.setReporterUserId(reporterUserId);
            report.setReportReason(reportReason);

            int rows = activityReportMapper.insert(report);
            if (rows <= 0) {
                throw new RuntimeException("举报失败");
            }

            log.info("活动举报成功，reportId: {}", report.getId());

        } catch (Exception e) {
            log.error("举报活动失败: {}", e.getMessage());
            throw new RuntimeException("举报活动失败: " + e.getMessage());
        }
    }

    @Override
    public List<com.gdmu.pojo.ActivityReport> getReportedActivities(int page, int pageSize) {
        log.info("获取被举报活动列表，page: {}, pageSize: {}", page, pageSize);

        try {
            // 验证分页参数
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;

            int offset = (page - 1) * pageSize;
            return activityReportMapper.selectAllWithPagination(offset, pageSize);

        } catch (Exception e) {
            log.error("获取被举报活动列表失败: {}", e.getMessage());
            throw new RuntimeException("获取被举报活动列表失败: " + e.getMessage());
        }
    }

    @Override
    public int getReportedActivityCount() {
        log.info("获取被举报活动总数");
        return activityReportMapper.countReports();
    }

    @Override
    public List<com.gdmu.pojo.ActivityReport> getReportedActivitiesByStatus(Integer handleStatus, int page, int pageSize) {
        log.info("根据处理状态获取被举报活动列表，handleStatus: {}, page: {}, pageSize: {}", handleStatus, page, pageSize);

        try {
            // 验证分页参数
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;

            // 验证处理状态
            if (handleStatus == null || (handleStatus != 0 && handleStatus != 1 && handleStatus != 2)) {
                throw new RuntimeException("无效的处理状态");
            }

            int offset = (page - 1) * pageSize;
            return activityReportMapper.selectByHandleStatus(handleStatus, offset, pageSize);

        } catch (Exception e) {
            log.error("根据处理状态获取被举报活动列表失败: {}", e.getMessage());
            throw new RuntimeException("获取被举报活动列表失败: " + e.getMessage());
        }
    }

    @Override
    public int getReportedActivityCountByStatus(Integer handleStatus) {
        log.info("根据处理状态获取被举报活动总数，handleStatus: {}", handleStatus);

        try {
            // 验证处理状态
            if (handleStatus == null || (handleStatus != 0 && handleStatus != 1 && handleStatus != 2)) {
                throw new RuntimeException("无效的处理状态");
            }

            return activityReportMapper.countReportsByHandleStatus(handleStatus);

        } catch (Exception e) {
            log.error("根据处理状态获取被举报活动总数失败: {}", e.getMessage());
            throw new RuntimeException("获取被举报活动总数失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyReport(Long reportId) {
        log.info("核实举报，reportId: {}", reportId);

        try {
            // 获取举报记录
            com.gdmu.pojo.ActivityReport report = activityReportMapper.selectById(reportId);
            if (report == null) {
                throw new RuntimeException("举报记录不存在");
            }

            // 验证举报是否已处理
            if (report.getHandleStatus() != 0) {
                throw new RuntimeException("该举报已处理");
            }

            // 获取关联的活动
            Long activityId = report.getActivityId();
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 将该活动的所有举报标记为已核实（处理状态设置为1）
            activityReportMapper.updateHandleStatusByActivityId(activityId, 1);

            // 结束活动（状态设置为3-已结束）
            activityMapper.updateStatus(activityId, 3);

            // 获取所有活动参与者
            List<Participant> participants = participantMapper.selectByActivityId(activityId);
            if (!participants.isEmpty()) {
                // 向所有参与者发送系统通知
                String activityTitle = activity.getTitle();
                for (Participant participant : participants) {
                    Long userId = participant.getUserId();
                    systemNotificationService.sendActivityEndNotification(activityId, activityTitle);
                }
            }

            log.info("举报核实成功，活动已结束，reportId: {}, activityId: {}", reportId, activityId);

        } catch (Exception e) {
            log.error("核实举报失败: {}", e.getMessage());
            throw new RuntimeException("核实举报失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectReport(Long reportId) {
        log.info("驳回举报，reportId: {}", reportId);

        try {
            // 获取举报记录
            com.gdmu.pojo.ActivityReport report = activityReportMapper.selectById(reportId);
            if (report == null) {
                throw new RuntimeException("举报记录不存在");
            }

            // 验证举报是否已处理
            if (report.getHandleStatus() != 0) {
                throw new RuntimeException("该举报已处理");
            }

            // 获取关联的活动
            Long activityId = report.getActivityId();
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new RuntimeException("活动不存在");
            }

            // 将该活动的所有举报标记为已驳回（处理状态设置为2）
            activityReportMapper.updateHandleStatusByActivityId(activityId, 2);

            log.info("举报驳回成功，reportId: {}, activityId: {}", reportId, activityId);

        } catch (Exception e) {
            log.error("驳回举报失败: {}", e.getMessage());
            throw new RuntimeException("驳回举报失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void aiSuggestReports() {
        log.info("开始AI建议举报处理");
        
        try {
            List<com.gdmu.pojo.ActivityReport> unsuggestedReports = activityReportMapper.selectUnsuggestedReports();
            log.info("找到{}条未建议的举报记录", unsuggestedReports.size());
            
            if (unsuggestedReports.isEmpty()) {
                log.info("没有需要处理的举报记录");
                return;
            }
            
            List<Long> activityIds = unsuggestedReports.stream()
                    .map(com.gdmu.pojo.ActivityReport::getActivityId)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
            
            List<Activity> activities = new java.util.ArrayList<>();
            for (Long activityId : activityIds) {
                Activity activity = activityMapper.selectById(activityId);
                if (activity != null) {
                    activities.add(activity);
                }
            }
            
            List<com.gdmu.pojo.AIReportSuggestion> suggestions = aiService.analyzeReports(unsuggestedReports, activities);
            
            java.util.Map<Long, String> suggestionMap = new java.util.HashMap<>();
            for (com.gdmu.pojo.AIReportSuggestion suggestion : suggestions) {
                suggestionMap.put(suggestion.getReportId(), suggestion.getReason());
            }
            
            for (com.gdmu.pojo.ActivityReport report : unsuggestedReports) {
                int suggestion = suggestionMap.containsKey(report.getId()) ? 1 : 0;
                activityReportMapper.updateAiSuggestion(report.getId(), suggestion);
                
                if (suggestion == 1) {
                    String reason = suggestionMap.get(report.getId());
                    log.info("AI建议下架举报ID: {}, 活动ID: {}, 审核理由: {}", report.getId(), report.getActivityId(), reason);
                } else {
                    log.info("AI不建议下架举报ID: {}, 活动ID: {}", report.getId(), report.getActivityId());
                }
            }
            
            log.info("AI建议举报处理完成，共处理{}条举报，建议下架{}条", unsuggestedReports.size(), suggestions.size());
            
        } catch (Exception e) {
            log.error("AI建议举报处理失败: {}", e.getMessage());
            throw new RuntimeException("AI建议举报处理失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void aiAutoReview() {
        log.info("开始AI自动审核举报");
        
        try {
            aiSuggestReports();
            
            List<com.gdmu.pojo.ActivityReport> suggestedReports = activityReportMapper.selectSuggestedButUnprocessedReports();
            log.info("找到{}条已建议但未处理的举报记录", suggestedReports.size());
            
            int verifyCount = 0;
            int rejectCount = 0;
            
            for (com.gdmu.pojo.ActivityReport report : suggestedReports) {
                try {
                    if (report.getAiSuggestion() != null && report.getAiSuggestion() == 1) {
                        verifyReport(report.getId());
                        verifyCount++;
                        log.info("AI自动核实举报成功，已下架活动，reportId: {}, activityId: {}", report.getId(), report.getActivityId());
                    } else if (report.getAiSuggestion() != null && report.getAiSuggestion() == 0) {
                        rejectReport(report.getId());
                        rejectCount++;
                        log.info("AI自动驳回举报成功，reportId: {}, activityId: {}", report.getId(), report.getActivityId());
                    }
                } catch (Exception e) {
                    log.error("AI自动审核失败，reportId: {}, error: {}", report.getId(), e.getMessage());
                }
            }
            
            log.info("AI自动审核举报完成，共处理{}条举报，核实{}条，驳回{}条", suggestedReports.size(), verifyCount, rejectCount);
            
        } catch (Exception e) {
            log.error("AI自动审核举报失败: {}", e.getMessage());
            throw new RuntimeException("AI自动审核举报失败: " + e.getMessage());
        }
    }
}