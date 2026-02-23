# sspuoj_backend_for_micro

#### 介绍
二工大OJ系统后端项目代码，用spring boot实现

#### 软件架构
软件架构说明


#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

swagger: http://localhost:9101/api/doc.html#/home

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

根据所要存取字符长度及MySQL数据库版本，选择字段类型

varchar类型
MySQL5.0以下版本: 最大长度255

MySQL5.0以上版本: 最大长度65535

如果VARCHAR类型不能满足你存取字符串长度的需求，那么应选择以下字符字段类型。

text类型
最大长度65535

mediumtext类型
最大长度16777215

longtext类型
最大长度4294967295

上面这些类型都对应java的String类型

如果存储很多长的文本数据，不建议使用mysql，可以考虑使用nosql数据库；如果同时还需要经常进行文本的检索，可以使用搜索引擎技术，比如elasticsearch。



#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
