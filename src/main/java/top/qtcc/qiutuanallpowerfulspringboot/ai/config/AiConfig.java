package top.qtcc.qiutuanallpowerfulspringboot.ai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置：
 * - VectorStore 使用 SimpleVectorStore（内存实现，启动时由 KnowledgeInitializer 灌入知识库，
 *   生产环境可替换为 Redis/PGVector/ES 等持久化向量库）
 *
 * @author qiutuan
 */
@Configuration
public class AiConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
