package top.qtcc.qiutuanallpowerfulspringboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.RequestLog;

/**
 * 请求日志服务接口
 *
 * @author qiutuan
 * @date 2024/12/07
 */
public interface RequestLogService extends IService<RequestLog> {

    /**
     * 异步保存请求日志
     */
    void asyncSave(RequestLog requestLog);

    /**
     * 清理过期日志
     */
    void cleanExpiredLogs();
}
