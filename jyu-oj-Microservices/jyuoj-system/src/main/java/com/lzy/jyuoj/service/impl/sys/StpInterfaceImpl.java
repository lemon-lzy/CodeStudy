package sspu.zzx.sspuoj.service.impl.sys;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2023/11/15 16:45
 */

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sspu.zzx.sspuoj.model.entity.SysRole;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.service.UserService;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 自定义权限加载接口实现类
 */
@Component    // 保证此类被 SpringBoot 扫描，完成 Sa-Token 的自定义权限验证扩展
public class StpInterfaceImpl implements StpInterface
{
    @Autowired
    private UserService userService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType)
    {
        List<String> list = new ArrayList<String>();
        if (loginId != null)
        {
            User user = userService.getById((Serializable) loginId);
            String createRights = user.getCreateRights();
            String[] split = createRights.split(",");
            list.addAll(Arrays.asList(split));
        }
        return list;
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType)
    {
        List<String> list = new ArrayList<String>();
        if (loginId != null)
        {
            User user = userService.getById((Serializable) loginId);
            list.add(user.getUserType());
        }
        return list;
    }

}
