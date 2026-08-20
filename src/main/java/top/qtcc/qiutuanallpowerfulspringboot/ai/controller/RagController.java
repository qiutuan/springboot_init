package top.qtcc.qiutuanallpowerfulspringboot.ai.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.qtcc.qiutuanallpowerfulspringboot.ai.service.RagService;
import top.qtcc.qiutuanallpowerfulspringboot.common.BaseResponse;
import top.qtcc.qiutuanallpowerfulspringboot.common.ResultUtils;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.ai.RagRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.vo.ai.RagDocumentVO;

import java.util.List;

/**
 * RAG 检索增强问答接口（登录可见）
 *
 * @author qiutuan
 */
@RestController
@RequestMapping("/api/ai")
@SaCheckLogin
public class RagController {

    @Resource
    private RagService ragService;

    /**
     * RAG 对话（检索知识库 + 大模型回答）
     */
    @PostMapping("/rag/chat")
    public BaseResponse<String> ragChat(@RequestBody @Valid RagRequest request) {
        return ResultUtils.success(ragService.chat(request.getQuestion()));
    }

    /**
     * 仅 RAG 检索（返回命中的知识片段，不调用大模型）
     */
    @PostMapping("/rag/retrieve")
    public BaseResponse<List<RagDocumentVO>> ragRetrieve(@RequestBody @Valid RagRequest request) {
        List<Document> documents = ragService.retrieve(request.getQuestion());
        List<RagDocumentVO> vos = documents.stream().map(doc -> {
            RagDocumentVO vo = new RagDocumentVO();
            vo.setId(doc.getId());
            vo.setContent(truncate(doc.getText(), 500));
            vo.setSource(String.valueOf(doc.getMetadata().getOrDefault("source", "")));
            if (doc.getScore() != null) {
                vo.setScore(doc.getScore().doubleValue());
            }
            return vo;
        }).toList();
        return ResultUtils.success(vos);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
