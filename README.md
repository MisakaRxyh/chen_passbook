# chen_passbook
仿小米卡包后台项目
### 1、课程技术

存储：MySQL+Redis+HBase    
中间件：Kafka    
框架：SpringBoot    

#### HBase:
```
docker启动
docker run -d -h docker-hbase \
        -p 2181:2181 \
        -p 8080:8080 \
        -p 8085:8085 \
        -p 9090:9090 \
        -p 9000:9000 \
        -p 9095:9095 \
        -p 16000:16000 \
        -p 16010:16010 \
        -p 16201:16201 \
        -p 16301:16301 \
        -p 16020:16020\
        --name hbase \
        harisekhon/hbase
```
进入容器
```
docker exec -ti e60a300f7749 /bin/bash
```
进入HBase
```
hbase shell
```
docker中宿主机复制文件进容器
```shell
docker cp Downloads/coding-254/passbook/src/main/resources/passbook.hsh hbase:/tmp
```
用脚本创建hbase表
```
hbase shell /tmp/passbook.hsh
```
#### Kafka
启动
```
bin/kafka-server-start.sh config/server.properties
```
创建
```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic merchants-template
```
连接zk查看
```
bin/kafka-topics.sh --list --zookeeper localhost:2181
```
打开消费者客户端
```
./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic merchants-template --from-beginning
```
### 2、需求分析

![image-20210821123420118](/Users/yezi/Library/Application Support/typora-user-images/image-20210821123420118.png)

#### 商户投放子系统

商户开放平台：用于商户获取优惠券信息
商户接口字段

| 字段名称 | 含义 | 备注 |
| -------- | ---- | ---- |
| name | 商户名称 | 全局唯一 |
| logo_url | 商户logo |      |
| business_license_url | 商户营业执照 |      |
| phone | 商户联系电话 |      |
| address | 商户地址 |      |

优惠券接口字段
| 字段名称 | 含义 | 备注 |
| -------- | ---- | ---- |
| id   | 所属商户id |      |
| title | 优惠券标题 | 全局唯一 |
| summary | 优惠券摘要 |      |
| desc | 优惠券详细信息 |      |
| limit | 最大个数限制 |      |
| has_token | 优惠券是否有token |      |
| background | 优惠券背景色 |      |
| start | 优惠券开始使用时间 |      |
| end  | 优惠券结束使用时间 |      |

#### 用户应用子系统
模拟小米卡包
我的卡包 过期优惠券 优惠券库存 用户反馈
![image-20210820140348952](/Users/yezi/Library/Application Support/typora-user-images/image-20210820140348952.png)

将系统解耦，拆分成两个子系统的原因：

1、两个系统面向的群体不同，流量也不同，商家的操作频率远小于用户

2、业务体系不同，商户tob，用户toc

3、开发维护更方便，交给两个团队开发

### 3、技术架构

![image-20210820141109097](/Users/yezi/Library/Application Support/typora-user-images/image-20210820141109097.png)

#### 缓存层设计

![image-20210820141543186](/Users/yezi/Library/Application Support/typora-user-images/image-20210820141543186.png)

访问将商户信息和Token信息以这两类缓存结构放入redis缓存中，这样在展示优惠券信息时就不会产生性能上的差异。

#### 表结构设计
商户投放子系统
- 商户信息
  - is_audit:商户是否通过审核，默认为false
- 优惠券信息
  - b列族：base，表示基本信息
    - id:优惠券所属商户id，对应商户信息表id
    - has_token:标记优惠券是否有token
    - background:优惠券背景色
  - o列族：表示限制条件
    - limit:优惠券的最大限制数
    - start:优惠券的有效起始时间
    - end:优惠券的有效终止时间
- 优惠券Token信息

![image-20210820142957978](/Users/yezi/Library/Application Support/typora-user-images/image-20210820142957978.png)

用户应用子系统
- Pass 用户的优惠券
  - assigned_data:优惠券领取日期
  - con_date:优惠券消费日期
- Feedback 反馈
  - type:评论类型
  - template_id:优惠券的rowkey？对应于商户的passtemplate

![image-20210820143811760](/Users/yezi/Library/Application Support/typora-user-images/image-20210820143811760.png)

 id生成器
- user表
- RowKey
  - RowKey是用户表的行键，代表用户的id

![image-20210820144226114](/Users/yezi/Library/Application Support/typora-user-images/image-20210820144226114.png)

### 4、项目结构

merchants项目

