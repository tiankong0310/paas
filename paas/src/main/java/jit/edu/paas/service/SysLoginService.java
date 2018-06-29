package jit.edu.paas.service;

import com.baomidou.mybatisplus.service.IService;
import jit.edu.paas.domain.entity.SysLogin;

/**
 * <p>
 * 登陆表 服务类
 * </p>
 *
 * @author jitwxs
 * @since 2018-06-27
 */
public interface SysLoginService extends IService<SysLogin> {
    /**
     * 根据ID获取用户
     * @author jitwxs
     * @since 2018/6/29 16:59
     */
    SysLogin getById(String id);

    /**
     * 根据用户名获取用户
     * @author jitwxs
     * @since 2018/6/27 14:33
     */
    SysLogin getByUsername(String username);

    /**
     * 根据邮件获取用户
     * @author hf
     * @since 2018/6/27 14:33
     */
    SysLogin getByEmail(String email);

    /**
     * 获取权限Id
     * @author jitwxs
     * @since 2018/6/27 17:24
     */
    Integer getRoleId(String username);

    boolean checkPassword(String username, String password);

    /**
     * 保存用户信息至数据库
     * @author hf
     * @since 2018/6/27 14:33
     */
    boolean save(SysLogin sysLogin);

    /**
     * 更新数据库用户信息
     * @author hf
     * @since 2018/6/27 14:33
     */
    int update (SysLogin sysLogin);

    /**
     * 发送注册邮件
     * @author hf
     * @since 2018/6/27 14:33
     */
    Boolean sendRegisterEmail(String email);

    /**
     * 验证注册邮件
     * @author jitwxs
     * @since 2018/6/29 15:58
     */
    Boolean verifyRegisterEmail(String token);

    /**
     * 根据用户名删除
     * @since 2018/6/27 14:33
     */
    void deleteByUsername(String username);

    void deleteById(SysLogin login);

    /**
     * 清理用户缓存
     * @author jitwxs
     * @since 2018/6/29 16:26
     */
    void cleanLoginCache(SysLogin login);
}
