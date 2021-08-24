package com.chen.passbook.passbook.controller;

import com.chen.passbook.passbook.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * PassTemplate Token Upload
 *
 * @author Chen on 2021/8/23
 */
@Slf4j
@Controller
public class TokenUploadController {

    /**
     * redis 客户端
     */
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public TokenUploadController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 文件上传页面路由映射
     *
     * @return
     */
    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    /**
     * 上传token
     * @param merchantsId
     * @param passTemplateId
     * @param file
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/token")
    public String tokenFileUpload(@RequestParam("merchantsId") String merchantsId,
                                  @RequestParam("passTemplateId") String passTemplateId,
                                  @RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirectAttributes) {
        if (null == passTemplateId || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message","passTemplate is null or file is empty");
            return "redirect:/uploadStatus";
        }

        try {
            // 先获取token文件的存储路径即：TOKEN_DIR/merchantsId
            // 若不存在，则创建
            File cur = new File(Constants.TOKEN_DIR + merchantsId);
            if(!cur.exists()){
                log.info("Create File: {}", cur.mkdir());
            }
            // 创建 token文件 的完成路径：TOKEN_DIR/merchantsId/passTemplateId
            // 注意：token 有很多条，都保存在 passTemplateId 这个文件中
            Path path = Paths.get(Constants.TOKEN_DIR, merchantsId, passTemplateId);
            // 将用户上传的 token文件 内容写入 path 中
            Files.write(path, file.getBytes());

            // 以 passTemplateId 为 key，path 中存的 token 为 value 存入 redis中
            if (!writeTokenToRedis(path, passTemplateId)){
                redirectAttributes.addFlashAttribute("message","write token error");
            } else {
                redirectAttributes.addFlashAttribute("message", "You successfully uploaded '" + file.getOriginalFilename() + "'");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/uploadStatus";
    }

    /**
     * 文件上传状态展示页面路由映射
     *
     * @return
     */
    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }

    /**
     * 将 token 写入 redis
     *
     * @param path token 文件保存对应的路径 path
     * @param key  redis 的 key
     * @return
     */
    private boolean writeTokenToRedis(Path path, String key) {
        Set<String> tokens;

        // 从 path 路径的文件中读取 token 并保存到 Set 中
        try (Stream<String> stream = Files.lines(path)) {
            tokens = stream.collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // 如果 tokens 不为空，则将其保存到 redis 中
        if (!CollectionUtils.isEmpty(tokens)) {
            // 单机才支持 Pipelined 的方法，集群不支持
            redisTemplate.executePipelined(
                    // 创建连接 需要强转为 RedisCallback 类型
                    (RedisCallback<Object>) redisConnection -> {
                        for (String token : tokens) {
                            // 因为是 Set 集合， 所以要用 sAdd 方法
                            // Set 中 同一个 key 可以有多个 value
                            redisConnection.sAdd(key.getBytes(), token.getBytes());
                        }
                        return null;
                    }
            );
            return true;
        }
        return false;
    }
}
