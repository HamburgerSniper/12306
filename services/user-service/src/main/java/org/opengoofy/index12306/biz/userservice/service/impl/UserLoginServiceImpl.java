/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengoofy.index12306.biz.userservice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengoofy.index12306.biz.userservice.common.enums.UserChainMarkEnum;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserDO;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserDeletionDO;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserMailDO;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserPhoneDO;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserReuseDO;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserDeletionMapper;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserMailMapper;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserMapper;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserPhoneMapper;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserReuseMapper;
import org.opengoofy.index12306.biz.userservice.dto.req.UserDeletionReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserLoginReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.req.UserRegisterReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserLoginRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserRegisterRespDTO;
import org.opengoofy.index12306.biz.userservice.service.UserLoginService;
import org.opengoofy.index12306.biz.userservice.service.UserService;
import org.opengoofy.index12306.framework.starter.cache.DistributedCache;
import org.opengoofy.index12306.framework.starter.common.toolkit.BeanUtil;
import org.opengoofy.index12306.framework.starter.convention.exception.ClientException;
import org.opengoofy.index12306.framework.starter.convention.exception.ServiceException;
import org.opengoofy.index12306.framework.starter.designpattern.chain.AbstractChainContext;
import org.opengoofy.index12306.frameworks.starter.user.core.UserContext;
import org.opengoofy.index12306.frameworks.starter.user.core.UserInfoDTO;
import org.opengoofy.index12306.frameworks.starter.user.toolkit.JWTUtil;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.opengoofy.index12306.biz.userservice.common.constant.RedisKeyConstant.LOCK_USER_REGISTER;
import static org.opengoofy.index12306.biz.userservice.common.constant.RedisKeyConstant.USER_DELETION;
import static org.opengoofy.index12306.biz.userservice.common.constant.RedisKeyConstant.USER_REGISTER_REUSE_SHARDING;
import static org.opengoofy.index12306.biz.userservice.common.enums.UserRegisterErrorCodeEnum.HAS_USERNAME_NOTNULL;
import static org.opengoofy.index12306.biz.userservice.common.enums.UserRegisterErrorCodeEnum.MAIL_REGISTERED;
import static org.opengoofy.index12306.biz.userservice.common.enums.UserRegisterErrorCodeEnum.PHONE_REGISTERED;
import static org.opengoofy.index12306.biz.userservice.common.enums.UserRegisterErrorCodeEnum.USER_REGISTER_FAIL;
import static org.opengoofy.index12306.biz.userservice.toolkit.UserReuseUtil.hashShardingIdx;

