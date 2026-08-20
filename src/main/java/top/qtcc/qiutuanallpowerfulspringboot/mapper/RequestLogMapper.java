package top.qtcc.qiutuanallpowerfulspringboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.RequestLog;

import java.time.LocalDateTime;

/**
 * 请求日志Mapper接口
 *
 * @author qiutuan
 * @date 2024/12/07
 */
public interface RequestLogMapper extends BaseMapper<RequestLog> {

    /**
     * 删除过期日志
     *
     * @param expireTime 过期时间
     */
    @Delete("DELETE FROM request_log WHERE create_time < #{expireTime}")
    void deleteExpiredLogs(LocalDateTime expireTime);
}
