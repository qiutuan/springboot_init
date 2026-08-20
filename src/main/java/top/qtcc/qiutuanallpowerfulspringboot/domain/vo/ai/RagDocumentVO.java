package top.qtcc.qiutuanallpowerfulspringboot.domain.vo.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * RAG 检索结果视图
 *
 * @author qiutuan
 */
@Data
public class RagDocumentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档 ID
     */
    private String id;

    /**
     * 命中文档内容（截断）
     */
    private String content;

    /**
     * 来源文件
     */
    private String source;

    /**
     * 相似度得分
     */
    private Double score;
}
