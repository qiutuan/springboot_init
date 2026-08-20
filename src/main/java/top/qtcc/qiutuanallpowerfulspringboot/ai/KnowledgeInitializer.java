package top.qtcc.qiutuanallpowerfulspringboot.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 知识库初始化：启动时从目录（默认 ./data/knowledge，不存在则回退 classpath:/knowledge）
 * 加载 .md/.txt 文件，切片 + embedding 后灌入 VectorStore。
 * 注意：需配置有效的 DASHSCOPE_API_KEY，失败不影响应用启动。
 *
 * @author qiutuan
 */
@Slf4j
@Component
public class KnowledgeInitializer implements ApplicationRunner {

    /**
     * 单个切片目标长度（字符）
     */
    private static final int CHUNK_SIZE = 500;

    private final VectorStore vectorStore;

    @Value("${app.ai.knowledge-path:./data/knowledge}")
    private String knowledgePath;

    public KnowledgeInitializer(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Resource> resources = resolveKnowledgeResources();
            if (resources.isEmpty()) {
                log.warn("知识库目录为空或不存在: {}", knowledgePath);
                return;
            }
            int totalChunks = 0;
            for (Resource resource : resources) {
                String text = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String source = resource.getFilename();
                List<Document> chunks = splitText(text, source);
                vectorStore.add(chunks);
                totalChunks += chunks.size();
                log.info("知识库加载: {} -> {} 个切片", source, chunks.size());
            }
            log.info("知识库初始化完成，共加载 {} 个切片", totalChunks);
        } catch (Exception e) {
            log.error("知识库初始化失败（请检查 DASHSCOPE_API_KEY 与知识库文件）", e);
        }
    }

    /**
     * 解析知识库资源：优先外部目录，其次 classpath
     */
    private List<Resource> resolveKnowledgeResources() throws Exception {
        List<Resource> resources = new ArrayList<>();
        Path dir = Paths.get(knowledgePath);
        if (Files.isDirectory(dir)) {
            try (var stream = Files.list(dir)) {
                stream.filter(p -> isTextFile(p.getFileName().toString()))
                        .forEach(p -> resources.add(new FileSystemResource(p)));
            }
            return resources;
        }
        ClassPathResource classPathResource = new ClassPathResource("knowledge");
        if (classPathResource.exists() && classPathResource.getFile().isDirectory()) {
            File[] files = Objects.requireNonNull(classPathResource.getFile().listFiles());
            for (File file : files) {
                if (isTextFile(file.getName())) {
                    resources.add(new FileSystemResource(file));
                }
            }
        }
        return resources;
    }

    private boolean isTextFile(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".md") || lower.endsWith(".txt");
    }

    /**
     * 简单分片：按空行分段后合并到目标长度（避免引入额外分词依赖）
     */
    private List<Document> splitText(String text, String source) {
        List<Document> docs = new ArrayList<>();
        String[] paragraphs = text.split("\n\n");
        StringBuilder chunk = new StringBuilder();
        for (String paragraph : paragraphs) {
            String p = paragraph.trim();
            if (p.isEmpty()) {
                continue;
            }
            if (chunk.length() + p.length() + 2 > CHUNK_SIZE && chunk.length() > 0) {
                docs.add(new Document(chunk.toString(), Map.of("source", source)));
                chunk = new StringBuilder();
            }
            chunk.append(p).append("\n\n");
        }
        if (chunk.length() > 0) {
            docs.add(new Document(chunk.toString(), Map.of("source", source)));
        }
        return docs;
    }
}
