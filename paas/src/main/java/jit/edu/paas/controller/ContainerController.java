package jit.edu.paas.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import jit.edu.paas.commons.convert.UserContainerDTOConvert;
import jit.edu.paas.commons.util.ResultVOUtils;
import jit.edu.paas.commons.util.StringUtils;
import jit.edu.paas.domain.dto.UserContainerDTO;
import jit.edu.paas.domain.entity.UserContainer;
import jit.edu.paas.domain.enums.ContainerStatusEnum;
import jit.edu.paas.domain.enums.ResultEnum;
import jit.edu.paas.domain.enums.RoleEnum;
import jit.edu.paas.domain.vo.ResultVO;
import jit.edu.paas.service.SysLoginService;
import jit.edu.paas.service.UserContainerService;
import jit.edu.paas.service.UserProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 容器Controller
 * @author jitwxs
 * @since 2018/6/28 14:27
 */
@RestController
@RequestMapping("/container")
public class ContainerController {
    @Autowired
    private UserContainerService containerService;
    @Autowired
    private SysLoginService loginService;
    @Autowired
    private UserProjectService projectService;

    @Value("${docker.server.address}")
    private String dockerAddress;
    @Value("${docker.server.port}")
    private String dockerPort;
    @Value("${server.ip}")
    private String serverIp;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private UserContainerDTOConvert dtoConvert;

    /**
     * 获取容器
     * @author jitwxs
     * @since 2018/7/9 22:59
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO getById(@RequestAttribute String uid, @PathVariable String id) {
        ResultVO resultVO = containerService.checkPermission(uid, id);
        if(ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }
        UserContainerDTO containerDTO = containerService.getById(id);

        return ResultVOUtils.success(containerDTO);
    }

    /**
     * 获取容器列表
     * 普通用户获取本人容器，系统管理员获取所有容器
     * @author jitwxs
     * @since 2018/7/9 11:19
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listContainer(@RequestAttribute String uid, Page<UserContainer> page) {
        // 鉴权
        String roleName = loginService.getRoleName(uid);
        // 角色无效
        if(StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }

        Page<UserContainerDTO> selectPage = null;

        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            selectPage = containerService.listContainerByUserId(uid, page);
        } else if(RoleEnum.ROLE_SYSTEM.getMessage().equals(roleName)) {
            selectPage = containerService.listContainerByUserId(null, page);
        }

        return ResultVOUtils.success(selectPage);
    }


    /**
     * 获取项目容器列表
     * 普通用户获取本人项目的容器，系统管理员任意项目的容器
     * @author jitwxs
     * @since 2018/7/1 15:16
     */
    @GetMapping("/list/project/{projectId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO listContainerByProject(@RequestAttribute String uid, @PathVariable String projectId, Page<UserContainer> page) {
        // 1、鉴权
        String roleName = loginService.getRoleName(uid);
        // 1.1、角色无效
        if(StringUtils.isBlank(roleName)) {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }
        // 1.2、越权访问
        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            if(!projectService.hasBelong(projectId, uid)) {
                return ResultVOUtils.error(ResultEnum.PERMISSION_ERROR);
            }
        }

        // 2、查询
        Page<UserContainer> selectPage = containerService.selectPage(page,
                new EntityWrapper<UserContainer>().eq("project_id", projectId));

