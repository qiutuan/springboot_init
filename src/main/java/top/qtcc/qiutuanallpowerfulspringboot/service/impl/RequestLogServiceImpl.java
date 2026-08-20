package top.qtcc.qiutuanallpowerfulspringboot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.RequestLog;
import top.qtcc.qiutuanallpowerfulspringboot.mapper.RequestLogMapper;
import top.qtcc.qiutuanallpowerfulspringboot.service.RequestLogService;

import java.time.LocalDateTime;

/**
 * 请求日志服务实现类
 *
 * @author qiutuan
 * @date 2024/12/07
 */
@Slf4j
@Service
public class RequestLogServiceImpl extends ServiceImpl<RequestLogMapper, RequestLog> implements RequestLogService {

    private static final Integer DAY = 30;

    /**
     * 异步保存请求日志
     */
    @Async("asyncExecutor")
    @Override
    public void asyncSave(RequestLog requestLog) {
        try {
            this.save(requestLog);
        } catch (Exception e) {
            log.error("保存请求日志失败", e);
        }
    }

    /**
     * 每天凌晨2点执行清理30天前的日志
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Override
    public void cleanExpiredLogs() {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(DAY);
            this.baseMapper.deleteExpiredLogs(expireTime);
            log.info("清理{}天前的请求日志成功", DAY);
        } catch (Exception e) {
            log.error("清理请求日志失败", e);
        }
    }
}