```
.
├── MerchantsApplication.java			// 主运行文件
├── constant											// 静态变量文件夹
│   ├── Constants.java
│   ├── ErrorCode.java
│   └── TemplateColor.java
├── controller
│   └── MerchantsController.java
├── dao														// 接口实现数据库查询 orm 实体关系映射 可以用mybatis替换
│   └── MerchantsDao.java
├── entity												// 实体对象
│   └── Merchants.java
├── security											// 安全访问
│   ├── AccessContext.java				// 存储Token
│   └── AuthCheckInterceptor.java	// 拦截器验证Token
├── service
│   ├── .DS_Store
│   ├── IMerchantsServ.java				// 对商户服务的接口定义
│   └── impl
│       └── MerchantsServImpl.java
└── vo														// value-object 值对象，用于在业务service之间传递
    ├── CreateMerchantsRequest.java // 创建商户对象时的请求对象 最后会将其转化为Merchants对象保存到数据库
    ├── CreateMerchantsResponse.java // 创建商户对象时的响应对象，用于返回商户id
    ├── PassTemplate.java					// 优惠券对象
    └── Response.java							// 通用的响应对象，用于返回给前端业务请求
```

passbook项目
```
.
├── .DS_Store
├── PassbookApplication.java
├── advice
│   └── GlobalExceptionHandler.java
├── constant
│   ├── Constants.java
│   ├── FeedbackType.java
│   └── PassStatus.java
├── controller
│   ├── CreateUserController.java
│   ├── PassbookController.java
│   └── TokenUploadController.java
├── dao
│   └── MerchantsDao.java
├── entity
│   └── Merchants.java
├── log
│   ├── LogConstants.java
│   ├── LogGenerator.java
│   └── LogObject.java
├── mapper												对应到HBase中的四张表的orm，可以直接从HBase表中获得一个对应的对象
│   ├── FeedbackRowMapper.java
│   ├── PassRowMapper.java
│   ├── PassTemplateRowMapper.java
│   └── UserRowMapper.java
├── service
│   ├── ConsumerPassTemplate.java
│   ├── IFeedbackService.java
│   ├── IGainPassTemplateService.java
│   ├── IHBasePassService.java
│   ├── IInventoryService.java
│   ├── IUserPassService.java
│   ├── IUserService.java
│   └── impl
│       ├── FeedbckServiceImpl.java
│       ├── GainPassTemplateServiceImpl.java
│       ├── HBasePassServiceImpl.java
│       ├── InventoryServiceImpl.java
│       ├── UserPassServiceImpl.java
│       └── UserServiceImpl.java
├── utils
│   └── RowKeyGenUtil.java
└── vo
    ├── ErrorInfo.java
    ├── Feedback.java
    ├── GainPassTemplateRequest.java
    ├── InventoryResponse.java
    ├── Pass.java
    ├── PassInfo.java
    ├── PassTemplate.java
    ├── PassTemplateInfo.java
    ├── Response.java
    └── User.java
```

### Q&A
1、在根据 PassTemplate 构造 RowKey 中为什么要用 md5 处理 passInfo 转为 rowKey ？

```
public static String genPassTemplateRowKey(PassTemplate passTemplate){

        String passInfo = String.valueOf(passTemplate.getId() + "_" + passTemplate.getTitle());
        String rowKey = DigestUtils.md5Hex(passInfo);

        log.info("GenPassTemplateRowKey:",passInfo,rowKey);
        return rowKey;
    }
```
> 因为HBase是一个集群，HBase上的数据都是基于RowKey进行存储，RowKey相近的值会存在一起，如果不处理，数据会集中在一个节点，也就是一台机器上，不利于负载均衡，RowKey越分散，数据存放会越分散，有利于HBase负载均衡的策略，查询速度也会更快。

2、根据 Feedback 构造 RowKey 中返回值的定义原因

```
public static String genFeedbackRowKey(Feedback feedback) {
        return new StringBuilder(String.valueOf(feedback.getUserId())).reverse().toString() +
                (Long.MAX_VALUE - System.currentTimeMillis());
    }
```
> 对于每一个feedback来说都会有一个用户id，而对于同一个用户来说，他的所有的feedback存在相近的位置会比较好，这样有利于我们去查询扫码同一个用户的所有feedback
> reverse()：是因为系统中的userId的前缀与系统中用户个数相关，数据量大了之后，前缀是相同的，而后缀是一个随机数，翻转后有利于数据的分散
> 而Long.MAX_VALUE - System.currentTimeMillis()则是使得数据根据创建时间倒序存放在HBase中，这样扫描HBase最先拿到的是用户最近创建的一条feedback

3、Kafka 注解
> @KafkaListener 标识kafka中的消费者
> @Payload 从kafka中接受到的数据是什么
> @Header 用于写入分区
> @
