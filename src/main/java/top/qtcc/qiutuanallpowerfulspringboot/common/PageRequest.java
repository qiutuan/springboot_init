package top.qtcc.qiutuanallpowerfulspringboot.common;

import lombok.Data;
import top.qtcc.qiutuanallpowerfulspringboot.constant.CommonConstant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页请求
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    @Min(value = 1, message = "页码不能小于 1")
    private long current = 1;

    /**
     * 页面大小
     */
    @Min(value = 1, message = "分页大小不能小于 1")
    @Max(value = CommonConstant.MAX_PAGE_SIZE, message = "分页大小超出限制")
    private long pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
