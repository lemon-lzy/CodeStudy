package sspu.zzx.sspuoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目
 *
 * @TableName question
 */
@TableName(value = "question")
@Data
public class Question implements Serializable
{
    /**
     * id，改成自增id，方便用户后续通过题号搜索
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private String tags;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 题目答案模板
     * json-Map
     * 格式：{"language":"${template_value}"}
     * 编程题作为代码模板
     * 非编程题作为文本模板，起到提示的作用
     * 因map无法直接转，先搞成list，元素是[key,val]
     */
    private String answerTemplate;

    /**
     * 题目提交数
     */
    private Integer submitNum;

    /**
     * 题目通过数
     */
    private Integer acceptedNum;

    /**
     * 判题用例（json 数组）
     */
    private String judgeCase;

    /**
     * 判题配置（json 对象）
     */
    private String judgeConfig;

    // todo 添加题目类型枚举类
    /**
     * 题目类型：代码题和文字题
     */
    private String questionType;

    // todo 合理处理题目的私有化逻辑
    /**
     * 是否私有化：即不公开，只允许自己或所在组织查看
     */
    private Boolean isPrivate;

    /**
     * 题目难度：简单、中等、困难
     */
    private String questionDifficulty;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}