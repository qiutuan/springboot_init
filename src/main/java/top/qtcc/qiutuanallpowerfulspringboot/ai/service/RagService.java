package top.qtcc.qiutuanallpowerfulspringboot.ai.service;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * RAG 检索增强问答服务
 *
 * @author qiutuan
 */
public interface RagService {

    /**
     * RAG 对话（检索知识库 + 大模型回答）
     *
     * @param question 问题
     * @return 回答
     */
    String chat(String question);

    /**
     * 仅 RAG 检索（返回命中的知识片段，不调用大模型）
     *
     * @param question 问题
     * @return 命中文档列表
     */
    List<Document> retrieve(String question);
}
