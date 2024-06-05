package com.hamsterwhat.wechat.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Set value with expiration time
     *
     * @param key     the key
     * @param value   the value
     * @param timeout expiration time in seconds
     */
    public boolean set(String key, Object value, long timeout) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            logger.error("Failed to set value to redis", e);
            return false;
        }
    }

    /**
     * Set value without expiration time
     *
     * @param key   the key
     * @param value the value
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("Failed to set value to redis", e);
            return false;
        }
    }

    /**
     * Get value by key
     *
     * @param key the key
     * @return the value
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * Delete value by key
     *
     * @param key the key
     * @return true if deleted, otherwise false
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * Delete multiple keys
     *
     * @param keys the keys
     * @return true if all keys are deleted, otherwise false
     */
    public boolean delete(Collection<String> keys) {
        try {
            Long deletedCount = redisTemplate.delete(keys);
            return deletedCount != null && deletedCount == keys.size();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if key exists
     *
     * @param key the key
     * @return true if key exists, otherwise false
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Set expiration time for key
     *
     * @param key     the key
     * @param timeout expiration time in seconds
     * @return true if expiration time set, otherwise false
     */
    public boolean expire(String key, long timeout) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, TimeUnit.SECONDS));
    }

    /**
     * Get expiration time of key
     *
     * @param key the key
     * @return expiration time in seconds, -1 if the key does not exist or no expiration time is set
     */
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return Optional.ofNullable(expire).orElse(-1L);
    }

    /**
     * Adds a single value to the beginning of a list and sets an expiration time.
     *
     * @param key     the key of the list
     * @param timeout the expiration time in seconds
     * @param value   the value to be added
     */
    public void leftPush(String key, long timeout, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * Adds a list of values to the beginning of a list and sets an expiration time.
     *
     * @param key     the key of the list
     * @param timeout the expiration time in seconds
     * @param values  the values to be added
     */
    public void leftPushAll(String key, long timeout, Collection<Object> values) {
        redisTemplate.opsForList().leftPushAll(key, values);
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * Adds a single value to the end of a list and sets an expiration time.
     *
     * @param key     the key of the list
     * @param timeout the expiration time in seconds
     * @param value   the value to be added
     */
    public void rightPush(String key, long timeout, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * Adds a list of values to the end of a list and sets an expiration time.
     *
     * @param key     the key of the list
     * @param timeout the expiration time in seconds
     * @param values  the values to be added
     */
    public void rightPushAll(String key, long timeout, Collection<Object> values) {
        redisTemplate.opsForList().rightPushAll(key, values);
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * Removes and returns the first element of a list.
     *
     * @param key the key of the list
     * @return the first element of the list, or null if the list is empty
     */
    public Object leftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * Removes and returns the last element of a list.
     *
     * @param key the key of the list
     * @return the last element of the list, or null if the list is empty
     */
    public Object rightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * Returns a range of elements from a list.
     *
     * @param key   the key of the list
     * @param start the start index (0-based)
     * @param end   the end index (0-based, inclusive). Use -1 to go up to the last element.
     * @return a list of elements in the specified range
     */
    public List<Object> listRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * Returns the length of a list.
     *
     * @param key the key of the list
     * @return the length of the list
     */
    public long listLen(String key) {
        Long length = redisTemplate.opsForList().size(key);
        return Optional.ofNullable(length).orElse(-1L);
    }

    /**
     * Adds one single member to a set with an optional TTL.
     *
     * @param key   the key
     * @param value the value to add to the set
     * @param ttl   the TTL (Time To Live) in seconds, if null no TTL is set
     */
    public void addToSet(String key, Long ttl, Object value) {
        redisTemplate.opsForSet().add(key, value);
        if (ttl != null) {
            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        }
    }

    /**
     * Adds a list of members to a set with an optional TTL.
     *
     * @param key    the key
     * @param values the values to add to the set
     * @param ttl    the TTL (Time To Live) in seconds, if null no TTL is set
     */
    public void addToSet(String key, Long ttl, Collection<Object> values) {
        redisTemplate.opsForSet().add(key, values);
        if (ttl != null) {
            redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        }
    }

    /**
     * Removes one or more members from a set.
     *
     * @param key    the key
     * @param values the values to remove from the set
     * @return the number of members removed from the set, not including non-existing members
     */
    public Long removeFromSet(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * Checks if a member is part of a set.
     *
     * @param key   the key
     * @param value the value to check for
     * @return true if the value is a member of the set, false otherwise
     */
    public Boolean isMemberOfSet(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * Gets all members of a set.
     *
     * @param key the key
     * @return the members of the set, or an empty set if the key does not exist
     */
    public Set<Object> getMembersOfSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Sets a value in Redis with an optional TTL.
     *
     * @param key   the key
     * @param value the value
     * @param ttl   the TTL (Time To Live) in seconds, if null no TTL is set
     */
    public void set(String key, Object value, Long ttl) {
        if (ttl != null) {
            redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }
}
