package jit.edu.paas.controller;

import jit.edu.paas.commons.util.ResultVOUtils;
import jit.edu.paas.domain.vo.ResultVO;
import jit.edu.paas.service.EnumsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 枚举Controller
 * @author jitwxs
 * @since 2018/6/30 14:45
 */
@RestController
@RequestMapping("/enums")
public class EnumsController {
    @Autowired
    private EnumsService enumsService;

    /**
     * 获取所有状态码
     * @author jitwxs
     * @since 2018/6/30 14:46
     */
    @GetMapping("/result")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listErrorCode() {
        return ResultVOUtils.success(enumsService.listResultCode());
    }

    /**
     * 获取所有系统日志类型
     * @author jitwxs
     * @since 2018/6/30 14:46
     */
    @GetMapping("/log/sys")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listSysLogType() {
        return ResultVOUtils.success(enumsService.listSysLogType());
    }

    /**
     * 获取所有项目日志类型
     * @author jitwxs
     * @since 2018/6/30 14:46
     */
    @GetMapping("/log/project")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listProjectLogType() {
        return ResultVOUtils.success(enumsService.listProjectLogType());
    }

    /**
     * 获取所有仓储类型
     * @author jitwxs
     * @since 2018/6/30 14:46
     */
    @GetMapping("/repository")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listRepositoryType() {
        return ResultVOUtils.success(enumsService.listRepositoryType());
    }

    /**
     * 获取所有镜像类型
     * @author jitwxs
     * @since 2018/6/30 14:46
     */
    @GetMapping("/image")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listImageType() {
        return ResultVOUtils.success(enumsService.listImageType());
    }

    /**
     * 获取所有容器状态
     * @author jitwxs
     * @since 2018/7/1 20:03
     */
    @GetMapping("/container")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listContainerStatus() {
        return ResultVOUtils.success(enumsService.listContainerStatus());
    }
}