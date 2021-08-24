package com.chen.passbook.passbook.controller;

import com.chen.passbook.passbook.log.LogConstants;
import com.chen.passbook.passbook.log.LogGenerator;
import com.chen.passbook.passbook.service.IFeedbackService;
import com.chen.passbook.passbook.service.IGainPassTemplateService;
import com.chen.passbook.passbook.service.IInventoryService;
import com.chen.passbook.passbook.service.IUserPassService;
import com.chen.passbook.passbook.vo.Feedback;
import com.chen.passbook.passbook.vo.GainPassTemplateRequest;
import com.chen.passbook.passbook.vo.Pass;
import com.chen.passbook.passbook.vo.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Repeatable;

/**
 * Passbook Rest Controller
 *
 * @author Chen on 2021/8/23
 */
@Slf4j
@RestController
@RequestMapping("/passbook")
public class PassbookController {

    /**
     * 用户优惠券服务
     */
    private final IUserPassService userPassService;

    /**
     * 优惠券库存服务
     */
    private final IInventoryService inventoryService;

    /**
     * 领取优惠券服务
     */
    private final IGainPassTemplateService gainPassTemplateService;

    /**
     * 反馈服务
     */
    private final IFeedbackService feedbackService;

    /**
     * HttpServletRequest
     */
    private final HttpServletRequest httpServletRequest;

    @Autowired
    public PassbookController(IUserPassService userPassService,
                              IInventoryService inventoryService,
                              IGainPassTemplateService gainPassTemplateService,
                              IFeedbackService feedbackService,
                              HttpServletRequest httpServletRequest) {
        this.userPassService = userPassService;
        this.inventoryService = inventoryService;
        this.gainPassTemplateService = gainPassTemplateService;
        this.feedbackService = feedbackService;
        this.httpServletRequest = httpServletRequest;
    }

    /**
     * 获取用户个人的优惠券信息
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/userpassinfo")
    Response userPassInfo(Long userId) throws Exception {
        LogGenerator.genLog(
                httpServletRequest, userId, LogConstants.ActionName.USER_PASS_INFO, null);
        return userPassService.getUserPassInfo(userId);
    }

    /**
     * 获取用户使用了的优惠券
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/userusedpassinfo")
    Response userUsedPassInfo(Long userId) throws Exception {
        LogGenerator.genLog(
                httpServletRequest, userId, LogConstants.ActionName.USER_USED_PASS_INFO, null);
        return userPassService.getUserUsedPassInfo(userId);
    }

    /**
     * 用户使用优惠券
     *
     * @param pass
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/userusepass")
    Response userUsePass(@RequestBody Pass pass) throws Exception {
        LogGenerator.genLog(
                httpServletRequest, pass.getUserId(), LogConstants.ActionName.USER_USE_PASS, null);
        return userPassService.userUsePass(pass);
    }

    /**
     * 获取库存信息
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/inventoryinfo")
    Response inventoryInfo(Long userId) throws Exception {
        LogGenerator.genLog(
                httpServletRequest, userId, LogConstants.ActionName.INVENTORY_INFO, null);
        return inventoryService.getInventoryInfo(userId);
    }

    /**
     * 用户领取优惠券
     *
     * @param request
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/gainpasstemplate")
    Response gainPassTemplate(@RequestBody GainPassTemplateRequest request) throws Exception {
        LogGenerator.genLog(
                httpServletRequest, request.getUserId(), LogConstants.ActionName.GAIN_PASS_TEMPLATE, null);
        return gainPassTemplateService.gainPassTemplate(request);
    }

    /**
     * 用户创建评论
     * @param feedback
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/createfeedback")
    Response createFeedback(@RequestBody Feedback feedback) throws Exception {
        LogGenerator.genLog(
                httpServletRequest, feedback.getUserId(), LogConstants.ActionName.CREATE_FEEDBACK, null);
        return feedbackService.createFeedback(feedback);
    }

    /**
     * 用户获取评论
     * @param userId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("getfeedback")
    Response getFeedback(Long userId) throws Exception {
        LogGenerator.genLog(
                httpServletRequest, userId, LogConstants.ActionName.GET_FEEDBACK, null);
        return feedbackService.getFeedback(userId);
    }

    /**
     * 异常演示接口
     * @return
     * @throws Exception
     */
    @ResponseBody
    @GetMapping("/exception")
    Response exception()throws Exception{
        throw new Exception("Welcome To Chen Passbook");
    }
}
