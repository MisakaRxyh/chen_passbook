package com.chen.passbook.passbook.service;

import com.chen.passbook.passbook.vo.Feedback;
import com.chen.passbook.passbook.vo.Response;

/**
 * 评论功能：即用户评论相关功能实现
 *
 * @author Chen on 2021/8/22
 */
public interface IFeedbackService {

    /**
     * 创建评论
     *
     * @param feedback
     * @return
     */
    Response createFeedback(Feedback feedback);

    /**
     * 根据用户id获取用户评论
     *
     * @param userId
     * @return
     */
    Response getFeedback(Long userId);


}