/**
 * @description 用户登录接口实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserReuseMapper userReuseMapper;
    private final UserDeletionMapper userDeletionMapper;
    private final UserPhoneMapper userPhoneMapper;
    private final UserMailMapper userMailMapper;
    private final RedissonClient redissonClient;
    private final DistributedCache distributedCache;
    private final AbstractChainContext<UserRegisterReqDTO> abstractChainContext;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    /*
     * 基于128位Hash的布隆过滤器 -- 用于解决用户注册时缓存穿透的布隆过滤器
     * - 缓存穿透：查询一个缓存和数据库中都不存在的数据
     * - 缓存穿透的主要解决方案：(a) 缓存空对象(浪费空间 & 数据不一致)
     *                       (b) BloomFilter 布隆过滤器
     *                       (c) Redis Set(内存开销过大)
     *                       (d) 分布式锁(用户注册高峰期时,产生拥挤现象，用户体验较差)
     * - 关于BloomFilter的解决方案
     * -- 1.BloomFilter的优点：
     * --- (1) 总的来说，相比于其他数据结构，BloomFilter在时间和空间上都有巨大优势
     *         其存储空间和插入/查询时间都是常数O(k)，空间复杂度为O(m)，不会随着元素增加而增加，且占用空间少
     *         BloomFilter并不需要存储元素本身，在某些对保密要求严格的场景下有优势(全量存储且不存储数据本身)
     * --- (2) 相比于HashMap：HashMap本身是一个指针数组，其在64bit的系统上是64bit的开销，如果采用拉链法处理冲突，则又需要额外的开销
     *         返回可能存在的情况中，如果允许有1%的错误率的话，每个元素大约需要10bit的存储空间，整个存储空间开销约为HashMap的15%左右
     * --- (3) 相比与Set：如果采用HashMap的方式实现，则情况同上；如果采用平衡树的方式实现，则一个节点需要一个指针存储数据的位置，还需要
     *         两个指针指向其子节点，因此开销相对于HashMap是更多的
     * --- (4) 相比于bit array：对于某个元素是否存在，先对元素做hash，取模定位到具体的bit，如果该bit为1，则返回元素存在；如果该bit为0
     *         则返回此元素不存在。可以看出，在返回元素是否存在时，bit array也是会出现误判的，如果想获得和BloomFilter同样的误判率，其
     *         往往需要比BloomFilter更大的存储空间
     * -- 2.BloomFilter的缺点：
     * --- (1) 存在误判问题：BloomFilter认为存在的元素不一定存在；但是BloomFilter认为不存在的元素一定不存在
     * --- (2) 删除元素较麻烦：原则上BloomFilter不允许删除元素，这样会导致其中的元素越来越多，实际磁盘中已经删除的元素，BloomFilter
     *         会依然认为其存在，这回造成越来越多的false positive
     *
     * - 关于BloomFilter的设计问题
     * -- 1. 导致BloomFilter查询碰撞误判问题的原因
     * --- (1) 哈希碰撞：BloomFilter使用多个哈希函数将一个元素映射到多个位置，在极少数情况下，不同元素可能会映射到相同为止导致误判
     * --- (2) 容量不足：如果BloomFilter的容量设置的不够大，会增加误判的可能性；过小的容量可能会导致哈希冲突的增加，导致误判率上升
     * --- (3) 删除操作：Redis的BloomFilter不支持删除找抽，一旦一个元素被添加，就无法从BloomFilter中删除。如果需要删除可能会导致误判
     * --- (4) 业务准确性：如果业务场景对准确性要求极高，BloomFilter可能不是最合适的选择，应该考虑其他更准确的数据结构或算法
     * -- 2. BloomFilter实战——容量 & 碰撞率评估
     * --- (1) 淘宝商城在第一年做业务时，因为没有往年相关数据作为参考，因此可能很难预估订单量：12306则较好预估(人口估计，十亿级别)
     * --- (2) 碰撞率评估：BloomFilter可以设置碰撞率，这个碰撞率可以在非常低的范围内，例如0.1%甚至更低；本质上是一个空间和重复碰撞的博弈
     * -- 3. 初始容量评估不够用怎么办 Question:随着国内人口越来越多，之前评估的BloomFilter容量不够用了怎么办
     * --- (1) 我们可以有个定时任务，每一段时间统计BloomFilter和预期差值有多少，当已注册人数达到BloomFilter容量的一个触发阈值(比如80%)
     *         时，通过后台任务重建BloomFilter
     */

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        String usernameOrMailOrPhone = requestParam.getUsernameOrMailOrPhone();

        // mailFlag 用于判断phone登录还是mail登录
        boolean mailFlag = false;
        // 时间复杂度最佳 O(1)。indexOf or contains 时间复杂度为 O(n)
        for (char c : usernameOrMailOrPhone.toCharArray()) {
            // 判断是否通过mail的方式注册，mail方式注册的用户最显著的特征为"@"
            if (c == '@') {
                mailFlag = true;
                break;
            }
        }

        // 获取username，三种登录方式中，mail/phone两种登录方式都需要额外再获取user信息
        String username;
        if (mailFlag) {
            // 通过mail登录
            LambdaQueryWrapper<UserMailDO> queryWrapper = Wrappers.lambdaQuery(UserMailDO.class)
                    .eq(UserMailDO::getMail, usernameOrMailOrPhone);
            username = Optional.ofNullable(userMailMapper.selectOne(queryWrapper))
                    .map(UserMailDO::getUsername)
                    .orElseThrow(() -> new ClientException("用户名/手机号/邮箱不存在"));
        } else {
            // 通过phone登录则能获取到username，通过username登录则暂时将username设为null等待下一步获取
            LambdaQueryWrapper<UserPhoneDO> queryWrapper = Wrappers.lambdaQuery(UserPhoneDO.class)
                    .eq(UserPhoneDO::getPhone, usernameOrMailOrPhone);
            username = Optional.ofNullable(userPhoneMapper.selectOne(queryWrapper))
                    .map(UserPhoneDO::getUsername)
                    .orElse(null);
        }

        // 此时如果username为null，说明user不是通过mail登录，也不是通过phone登录，而是通过username用户名登录
        username = Optional.ofNullable(username).orElse(requestParam.getUsernameOrMailOrPhone());
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username)
                .eq(UserDO::getPassword, requestParam.getPassword())
                .select(UserDO::getId, UserDO::getUsername, UserDO::getRealName);
        UserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO != null) {
            UserInfoDTO userInfo = UserInfoDTO.builder()
                    .userId(String.valueOf(userDO.getId()))
                    .username(userDO.getUsername())
                    .realName(userDO.getRealName())
                    .build();
            // 生成accessToken，token的过期时间为1天
            String accessToken = JWTUtil.generateAccessToken(userInfo);
            UserLoginRespDTO actual = new UserLoginRespDTO(userInfo.getUserId(), requestParam.getUsernameOrMailOrPhone(), userDO.getRealName(), accessToken);
            // 将actual+token放入缓存 设置过期时间为30min
            distributedCache.put(accessToken, JSON.toJSONString(actual), 30, TimeUnit.MINUTES);
            return actual;
        }
        throw new ServiceException("账号不存在或密码错误");
    }

    @Override
    public UserLoginRespDTO checkLogin(String accessToken) {
        // 校验用户是否登录时，因在登录时已经将通过userInfo生成的accessToken放入了缓存
        // 因此，只需要校验缓存中是否存在accessToken即可
        return distributedCache.get(accessToken, UserLoginRespDTO.class);
    }

    @Override
    public void logout(String accessToken) {
        // 同样，用户登出时，只需要在缓存中删除accessToken即可
        if (StrUtil.isNotBlank(accessToken)) {
            distributedCache.delete(accessToken);
        }
    }


    /*
     * 通过加一层Redis Set缓存，解决BloomFilter无法进行删除的问题(Redis Set用来记录注册过但被注销的用户名)
     * 假设我们有一条用户名为"Hamburger"的数据，注册后是如何不被重复注册，以及用户注销后又是如何能够被再次使用的
     * -- 1. 用户名"Hamburger"成功注册后，将其添加至BloomFilter
     * -- 2. 当其他用户查询"Hamburger"是否已经被使用时，首先检查BloomFilter是否包含该用户名
     * -- 3. 如果BloomFilter中不存在这个用户名，根据BloomFilter的特点，可以确认该用户名一定没有被使用过。
     *      因此返回成功证明该用户名可用
     * -- 4. 如果BloomFilter中存在该用户名，则需要再Redis Set结构中进一步检查是否包含该用户名。
     *      如果Redis Set中存在该用户名，则表示该用户名已被注销，同样可被再次使用
     * -- 5. 如果BloomFilter中存在该用户名，但Redis Set结构中不存在该用户名，则说明该用户名已被使用且尚未被注销
     *      因此返回该用户名不可用(这里即使BloomFilter出现误判，用户体验也并不会因为一个用户名被占用而变差)
     *
     * 但是上面这种解决方式依然存在问题：如果用户频繁地申请账号再注销，可能会导致用户注销可复用的username Redis Set结构
     * 变得越来越庞大，增加了存储和查询的负担。因此，为了防止这种情况的发生，采用以下方式进行解决
     * -- 1. 异常行为限制 {@link UserRegisterCheckDeletionChainHandler}
     *      用户每次注销时，记录用户的证件号，并限制单证件号仅可用于注销5次，超过这个限制的次数，将禁止该证件号再次注册
     * -- 2. 缓存分片处理 {@link UserReuseUtil#hashShardingIdx(String)}
     *      对username Redis Set进行分片。即使我们对异常行为进行限制，也会存在大量的用户注销账户，那么到时候这些数据
     *      存储在一个大的Redis Set中会成为一个灾难，可能会出现Redis的大key问题，因此将Set结构进行分片处理
     *      根据username的hashCode值对1024进行取模，将存储分散在了1024个Set结构中，可以有效解决这个问题
     *
     * Redis的大key问题
     * -- 1. 单个简单的key存储的value很大：当使用String类型时，如果value的大小超过10KB，则可能被视作大key
     * -- 2. hash/set/list等数据类型中存储过多的元素
     * -- 3. 一个集群中存储了上亿的key
     * Redis的大key问题的解决方案
     * -- 1. 使用分片技术 sharding
     *      将数据分散到多个redis节点上，有效降低单个key的大小。
     *      这种方法适用于数据量较大的场景，如电商、社交等
     * -- 2. 使用更合适的数据结构
     *      如果需要存储大量的字符串，可以使用List或Hash代替String，这样可以避免单个key过大的问题
     * -- 3. 使用懒加载技术 lazy loading
     *      通过延迟加载数据，可以在需要时再加载完整的数据，从而避免一次性加载过大数据的问题。
     *      这种方法适用于数据加载过快的场景，例如图片缓存、视频缓存等
     * -- 4. 使用数据压缩技术
     *      通过对数据进行压缩，可以减少数据的大小，从而避免大key问题。
     *      这种方法适用于数据量较大且适合压缩的场景，例如文本数据、日志数据等
     * -- 5. 使用定时任务清理过期数据 cron job
     *      通过定期清理过期数据，可以避免大key问题。
     *      这种方法适用于数据有过期时间的场景，例如缓存数据、统计数据等。
     */

    @Override
    public Boolean hasUsername(String username) {
        boolean hasUsername = userRegisterCachePenetrationBloomFilter.contains(username);
        if (hasUsername) {
            // BloomFilter中存在该用户名，则需要再Redis Set结构中进一步检查是否包含该用户名
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            // 如果Redis Set中存在该用户名，则表示该用户名已被注销，同样可被再次使用;
            // 如果BloomFilter中存在该用户名，但Redis Set结构中不存在该用户名，则说明该用户名已被使用且尚未被注销
            return instance.opsForSet().isMember(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        }
        // BloomFilter中不存在这个用户名，则该用户名一定没有被使用过
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public UserRegisterRespDTO register(UserRegisterReqDTO requestParam) {
        // 责任链模式：验证注册用户请求参数是否合规 0：用户名必填参数校验 1：用户名唯一性校验 2：用户注册证件号是否多次注销
        abstractChainContext.handler(UserChainMarkEnum.USER_REGISTER_FILTER.name(), requestParam);
        // RLock：基于Redis的可重入锁
        // 用户注册锁：对username加锁
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER + requestParam.getUsername());
        boolean tryLock = lock.tryLock();
        if (!tryLock) {
            // 获取锁失败，则说明该username已经被别人加锁 "该用户名已存在"
            throw new ServiceException(HAS_USERNAME_NOTNULL);
        }
        try {
            try {
                // 注册用户信息 inserted:插入记录数
                int inserted = userMapper.insert(BeanUtil.convert(requestParam, UserDO.class));
                if (inserted < 1) {
                    // 插入小于1条记录，说明插入失败 "用户注册失败"
                    throw new ServiceException(USER_REGISTER_FAIL);
                }
            } catch (DuplicateKeyException dke) {
                // DuplicateKeyException:尝试插入或更新数据导致违反主键或唯一性约束时引发的异常
                log.error("用户名 [{}] 重复注册", requestParam.getUsername());
                // 捕获到该异常时，说明数据库中已存在该用户名 "用户重复注册"
                throw new ServiceException(HAS_USERNAME_NOTNULL);
            }

            // 注册成功，则更新其他表走接下来的流程
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .phone(requestParam.getPhone())
                    .username(requestParam.getUsername())
                    .build();
            try {
                userPhoneMapper.insert(userPhoneDO);
            } catch (DuplicateKeyException dke) {
                log.error("用户 [{}] 注册手机号 [{}] 重复", requestParam.getUsername(), requestParam.getPhone());
                throw new ServiceException(PHONE_REGISTERED);
            }
            /*
             * 同时写userMailMapper和userPhoneMapper的原因：user_mail和user_phone时两个路由表
             * 在分库分表中，我们是通过username进行分片的。因此，如果在查询用户信息时不带username，将会触发读扩散问题
             * 读扩散问题：登录时由于没带用户名，导致无法确定用户的分片键，使得系统无法直接锁定用户的数据位于哪个数据库表中，
             *           进而导致为了找到用户的数据，只能对全部的数据库表进行扫描和查询
             */
            if (StrUtil.isNotBlank(requestParam.getMail())) {
                UserMailDO userMailDO = UserMailDO.builder()
                        .mail(requestParam.getMail())
                        .username(requestParam.getUsername())
                        .build();
                try {
                    userMailMapper.insert(userMailDO);
                } catch (DuplicateKeyException dke) {
                    log.error("用户 [{}] 注册邮箱 [{}] 重复", requestParam.getUsername(), requestParam.getMail());
                    throw new ServiceException(MAIL_REGISTERED);
                }
            }
            String username = requestParam.getUsername();
            // 此时这个username已经被注册，那么该username已经不可被复用，通过userReuseMapper删除用户可复用数据
            userReuseMapper.delete(Wrappers.update(new UserReuseDO(username)));

            // 在username Redis Set中也需要删除掉username(Redis Set中存在表示其可被复用)，防止其被复用造成冲突
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            instance.opsForSet().remove(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
            // 布隆过滤器设计问题：设置多大、碰撞率以及初始容量不够了怎么办？详情查看：https://nageoffer.com/12306/question
            userRegisterCachePenetrationBloomFilter.add(username);
        } finally {
            lock.unlock();
        }
        return BeanUtil.convert(requestParam, UserRegisterRespDTO.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletion(UserDeletionReqDTO requestParam) {
        String username = UserContext.getUsername();
        if (!Objects.equals(username, requestParam.getUsername())) {
            // 此处严谨来说，需要上报风控中心进行异常检测
            throw new ClientException("注销账号与登录账号不一致");
        }
        RLock lock = redissonClient.getLock(USER_DELETION + requestParam.getUsername());
        // 加锁为什么放在 try 语句外？https://www.yuque.com/magestack/12306/pu52u29i6eb1c5wh
        lock.lock();
        try {
            UserQueryRespDTO userQueryRespDTO = userService.queryUserByUsername(username);
            UserDeletionDO userDeletionDO = UserDeletionDO.builder()
                    .idType(userQueryRespDTO.getIdType())
                    .idCard(userQueryRespDTO.getIdCard())
                    .build();
            userDeletionMapper.insert(userDeletionDO);
            UserDO userDO = new UserDO();
            userDO.setDeletionTime(System.currentTimeMillis());
            userDO.setUsername(username);
            // MyBatis Plus 不支持修改语句变更 del_flag 字段
            userMapper.deletionUser(userDO);
            UserPhoneDO userPhoneDO = UserPhoneDO.builder()
                    .phone(userQueryRespDTO.getPhone())
                    .deletionTime(System.currentTimeMillis())
                    .build();
            userPhoneMapper.deletionUser(userPhoneDO);
            if (StrUtil.isNotBlank(userQueryRespDTO.getMail())) {
                UserMailDO userMailDO = UserMailDO.builder()
                        .mail(userQueryRespDTO.getMail())
                        .deletionTime(System.currentTimeMillis())
                        .build();
                userMailMapper.deletionUser(userMailDO);
            }
            // 用户注销 删除token
            distributedCache.delete(UserContext.getToken());
            // 用户注销 username复用性构建
            userReuseMapper.insert(new UserReuseDO(username));
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            instance.opsForSet().add(USER_REGISTER_REUSE_SHARDING + hashShardingIdx(username), username);
        } finally {
            lock.unlock();
        }
    }
}
