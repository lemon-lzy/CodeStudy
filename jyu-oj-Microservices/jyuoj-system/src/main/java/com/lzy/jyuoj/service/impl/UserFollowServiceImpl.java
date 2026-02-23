package sspu.zzx.sspuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.mapper.UserMapper;
import sspu.zzx.sspuoj.model.entity.User;
import sspu.zzx.sspuoj.model.entity.UserFollow;
import sspu.zzx.sspuoj.model.vo.user.UserVO;
import sspu.zzx.sspuoj.service.UserFollowService;
import sspu.zzx.sspuoj.mapper.UserFollowMapper;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.service.UserService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ZZX
 * @description 针对表【user_follow】的数据库操作Service实现
 * @createDate 2023-12-12 10:29:40
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService
{

    @Resource
    private UserFollowMapper userFollowMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public List<UserVO> getFollowerList(Long followeeId)
    {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFolloweeId, followeeId);
        List<UserFollow> userFollows = userFollowMapper.selectList(queryWrapper);
        List<Long> followerIdList = userFollows.stream().map(UserFollow::getFollowerId).collect(Collectors.toList());

        return getUserVOByUserIdList(followerIdList);
    }

    @Override
    public List<UserVO> getFolloweeList(Long followerId)
    {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId);
        List<UserFollow> userFollows = userFollowMapper.selectList(queryWrapper);
        List<Long> followeeIdList = userFollows.stream().map(UserFollow::getFolloweeId).collect(Collectors.toList());

        return getUserVOByUserIdList(followeeIdList);
    }

    @Override
    public Boolean follow(Long followerId, Long followeeId)
    {
        // 不能重复关注
        List<UserFollow> userFollows = getUserFollowListByUserId(followerId, followeeId);
        if (userFollows.size() > 0)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "已关注该用户，不能重复关注！");
        }
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFolloweeId(followeeId);
        int insert = userFollowMapper.insert(userFollow);

        return insert > 0;
    }


    @Override
    public Boolean unfollow(Long followerId, Long followeeId)
    {
        // 检查是否存在
        List<UserFollow> userFollows = getUserFollowListByUserId(followerId, followeeId);
        if (userFollows.size() == 0)
        {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "该记录存在！");
        }
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId);
        queryWrapper.eq(UserFollow::getFolloweeId, followeeId);
        int delete = userFollowMapper.delete(queryWrapper);

        return delete > 0;
    }

    public List<UserVO> getUserVOByUserIdList(List<Long> userIdList)
    {
        if (CollectionUtils.isEmpty(userIdList))
        {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, userIdList);
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    public UserVO getUserVO(User user)
    {
        if (user == null)
        {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    public List<UserFollow> getUserFollowListByUserId(Long followerId, Long followeeId)
    {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId);
        queryWrapper.eq(UserFollow::getFolloweeId, followeeId);

        return userFollowMapper.selectList(queryWrapper);
    }

}




