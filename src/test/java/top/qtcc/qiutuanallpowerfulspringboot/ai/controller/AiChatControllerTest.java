package top.qtcc.qiutuanallpowerfulspringboot.ai.controller;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.ai.AiChatRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiChatControllerTest {

    @Resource
    private AiChatController aiChatController;

    @Test
    public void testChat() {

        System.out.println(aiChatController.chat(new AiChatRequest("你好", "1")));
    }

}