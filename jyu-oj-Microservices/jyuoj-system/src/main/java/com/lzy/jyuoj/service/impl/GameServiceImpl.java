package sspu.zzx.sspuoj.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sspu.zzx.sspuoj.common.ErrorCode;
import sspu.zzx.sspuoj.constant.CommonConstant;
import sspu.zzx.sspuoj.exception.BusinessException;
import sspu.zzx.sspuoj.mapper.GameMapper;
import sspu.zzx.sspuoj.mapper.GameQuestionMapper;
import sspu.zzx.sspuoj.mapper.GameRankMapper;
import sspu.zzx.sspuoj.mapper.UserGameMapper;
import sspu.zzx.sspuoj.model.dto.game.*;
import sspu.zzx.sspuoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import sspu.zzx.sspuoj.model.entity.*;
import sspu.zzx.sspuoj.model.enums.JudgeInfoMessageEnum;
import sspu.zzx.sspuoj.model.judge.model.JudgeInfo;
import sspu.zzx.sspuoj.model.vo.game.GameInfoVo;
import sspu.zzx.sspuoj.model.vo.game.GameQuestionVo;
import sspu.zzx.sspuoj.model.vo.game.GameRankDetail;
import sspu.zzx.sspuoj.model.vo.question.QuestionVO;
import sspu.zzx.sspuoj.model.vo.user.UserVO;
import sspu.zzx.sspuoj.service.GameService;
import sspu.zzx.sspuoj.service.QuestionService;
import sspu.zzx.sspuoj.service.QuestionSubmitService;
import sspu.zzx.sspuoj.service.UserService;
import sspu.zzx.sspuoj.utils.SqlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author ZZX
 * @description 针对表【game】的数据库操作Service实现
 * @createDate 2023-11-15 14:36:13
 */
@Service
public class GameServiceImpl extends ServiceImpl<GameMapper, Game> implements GameService
{
    @Resource
    private GameMapper gameMapper;
    @Resource
    private UserGameMapper userGameMapper;
    @Resource
    private GameQuestionMapper gameQuestionMapper;
    @Resource
    private QuestionService questionService;
    @Resource
    private UserService userService;
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private GameRankMapper gameRankMapper;

    @Override
    public QueryWrapper<Game> getQueryWrapper(GameQueryRequest gameQueryRequest)
    {
        if (gameQueryRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = gameQueryRequest.getId();
        String gameName = gameQueryRequest.getGameName();
        Long gameFounderId = gameQueryRequest.getGameFounderId();
        String gameFounderName = gameQueryRequest.getGameFounderName();
        String publicZone = gameQueryRequest.getPublicZone();
        String gameType = gameQueryRequest.getGameType();
        String startTime = gameQueryRequest.getStartTime();
        String endTime = gameQueryRequest.getEndTime();
        String sortField = gameQueryRequest.getSortField();
        String sortOrder = gameQueryRequest.getSortOrder();

        QueryWrapper<Game> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(gameName), "gameName", gameName);
        queryWrapper.eq(gameFounderId != null, "gameFounderId", gameFounderId);
        queryWrapper.like(StringUtils.isNotBlank(gameFounderName), "gameFounderName", gameFounderName);
        queryWrapper.eq(StringUtils.isNotBlank(publicZone), "publicZone", publicZone);
        queryWrapper.eq(StringUtils.isNotBlank(gameType), "gameType", gameType);
        if (!StringUtils.isAnyBlank(startTime, endTime))
        {
            queryWrapper.between("startTime", startTime, endTime);
        }
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        return queryWrapper;
    }

    @Override
    public List<Game> getJoinedGameList(long userId)
    {
        // 获取用户参与的竞赛id列表
        QueryWrapper<UserGame> userGameWrapper = new QueryWrapper<>();
        userGameWrapper.eq("userId", userId);
        List<UserGame> userGameList = userGameMapper.selectList(userGameWrapper);
        if (CollectionUtils.isEmpty(userGameList))
        {
            return new ArrayList<>();
        }
        List<Long> userGameIds = userGameList.stream().map(UserGame::getGameId).distinct().collect(Collectors.toList());

        // 获取竞赛列表
        List<Game> games = gameMapper.selectBatchIds(userGameIds);

        return games;
    }