        return ResultVOUtils.success(dtoConvert.convert(selectPage));
    }

    /**
     * 创建容器
     * @param imageId 镜像ID
     * @param containerName 容器名
     * @param projectId 所属项目
     * @param portMap 端口映射
     * @param cmd 执行命令，如若为空，使用默认的命令
     * @param env 环境变量
     * @destination 容器内部目录
     * @author jitwxs
     * @since 2018/7/1 15:52
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResultVO createContainer(@RequestAttribute String uid, String imageId, String containerName, String projectId,
                                    Map<String,Integer> portMap, String[] cmd, String[] env, String[] destination){
        // 输入验证
        if(StringUtils.isBlank(imageId,containerName,projectId)) {
            return ResultVOUtils.error(ResultEnum.PARAM_ERROR);
        }
        portMap.put("80", 37766);

        return containerService.createContainer(uid, imageId, cmd, portMap, containerName, projectId, env, destination);
    }

    /**
     * 开启容器【异步】
     * @author jitwxs
     * @since 2018/7/1 15:39
     */
    @GetMapping("/start/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO startContainer(@RequestAttribute String uid, @PathVariable String containerId){
        containerService.startContainerTask(uid,containerId);
        return ResultVOUtils.success("容器启动中");
    }

    /**
     * 暂停容器
     * @author jitwxs
     * @since 2018/7/1 16:07
     */
    @GetMapping("/pause/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO pauseContainer(@RequestAttribute String uid, @PathVariable String containerId){
        return containerService.pauseContainer(uid, containerId);
    }

    /**
     * 把容器从暂停状态恢复
     * @author jitwxs
     * @since 2018/7/1 16:09
     */
    @GetMapping("/continue/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO continueContainer(@RequestAttribute String uid, @PathVariable String containerId){
        return containerService.continueContainer(uid, containerId);
    }

    /**
     * 停止容器
     * @author jitwxs
     * @since 2018/7/1 15:59
     */
    @GetMapping("/stop/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO stopContainer(@RequestAttribute String uid, @PathVariable String containerId){
        return containerService.stopContainer(uid, containerId);
    }

    /**
     * 强制停止容器
     * @author jitwxs
     * @since 2018/7/1 16:02
     */
    @GetMapping("/kill/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO killContainer(@RequestAttribute String uid, @PathVariable String containerId){
        return containerService.killContainer(uid, containerId);
    }

    /**
     * 重启容器
     * @author jitwxs
     * @since 2018/7/1 16:02
     */
    @GetMapping("/restart/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO restartContainer(@RequestAttribute String uid, @PathVariable String containerId){
        return containerService.restartContainer(uid, containerId);
    }

    /**
     * 获取运行容器的内部状态
     * @author jitwxs
     * @since 2018/7/1 16:12
     */
    @GetMapping("/top/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO topContainer(@RequestAttribute String uid, @PathVariable String containerId){
        return containerService.topContainer(uid, containerId);
    }

    /**
     * 删除容器
     * @author jitwxs
     * @since 2018/7/1 16:05
     */
    @DeleteMapping("/remove/{containerId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO removeContainer(@RequestAttribute String uid, @PathVariable String containerId){
        return containerService.removeContainer(uid, containerId);
    }

    /**
     * 调用终端
     * @param containerId 容器ID
     * @author jitwxs
     * @since 2018/7/1 14:35
     */
    @PostMapping("/terminal")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO showTerminal(@RequestAttribute String uid, String containerId,
                                 @RequestParam(defaultValue = "false") Boolean cursorBlink,
                                 @RequestParam(defaultValue = "100") Integer cols,
                                 @RequestParam(defaultValue = "50") Integer rows,
                                 @RequestParam(defaultValue = "100") Integer width,
                                 @RequestParam(defaultValue = "50") Integer height) {
        UserContainer container = containerService.getById(containerId);
        if(container == null) {
            return ResultVOUtils.error(ResultEnum.CONTAINER_NOT_FOUND);
        }
        // 只有启动状态容器才能调用Terminal
        if(!containerService.hasEqualStatus(container.getStatus(),ContainerStatusEnum.START)) {
            return ResultVOUtils.error(ResultEnum.CONTAINER_STATUS_REFUSE);
        }

        // 鉴权
        ResultVO resultVO = containerService.checkPermission(uid, containerId);
        if(ResultEnum.OK.getCode() != resultVO.getCode()) {
            return resultVO;
        }

//            String html = "<html>\n" +
//                    "<head>\n" +
//                    "    <meta charset=\"UTF-8\">\n" +
//                    "    <title>xtermjs</title>\n" +
//                    "    <link rel=\"stylesheet\" href=\"/webjars/xterm/2.9.2/dist/xterm.css\" />\n" +
//                    "    <script type=\"application/javascript\" src=\"/webjars/xterm/2.9.2/dist/xterm.js\"></script>\n" +
//                    "    <script type=\"application/javascript\" src=\"/webjars/xterm/2.9.2/dist/addons/attach/attach.js\"></script>\n" +
//                    "</head>\n" +
//                    "<body>\n" +
//                    "<div style=\"width:1000px;\" id=\"xterm\"></div>\n" +
//                    "<script type=\"application/javascript\">\n" +
//                    "    var term = new Terminal({\n" +
//                    "        cursorBlink: false,\n" +
//                    "        cols: 100,\n" +
//                    "        rows: 50\n" +
//                    "    });\n" +
//                    "    term.open(document.getElementById('xterm'));\n" +
//                    "    var socket = new WebSocket('ws://localhost:"+serverPort+"/ws/container/exec?width=100&height=50&ip=" + dockerAddress + "&port=" + dockerPort + "&containerId="+containerId+"');\n" +
//                    "    term.attach(socket);\n" +
//                    "    term.focus();\n" +
//                    "</script>\n" +
//                    "</body>\n" +
//                    "</html>";
        String url = "ws://" + serverIp + ":" + serverPort + "/ws/container/exec?width=" + width + "&height=" + height +
                "&ip=" + dockerAddress + "&port=" + dockerPort + "&containerId=" + containerId;

        Map<String, Object> map = new HashMap<>(16);
        map.put("cursorBlink", cursorBlink);
        map.put("cols", cols);
        map.put("rows", rows);
        map.put("url", url);
        return ResultVOUtils.success(map);
    }

    /**
     * 同步容器状态
     * 普通用户同步本人容器，系统管理员同步所有容器
     * @author jitwxs
     * @since 2018/7/9 13:09
     */
    @GetMapping("/sync")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_SYSTEM')")
    public ResultVO sync(@RequestAttribute String uid) {
        String roleName = loginService.getRoleName(uid);

        if(RoleEnum.ROLE_USER.getMessage().equals(roleName)) {
            return ResultVOUtils.success(containerService.syncStatus(uid));
        } else if(RoleEnum.ROLE_SYSTEM.getMessage().equals(roleName)) {
            return ResultVOUtils.success(containerService.syncStatus(null));
        } else {
            return ResultVOUtils.error(ResultEnum.AUTHORITY_ERROR);
        }
    }
}
