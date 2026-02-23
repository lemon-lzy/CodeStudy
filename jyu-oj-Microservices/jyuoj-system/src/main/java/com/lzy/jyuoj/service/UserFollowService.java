package sspu.zzx.sspuoj.service;

import sspu.zzx.sspuoj.model.entity.UserFollow;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.vo.user.UserVO;

import java.util.List;

/**
 * @author ZZX
 * @description 针对表【user_follow】的数据库操作Service
 * @createDate 2023-12-12 10:29:40
 */
public interface UserFollowService extends IService<UserFollow>
{
    /**
     * 根据用户id获取粉丝列表
     *
     * @param followeeId
     * @return
     */
    List<UserVO> getFollowerList(Long followeeId);

    /**
     * 根据用户id获取关注列表
     *
     * @param followerId
     * @return
     */
    List<UserVO> getFolloweeList(Long followerId);

    /**
     * 关注
     *
     * @param followerId
     * @param followeeId
     * @return
     */
    Boolean follow(Long followerId, Long followeeId);

    /**
     * 取关
     *
     * @param followerId
     * @param followeeId
     * @return
     */
    Boolean unfollow(Long followerId, Long followeeId);

}