    @Override
    public GameInfoVo getGameInfo(long id)
    {
        Game game = gameMapper.selectById(id);
        GameInfoVo gameInfoVo = new GameInfoVo();
        BeanUtils.copyProperties(game, gameInfoVo);

        // 获取用户参与的竞赛id列表
        QueryWrapper<UserGame> userGameWrapper = new QueryWrapper<>();
        userGameWrapper.eq("gameId", id);
        List<UserGame> userGameList = userGameMapper.selectList(userGameWrapper);
        // 设置竞赛题目的数量
        gameInfoVo.setQuestionNum(userGameList.size());

        return gameInfoVo;
    }

    @Override
    public List<GameQuestionVo> getProblemListByGameId(long gameId)
    {
        // 获得竞赛题目id列表
        QueryWrapper<GameQuestion> gameQuestionWrapper = new QueryWrapper<>();
        gameQuestionWrapper.eq("gameId", gameId);
        List<GameQuestion> gameQuestionList = gameQuestionMapper.selectList(gameQuestionWrapper);
        if (gameQuestionList.isEmpty())
        {
            return new ArrayList<>();
        }
        List<Long> questionIds = gameQuestionList.stream().map(GameQuestion::getQuestionId).collect(Collectors.toList());
        List<Question> questionList = questionService.listByIds(questionIds);
        // 获得竞赛题目信息列表
        List<QuestionVO> questionVOList = questionService.getQuestionVOList(questionList, false);
        // 组装题目信息
        List<GameQuestionVo> gameQuestionVoList = new ArrayList<>();
        for (int i = 0; i < gameQuestionList.size(); i++)
        {
            GameQuestion gameQuestion = gameQuestionList.get(i);
            GameQuestionVo gameQuestionVo = new GameQuestionVo();
            gameQuestionVo.setQuestionVO(questionVOList.get(i));
            gameQuestionVo.setFullScore(gameQuestion.getFullScore());
            gameQuestionVoList.add(gameQuestionVo);
        }

        return gameQuestionVoList;
    }

    @Override
    public Long createGame(GameAddRequest gameAddRequest)
    {
        // 参数校验
        validAddRequest(gameAddRequest);
        // 创建竞赛
        Game game = new Game();
        BeanUtils.copyProperties(gameAddRequest, game);
        // 设置竞赛时间范围
        game.setStartTime(DateUtil.parse(gameAddRequest.getStartTime()));
        game.setEndTime(DateUtil.parse(gameAddRequest.getEndTime()));
        // 设置作者信息
        User loginUser = userService.getLoginUser(null);
        game.setGameFounderId(loginUser.getId());
        game.setGameFounderName(loginUser.getUserName());
        // 设置竞赛人数信息
        game.setGameCurrentNum(1);
        // 设置竞赛题目信息
        int insert = gameMapper.insert(game);
        Long gameId = game.getId();
        if (insert > 0)
        {
            List<Long> questionIdList = gameAddRequest.getQuestionIdList();
            List<Integer> questionFullScoreList = gameAddRequest.getQuestionFullScoreList();
            insertGameQuestion(gameId, questionIdList, questionFullScoreList);
        }
        // 新增竞赛成员
        UserGame userGame = new UserGame();
        userGame.setGameId(gameId);
        userGame.setUserId(loginUser.getId());
        userGameMapper.insert(userGame);
        return gameId;
    }

