package com.lzy.codestudybackend.controller.game;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.codestudybackend.common.BaseResponse;
import com.lzy.codestudybackend.common.ErrorCode;
import com.lzy.codestudybackend.common.ResultUtils;
import com.lzy.codestudybackend.exception.BusinessException;
import com.lzy.codestudybackend.exception.ThrowUtils;
import com.lzy.codestudybackend.model.dto.game.GameAddRequest;
import com.lzy.codestudybackend.model.dto.game.GameQueryRequest;
import com.lzy.codestudybackend.model.dto.game.GameQuestionSubmitRequest;
import com.lzy.codestudybackend.model.entity.game.Game;
import com.lzy.codestudybackend.model.entity.game.UserGame;
import com.lzy.codestudybackend.model.vo.UserVO;
import com.lzy.codestudybackend.model.vo.game.GameInfoVo;
import com.lzy.codestudybackend.model.vo.game.GameQuestionVo;
import com.lzy.codestudybackend.model.vo.game.GameRankDetail;
import com.lzy.codestudybackend.service.game.GameService;
import com.lzy.codestudybackend.service.game.UserGameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


import javax.annotation.Resource;
import java.util.List;

/**
 * @version 1.0
 * @Author ZZX
 * @Date 2024/1/10 10:01
 */
@RestController
@RequestMapping("/game")
@Slf4j
public class GameController
{
    @Resource
    private GameService gameService;
    @Resource
    private UserGameService userGameService;

    /**
     * 分页获取竞赛列表
     * 查询条件包括：公开范围、竞赛名称、竞赛类型、创建者id、创建者名称、开始时间
     *
     * @param gameQueryRequest
     * @return
     */
    @PostMapping("list/page")
    public BaseResponse<Page<Game>> getGameListPage(@RequestBody GameQueryRequest gameQueryRequest)
    {
        long current = gameQueryRequest.getCurrent();
        long size = gameQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Game> gamePage = gameService.page(new Page<>(current, size), gameService.getQueryWrapper(gameQueryRequest));
        return ResultUtils.success(gamePage);
    }

    /**
     * 获得全量列表
     *
     * @param gameQueryRequest
     * @return
     */
    @PostMapping("getAllGames")
    public BaseResponse<List<Game>> getAllGames(@RequestBody GameQueryRequest gameQueryRequest)
    {
        if (gameQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<Game> games = gameService.list(gameService.getQueryWrapper(gameQueryRequest));
        return ResultUtils.success(games);
    }

    /***
     * 判断用户是否在竞赛中
     * @param userId
     * @param gameId
     * @return
     */
    @GetMapping("judgeInGameByUserId")
    public BaseResponse<Boolean> judgeInGameByUserId(@RequestParam("userId") long userId, @RequestParam("gameId") long gameId)
    {
        QueryWrapper<UserGame> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId).eq("gameId", gameId);
        userGameService.getOne(wrapper);
        return ResultUtils.success(userGameService.getOne(wrapper) != null);
    }

    /**
     * 根据userId获得已参加的竞赛
     * 包括自己创建的和额外参加的
     *
     * @param userId
     * @return
     */
    @GetMapping("list/joined")
    public BaseResponse<List<Game>> getJoinedGameList(@RequestParam("userId") long userId)
    {
        List<Game> joinedGames = gameService.getJoinedGameList(userId);
        return ResultUtils.success(joinedGames);
    }

    /**
     * 根据id获取竞赛信息详情
     * 包括竞赛题目数量信息
     *
     * @param id
     * @return
     */
    @GetMapping("info/{id}")
    public BaseResponse<GameInfoVo> getGameInfo(@PathVariable("id") long id)
    {
        return ResultUtils.success(gameService.getGameInfo(id));
    }

    /**
     * 根据id获取竞赛题目列表
     *
     * @param gameId
     * @return
     */
    @GetMapping("problem/list/{gameId}")
    public BaseResponse<List<GameQuestionVo>> getProblemListByGameId(@PathVariable("gameId") long gameId)
    {
        return ResultUtils.success(gameService.getProblemListByGameId(gameId));
    }

    /**
     * 根据id获取竞赛用户列表
     *
     * @param gameId
     * @return
     */
    @GetMapping("user/list/{gameId}")
    public BaseResponse<List<UserVO>> getUserListByGameId(@PathVariable("gameId") long gameId)
    {
        return ResultUtils.success(gameService.getUserListByGameId(gameId));
    }


    /**
     * 创建竞赛
     *
     * @param gameAddRequest
     * @return
     */
    @PostMapping("create")
    @SaCheckPermission(value = {"create.game"}, orRole = "admin")
    public BaseResponse<Long> createGame(@RequestBody GameAddRequest gameAddRequest)
    {

        if (gameAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long gameId = gameService.createGame(gameAddRequest);
        return ResultUtils.success(gameId);
    }

    /**
     * 更新竞赛
     *
     * @param gameAddRequest
     * @return
     */
    @PutMapping("update")
    public BaseResponse<Boolean> updateGame(@RequestBody GameAddRequest gameAddRequest)
    {
        if (gameAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = gameService.updateGame(gameAddRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除竞赛
     * 仅能删除除自己外没人报名的竞赛
     *
     * @param id
     * @return
     */
    @DeleteMapping("delete/{id}")
    public BaseResponse<Boolean> deleteGame(@PathVariable("id") long id)
    {
        return ResultUtils.success(gameService.deleteGame(id));
    }


    /**
     * 报名/取消报名参加竞赛
     *
     * @param gameId
     * @param userId
     * @return
     */
    @PostMapping("joinOrNot")
    public BaseResponse<Boolean> joinOrNot(@RequestParam("gameId") long gameId, @RequestParam("userId") long userId)
    {
        return ResultUtils.success(gameService.joinOrNot(gameId, userId));
    }

    /**
     * 提交题目
     *
     * @param gameQuestionSubmitRequest
     * @return
     */
    @PostMapping("submit")
    public BaseResponse<Long> questionSubmit(@RequestBody GameQuestionSubmitRequest gameQuestionSubmitRequest)
    {
        if (gameQuestionSubmitRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(gameService.questionSubmit(gameQuestionSubmitRequest));
    }

    /**
     * 统计竞赛排行榜信息
     *
     * @param gameId
     * @return
     */
    @GetMapping("getRankByGameId/{gameId}")
    public BaseResponse<List<GameRankDetail>> getRankByGameId(@PathVariable("gameId") long gameId)
    {
        return ResultUtils.success(gameService.getRankByGameId(gameId));
    }

}
