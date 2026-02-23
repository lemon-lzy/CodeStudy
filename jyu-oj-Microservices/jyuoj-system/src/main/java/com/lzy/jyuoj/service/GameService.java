package sspu.zzx.sspuoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import sspu.zzx.sspuoj.model.dto.game.GameAddRequest;
import sspu.zzx.sspuoj.model.dto.game.GameQueryRequest;
import sspu.zzx.sspuoj.model.dto.game.GameQuestionSubmitRequest;
import sspu.zzx.sspuoj.model.entity.Game;
import sspu.zzx.sspuoj.model.vo.game.GameInfoVo;
import sspu.zzx.sspuoj.model.vo.game.GameQuestionVo;
import sspu.zzx.sspuoj.model.vo.game.GameRankDetail;
import sspu.zzx.sspuoj.model.vo.user.UserVO;

import java.util.List;


/**
 * @author ZZX
 * @description 针对表【game】的数据库操作Service
 * @createDate 2023-11-15 14:36:13
 */
public interface GameService extends IService<Game>
{

    /**
     * 获得查询条件
     *
     * @param gameQueryRequest
     * @return
     */
    QueryWrapper<Game> getQueryWrapper(GameQueryRequest gameQueryRequest);

    /**
     * 获取用户参与的竞赛列表
     *
     * @param userId
     * @return
     */
    List<Game> getJoinedGameList(long userId);

    /**
     * 获取竞赛信息详情
     *
     * @param id
     * @return
     */
    GameInfoVo getGameInfo(long id);

    /**
     * 获取竞赛题目列表
     *
     * @param gameId
     * @return
     */
    List<GameQuestionVo> getProblemListByGameId(long gameId);

    /**
     * 创建竞赛
     *
     * @param gameAddRequest
     * @return
     */
    Long createGame(GameAddRequest gameAddRequest);

    /**
     * 更新竞赛
     *
     * @param gameAddRequest
     * @return
     */
    boolean updateGame(GameAddRequest gameAddRequest);

    /**
     * 删除竞赛
     *
     * @param id
     * @return
     */
    boolean deleteGame(long id);

    /**
     * 用户加入或取消加入该竞赛
     *
     * @param gameId
     * @param userId
     * @return
     */
    boolean joinOrNot(long gameId, long userId);

    /**
     * 用户提交题目
     *
     * @param gameQuestionSubmitRequest
     * @return
     */
    Long questionSubmit(GameQuestionSubmitRequest gameQuestionSubmitRequest);

    /**
     * 统计竞赛排行榜信息
     *
     * @param gameId
     * @return
     */
    List<GameRankDetail> getRankByGameId(long gameId);

    /**
     * 获取竞赛的参与者列表
     *
     * @param gameId
     * @return
     */
    List<UserVO> getUserListByGameId(long gameId);
}