    @Override
    public boolean updateGame(GameAddRequest gameAddRequest)
    {
        // 竞赛id不为空
        if (gameAddRequest.getId() == null || gameAddRequest.getId() <= 0)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 参数校验
        validAddRequest(gameAddRequest);
        // 不能在竞赛开始后修改
        String startTime = gameAddRequest.getStartTime();
        if (DateUtil.parse(startTime).isBefore(new Date()))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "竞赛开始时间已过，无法修改");
        }
        // 更新基本信息
        Game game = new Game();
        BeanUtils.copyProperties(gameAddRequest, game);
        gameMapper.updateById(game);
        // 更新题目信息：删除原有的，然后重新添加
        Long gameId = game.getId();
        QueryWrapper<GameQuestion> gameQuestionQueryWrapper = new QueryWrapper<>();
        gameQuestionQueryWrapper.eq("gameId", gameId);
        // 删除
        gameQuestionMapper.delete(gameQuestionQueryWrapper);
        // 新增
        insertGameQuestion(gameId, gameAddRequest.getQuestionIdList(), gameAddRequest.getQuestionFullScoreList());
        return true;
    }

    public void insertGameQuestion(Long gameId, List<Long> questionIdList, List<Integer> questionFullScoreList)
    {
        for (int i = 0; i < questionIdList.size(); i++)
        {
            Long questionId = questionIdList.get(i);
            Integer fullScore = questionFullScoreList.get(i);
            GameQuestion gameQuestion = new GameQuestion();
            gameQuestion.setGameId(gameId);
            gameQuestion.setQuestionId(questionId);
            gameQuestion.setFullScore(fullScore);
            gameQuestionMapper.insert(gameQuestion);
        }
    }

    @Override
    public boolean deleteGame(long id)
    {
        // 已有人参加或已开始的竞赛无法删除
        Game game = gameMapper.selectById(id);
        if (game == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        if (game.getGameCurrentNum() > 1)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前已有其他用户加入此竞赛，无法删除");
        }
        Date startTime = game.getStartTime();
        if (startTime == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        if (startTime.before(new Date()))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前竞赛已开始，无法删除");
        }
        // 删除竞赛
        gameMapper.deleteById(id);
        QueryWrapper<GameQuestion> gameQuestionQueryWrapper = new QueryWrapper<>();
        // 删除竞赛题目
        gameQuestionQueryWrapper.eq("gameId", id);
        gameQuestionMapper.delete(gameQuestionQueryWrapper);
        // 删除竞赛成员
        QueryWrapper<UserGame> userGameQueryWrapper = new QueryWrapper<>();
        userGameQueryWrapper.eq("gameId", id);
        userGameMapper.delete(userGameQueryWrapper);
        return true;
    }

    @Override
    public boolean joinOrNot(long gameId, long userId)
    {
        Game game = gameMapper.selectById(gameId);
        if (game == null)
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛不存在");
        }
        // 查找是否已经加入
        QueryWrapper<UserGame> userGameQueryWrapper = new QueryWrapper<>();
        userGameQueryWrapper.eq("gameId", gameId).eq("userId", userId);
        UserGame userGame = userGameMapper.selectOne(userGameQueryWrapper);
        // 已经加入就取消
        if (userGame != null)
        {
            // 创建者不能退出
            if (game.getGameFounderId().equals(userId))
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建者不能退出");
            }
            // 比赛开始后不能退出
            if (game.getStartTime().before(new Date()))
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛开始后不能退出");
            }
            userGameMapper.deleteById(userGame.getId());
            // 更新当前人数
            game.setGameCurrentNum(game.getGameCurrentNum() - 1);
            gameMapper.updateById(game);
        }
        // 未加入就加入
        else
        {
            // 判断是否到达人数上限
            if (game.getGameCurrentNum() >= game.getGameTotalNum())
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛人数已满");
            }
            // 比赛开始后不能加入
            if (game.getStartTime().before(new Date()))
            {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛开始后不能加入");
            }
            userGame = new UserGame();
            userGame.setUserId(userId);
            userGame.setGameId(gameId);
            userGameMapper.insert(userGame);
            // 更新当前人数
            game.setGameCurrentNum(game.getGameCurrentNum() + 1);
            gameMapper.updateById(game);
        }
        return true;
    }

    @Override
    public Long questionSubmit(GameQuestionSubmitRequest gameQuestionSubmitRequest)
    {
        QuestionSubmitAddRequest questionSubmitAddRequest = gameQuestionSubmitRequest.getQuestionSubmitAddRequest();
        Long gameId = gameQuestionSubmitRequest.getGameId();
        if (questionSubmitAddRequest == null || gameId == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Game game = gameMapper.selectById(gameId);
        if (game == null)
        {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "竞赛不存在");
        }
        Date startTime = game.getStartTime();
        Date endTime = game.getEndTime();
        // 判断竞赛是否已经开始
        Date currentDate = new Date();
        if (startTime.after(currentDate))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛未开始");
        }
        // 判断竞赛是否已经结束
        if (endTime.before(currentDate))
        {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "竞赛已结束");
        }
        final User loginUser = userService.getLoginUser(null);
        long submitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        // 同时异步更新排行榜信息
        CompletableFuture.runAsync(() ->
        {
            // 更新用户提交成绩信息
            QuestionSubmit nowSubmit = questionSubmitService.getById(submitId);
            // 获得该竞赛各题目的满分
            QueryWrapper<GameQuestion> gameQuestionQueryWrapper = new QueryWrapper<>();
            gameQuestionQueryWrapper.eq("gameId", gameId);
            List<GameQuestion> gameQuestionList = gameQuestionMapper.selectList(gameQuestionQueryWrapper);
            Map<Long, Integer> questionIdToFullScore = gameQuestionList.stream().collect(Collectors.toMap(GameQuestion::getQuestionId, GameQuestion::getFullScore));
            // 获得当前用户在当前竞赛中的提交信息
            QueryWrapper<GameRank> gameRankQueryWrapper = new QueryWrapper<>();
            gameRankQueryWrapper.eq("userId", loginUser.getId()).eq("gameId", gameId);
            GameRank gameRank = gameRankMapper.selectOne(gameRankQueryWrapper);
            // 为空新建一个gameRank记录并将当前答题情况插入更新（因为第一次统计时，还没新建这个记录）
            if (gameRank == null)
            {
                gameRank = new GameRank();
                gameRank.setUserId(loginUser.getId());
                gameRank.setUserName(loginUser.getUserName());
                gameRank.setGameId(gameId);
                GameDetail gameDetail = new GameDetail();
                gameDetail.setGameId(gameId);
                gameDetail.setUserId(loginUser.getId());
                Map<Long, GameDetailUnit> gameDetailUnitMap = new HashMap<>();
                GameDetailUnit gameDetailUnit = getGameDetailUnit(nowSubmit, questionIdToFullScore);
                gameDetailUnitMap.put(nowSubmit.getQuestionId(), gameDetailUnit);
                gameDetail.setSubmitDetail(gameDetailUnitMap);
                gameRank.setGameDetail(JSONUtil.toJsonStr(gameDetail));
                gameRank.setTotalScore(gameDetailUnit.getScore());
                gameRank.setTotalMemory(gameDetailUnit.getMemoryCost());
                gameRank.setTotalTime(gameDetailUnit.getTimeCost());
                gameRankMapper.insert(gameRank);
            }
            // gameDetail()为空时，则将当前答题情况插入更新
            else if (StringUtils.isBlank(gameRank.getGameDetail()))
            {
                GameDetail gameDetail = new GameDetail();
                gameDetail.setGameId(gameId);
                gameDetail.setUserId(loginUser.getId());
                Map<Long, GameDetailUnit> gameDetailUnitMap = new HashMap<>();
                GameDetailUnit gameDetailUnit = getGameDetailUnit(nowSubmit, questionIdToFullScore);
                gameDetailUnitMap.put(nowSubmit.getQuestionId(), gameDetailUnit);
                gameDetail.setSubmitDetail(gameDetailUnitMap);
                gameRank.setGameDetail(JSONUtil.toJsonStr(gameDetail));
                gameRank.setTotalScore(gameDetailUnit.getScore());
                gameRank.setTotalMemory(gameDetailUnit.getMemoryCost());
                gameRank.setTotalTime(gameDetailUnit.getTimeCost());
                gameRankMapper.updateById(gameRank);
            } else
            {
                // 对比两个版本的当前题目提交信息，保留最优的
                GameDetail dbGameDetail = JSONUtil.toBean(gameRank.getGameDetail(), GameDetail.class);
                Map<Long, GameDetailUnit> dbSubmitDetail = dbGameDetail.getSubmitDetail();
                // 获得数据库已有的该题目提交信息
                GameDetailUnit dbGameDetailUnit = dbSubmitDetail.get(nowSubmit.getQuestionId());
                // 组装当前的该题目提交信息
                GameDetailUnit gameDetailUnit = getGameDetailUnit(nowSubmit, questionIdToFullScore);
                if (dbGameDetailUnit == null)
                {
                    dbSubmitDetail.put(nowSubmit.getQuestionId(), gameDetailUnit);
                    dbGameDetail.setSubmitDetail(dbSubmitDetail);
                    gameRank.setGameDetail(JSONUtil.toJsonStr(dbGameDetail));
                    gameRank.setTotalScore(gameRank.getTotalScore() + gameDetailUnit.getScore());
                    gameRank.setTotalMemory(gameRank.getTotalMemory() + gameDetailUnit.getMemoryCost());
                    gameRank.setTotalTime(gameRank.getTotalTime() + gameDetailUnit.getTimeCost());
                    gameRankMapper.updateById(gameRank);
                } else
                {
                    // 如果新的优于目前的
                    if (gameDetailUnit.isBetter(dbGameDetailUnit))
                    {
                        dbSubmitDetail.put(nowSubmit.getQuestionId(), gameDetailUnit);
                        dbGameDetail.setSubmitDetail(dbSubmitDetail);
                        gameRank.setGameDetail(JSONUtil.toJsonStr(dbGameDetail));
                        gameRank.setTotalScore(gameRank.getTotalScore() - dbGameDetailUnit.getScore() + gameDetailUnit.getScore());
                        gameRank.setTotalMemory(gameRank.getTotalMemory() - dbGameDetailUnit.getMemoryCost() + gameDetailUnit.getMemoryCost());
                        gameRank.setTotalTime(gameRank.getTotalTime() - dbGameDetailUnit.getTimeCost() + gameDetailUnit.getTimeCost());
                        gameRankMapper.updateById(gameRank);
                    }
                }
            }
        });
        return submitId;
    }

    @Override
    public List<GameRankDetail> getRankByGameId(long gameId)
    {
        // 获得竞赛的题目id
        QueryWrapper<GameQuestion> gameQuestionQueryWrapper = new QueryWrapper<>();
        gameQuestionQueryWrapper.eq("gameId", gameId);
        List<GameQuestion> gameQuestions = gameQuestionMapper.selectList(gameQuestionQueryWrapper);
        List<Long> gameQuestionIds = gameQuestions.stream().map(GameQuestion::getQuestionId).collect(Collectors.toList());
        // 获得参加竞赛的所有用户
        QueryWrapper<UserGame> userGameQueryWrapper = new QueryWrapper<>();
        userGameQueryWrapper.eq("gameId", gameId);
        List<UserGame> userGames = userGameMapper.selectList(userGameQueryWrapper);
        List<Long> userIds = userGames.stream().map(UserGame::getUserId).collect(Collectors.toList());
        // 计算用户最优答题集合
        List<GameRankDetail> gameRankDetails = new ArrayList<>(userGames.size());
        for (Long userId : userIds)
        {
            // 获得排名记录
            QueryWrapper<GameRank> gameRankQueryWrapper = new QueryWrapper<>();
            gameRankQueryWrapper.eq("userId", userId).eq("gameId", gameId);
            GameRank gameRank = gameRankMapper.selectOne(gameRankQueryWrapper);
            // 无排名记录则新建
            User user = userService.getById(userId);
            GameRankDetail gameRankDetail = new GameRankDetail();
            gameRankDetail.setUserId(userId);
            gameRankDetail.setUserName(user.getUserName());
            gameRankDetail.setTotalScore(0);
            gameRankDetail.setTotalMemory(0);
            gameRankDetail.setTotalTime(0);
            // 无排名记录则新建
            if (gameRank == null)
            {
                gameRank = new GameRank();
                gameRank.setGameId(gameId);
                gameRank.setUserId(userId);
                gameRank.setUserName(user.getUserName());
                gameRank.setTotalScore(0);
                gameRank.setTotalMemory(0);
                gameRank.setTotalTime(0);
                gameRankMapper.insert(gameRank);
                gameRankDetail.setQuestionDetails(getQuestionDetails(gameQuestionIds, null));
            } else
            {
                // 有排名记录但无这道题则新建
                if (StringUtils.isBlank(gameRank.getGameDetail()))
                {
                    gameRankDetail.setQuestionDetails(getQuestionDetails(gameQuestionIds, null));
                } else
                {
                    GameDetail gameDetail = JSONUtil.toBean(gameRank.getGameDetail(), GameDetail.class);
                    // 统计总分、总耗时和总好空间
                    calcGameDetailConfig(gameDetail, gameRankDetail);
                    // 更新题目详情
                    gameRankDetail.setQuestionDetails(getQuestionDetails(gameQuestionIds, gameDetail));
                }
            }
            gameRankDetails.add(gameRankDetail);
        }
        // 排序
        List<GameRankDetail> orderGameRankDetails = gameRankDetails.stream().sorted(Comparator.comparing(GameRankDetail::getTotalScore).reversed().thenComparing(GameRankDetail::getTotalTime).thenComparing(GameRankDetail::getTotalMemory)).collect(Collectors.toList());
        // 安排名次
        for (int i = 0; i < orderGameRankDetails.size(); i++)
        {
            orderGameRankDetails.get(i).setRankOrder(i + 1);
        }
        // 返回
        return orderGameRankDetails;
    }

    @Override
    public List<UserVO> getUserListByGameId(long gameId)
    {
        QueryWrapper<UserGame> userGameQueryWrapper = new QueryWrapper<>();
        userGameQueryWrapper.eq("gameId", gameId);
        List<UserGame> userGames = userGameMapper.selectList(userGameQueryWrapper);
        if (userGames.isEmpty())
        {
            return new ArrayList<>();
        }
        List<Long> userIds = userGames.stream().map(UserGame::getUserId).collect(Collectors.toList());

        return userService.getUserVOByUserIdList(userIds);
    }

    public GameDetailUnit getGameDetailUnit(QuestionSubmit nowSubmit, Map<Long, Integer> questionIdToFullScore)
    {
        GameDetailUnit gameDetailUnit = new GameDetailUnit();
        gameDetailUnit.setId(nowSubmit.getQuestionId());
        Question question = questionService.getById(nowSubmit.getQuestionId());
        gameDetailUnit.setName(question.getTitle());
        if (StringUtils.isBlank(nowSubmit.getJudgeInfo()) || "{}".equals(nowSubmit.getJudgeInfo()))
        {
            try
            {
                // todo 有死循环隐患，因为可能这块依赖于答题结果是否在异步处理后获得答题情况值，后续再优化
                while (StringUtils.isBlank(nowSubmit.getJudgeInfo()) || "{}".equals(nowSubmit.getJudgeInfo()))
                {
                    TimeUnit.SECONDS.sleep(5);
                    nowSubmit = questionSubmitService.getById(nowSubmit.getId());
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        JudgeInfo judgeInfo = JSONUtil.toBean(nowSubmit.getJudgeInfo(), JudgeInfo.class);
        gameDetailUnit.setTimeCost(Math.toIntExact(judgeInfo.getTime()));
        gameDetailUnit.setMemoryCost(Math.toIntExact(judgeInfo.getMemory()));
        String judgeInfoMessage = judgeInfo.getMessage();
        // todo 打分逻辑后续可以再完善，等判题机写完后
        if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfoMessage))
        {
            // 如果ACCEPTED则打满分
            gameDetailUnit.setScore(questionIdToFullScore.get(nowSubmit.getQuestionId()));
        } else
        {
            gameDetailUnit.setScore(0);
        }
        return gameDetailUnit;
    }

    public List<GameDetailUnit> getQuestionDetails(List<Long> questionIds, GameDetail gameDetail)
    {
        List<GameDetailUnit> gameDetailUnits = new ArrayList<>();
        List<Question> questions = questionService.listByIds(questionIds);
        // 为空返回空值题目
        if (gameDetail == null)
        {
            for (Question question : questions)
            {
                GameDetailUnit gameDetailUnit = new GameDetailUnit();
                gameDetailUnit.setId(question.getId());
                gameDetailUnit.setName(question.getTitle());
                gameDetailUnit.setScore(0);
                gameDetailUnit.setMemoryCost(0);
                gameDetailUnit.setTimeCost(0);
                gameDetailUnits.add(gameDetailUnit);
            }
        }
        // 不为空按需插入空值题目
        else
        {
            Map<Long, GameDetailUnit> submitDetail = gameDetail.getSubmitDetail();
            for (Question question : questions)
            {
                GameDetailUnit gameDetailUnit = submitDetail.get(question.getId());
                if (gameDetailUnit == null)
                {
                    gameDetailUnit = new GameDetailUnit();
                    gameDetailUnit.setId(question.getId());
                    gameDetailUnit.setName(question.getTitle());
                    gameDetailUnit.setScore(0);
                    gameDetailUnit.setMemoryCost(0);
                    gameDetailUnit.setTimeCost(0);
                    gameDetailUnits.add(gameDetailUnit);
                } else
                {
                    gameDetailUnits.add(gameDetailUnit);
                }
            }
        }
        return gameDetailUnits;
    }

    public void calcGameDetailConfig(GameDetail gameDetail, GameRankDetail gameRankDetail)
    {
        Map<Long, GameDetailUnit> submitDetail = gameDetail.getSubmitDetail();
        int totalScore = 0;
        int totalTime = 0;
        int totalMemory = 0;
        for (Long aLong : submitDetail.keySet())
        {
            GameDetailUnit gameDetailUnit = submitDetail.get(aLong);
            totalScore += gameDetailUnit.getScore();
            totalMemory += gameDetailUnit.getMemoryCost();
            totalTime += gameDetailUnit.getTimeCost();
        }
        gameRankDetail.setTotalMemory(totalMemory);
        gameRankDetail.setTotalTime(totalTime);
        gameRankDetail.setTotalScore(totalScore);
    }


    public void validAddRequest(GameAddRequest gameAddRequest)
    {
        if (gameAddRequest == null)
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String gameName = gameAddRequest.getGameName();
        Integer gameTotalNum = gameAddRequest.getGameTotalNum();
        String publicZone = gameAddRequest.getPublicZone();
        String gameType = gameAddRequest.getGameType();
        String startTime = gameAddRequest.getStartTime();
        String endTime = gameAddRequest.getEndTime();
        List<Long> questionIdList = gameAddRequest.getQuestionIdList();
        List<Integer> questionFullScoreList = gameAddRequest.getQuestionFullScoreList();

        if (gameTotalNum == null || gameTotalNum < 0 || StringUtils.isAnyBlank(gameName, publicZone, gameType, startTime, endTime))
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if (CollectionUtils.isEmpty(questionIdList) || CollectionUtils.isEmpty(questionFullScoreList) || questionIdList.size() != questionFullScoreList.size())
        {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目和分数列表不能为空");
        }
    }
}




