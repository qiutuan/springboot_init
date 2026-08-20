package top.qtcc.qiutuanallpowerfulspringboot.ai.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import top.qtcc.qiutuanallpowerfulspringboot.ai.advisor.LoggingAdvisor;
import top.qtcc.qiutuanallpowerfulspringboot.ai.advisor.PromptInjectionProtectionAdvisor;
import top.qtcc.qiutuanallpowerfulspringboot.ai.service.RagService;

import java.util.List;

/**
 * RAG 服务实现：
 * - RetrievalAugmentationAdvisor + VectorStoreDocumentRetriever：检索知识库并注入上下文回答
 * - retrieve：直接调用向量库相似度检索（不调用大模型）
 *
 * @author qiutuan
 */
@Service
public class RagServiceImpl implements RagService {

    private final VectorStore vectorStore;
    private final ChatClient ragChatClient;

    public RagServiceImpl(VectorStore vectorStore, ChatModel chatModel) {
        this.vectorStore = vectorStore;
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .topK(3)
                .similarityThreshold(0.3)
                .build();
        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .build();
        this.ragChatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(
                        new PromptInjectionProtectionAdvisor(),
                        new LoggingAdvisor(),
                        ragAdvisor
                )
                .build();
    }

    @Override
    public String chat(String question) {
        return ragChatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    @Override
    public List<Document> retrieve(String question) {
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(3)
                .build());
    }
}
