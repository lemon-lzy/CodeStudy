package com.lzy.codestudybackend.service.game.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.codestudybackend.mapper.game.UserGameMapper;
import com.lzy.codestudybackend.model.entity.game.UserGame;
import com.lzy.codestudybackend.service.game.UserGameService;
import org.springframework.stereotype.Service;

/**
* @author lzy
* @description 针对表【user_game】的数据库操作Service实现
* @createDate 2023-11-15 14:37:20
*/
@Service
public class UserGameServiceImpl extends ServiceImpl<UserGameMapper, UserGame>
    implements UserGameService {

}




