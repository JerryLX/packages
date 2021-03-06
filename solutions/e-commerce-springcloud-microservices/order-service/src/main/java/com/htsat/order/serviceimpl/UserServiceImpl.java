package com.htsat.order.serviceimpl;

import com.htsat.order.config.RedisConfig;
import com.htsat.order.dao.REcUserinfoMapper;
import com.htsat.order.model.REcUserinfo;
import com.htsat.order.service.IUserService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class UserServiceImpl implements IUserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    REcUserinfoMapper userinfoMapper;

    @Autowired
    RedisConfig redisConfig;

    private static final String userRedisKey = "UserInfo:";

    @Override
    public boolean checkUserAvailable(Long userId) {
        Jedis jedis = redisConfig.getJedis();
        if (jedis == null) {
            return false;
        }
        String userJson = null;
        try {
            userJson = jedis.get(userRedisKey + userId);
        } catch (Exception e) {
            logger.error("Redis get error: " + e.getMessage() + " - key : " + userId);
        } finally {
            redisConfig.returnResource(jedis);
        }

        if (StringUtils.isEmpty(userJson)) {
            REcUserinfo user =  userinfoMapper.selectByPrimaryKey(userId);
            if (user != null) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
