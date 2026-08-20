package top.qtcc.qiutuanallpowerfulspringboot.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.io.FileUtil;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.qtcc.qiutuanallpowerfulspringboot.common.BaseResponse;
import top.qtcc.qiutuanallpowerfulspringboot.common.ResultUtils;
import top.qtcc.qiutuanallpowerfulspringboot.domain.dto.file.UploadFileRequest;
import top.qtcc.qiutuanallpowerfulspringboot.domain.entity.User;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.ErrorCode;
import top.qtcc.qiutuanallpowerfulspringboot.domain.enums.FileUploadBizEnum;
import top.qtcc.qiutuanallpowerfulspringboot.exception.BusinessException;
import top.qtcc.qiutuanallpowerfulspringboot.manager.file.FileManager;
import top.qtcc.qiutuanallpowerfulspringboot.service.UserService;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * 文件接口（流式上传到对象存储，不落本地临时文件）
 *
 * @author qiutuan
 * @date 2024/11/02
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource(name = "fileManager")
    private FileManager fileManager;

    /**
     * 文件上传
     */
    @SaCheckLogin
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           @Valid UploadFileRequest uploadFileRequest) {
        if (uploadFileRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传参数不能为空");
        }
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "业务类型错误");
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser();
        // 文件目录：按业务、用户划分，文件名使用 UUID 前缀防重名
        String originalName = StringUtils.defaultString(multipartFile.getOriginalFilename(), "file");
        String safeName = sanitizeFileName(originalName);
        String filename = UUID.randomUUID().toString().replace("-", "") + "-" + safeName;
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            // 流式上传，避免 createTempFile 路径问题与磁盘 IO
            fileManager.putObject(filepath, inputStream, multipartFile.getSize(), multipartFile.getContentType());
            // 返回可访问地址（由当前启用的存储管理器生成）
            return ResultUtils.success(fileManager.getFileUrl(filepath));
        } catch (Exception e) {
            log.error("file upload error, filepath = {}", filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        }
    }

    /**
     * 清洗文件名：去掉路径分隔符与危险字符，防止路径穿越
     */
    private String sanitizeFileName(String name) {
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"'
                    || c == '<' || c == '>' || c == '|' || Character.isWhitespace(c)) {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        String cleaned = sb.toString().replace("..", "_");
        if (cleaned.length() > 100) {
            cleaned = cleaned.substring(cleaned.length() - 100);
        }
        return cleaned;
    }

    /**
     * 校验文件大小与后缀
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }
        long fileSize = multipartFile.getSize();
        String fileSuffix = FileUtil.getSuffix(StringUtils.defaultString(multipartFile.getOriginalFilename(), ""))
                .toLowerCase(Locale.ROOT);
        
        long maxSize = fileUploadBizEnum.getMaxSize();
        if (fileSize > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 " + (maxSize / (1024 * 1024L)) + "M");
        }
        if (!fileUploadBizEnum.getSuffixWhitelist().contains(fileSuffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
        }
    }
}
