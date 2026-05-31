package com.luochen.chenoj.service;

import com.luochen.chenoj.model.vo.stat.QuestionPlatformStatsVO;

/**
 * 平台题目数据统计（用户与管理员均可查看）
 */
public interface QuestionStatsService {

    QuestionPlatformStatsVO getPlatformStats();
}
