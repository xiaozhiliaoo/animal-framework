package org.lili.forfun.infra.middleware.redis;


public class RedisKey {
    /**
     * `CONF_SERVICE_{ENV}` 服务配置存储set
     */
    private static final String INFO_SUFFIX = "INFO_";

    /**
     * `INFO_NODE_{GROUP_KEY}_{ENV}` 组服务器资源使用信息
     */
    private static final String NODE_INFO_SUFFIX = "INFO_NODE_";

    /**
     *
     */
    private static final String GROUP_INFO_SUFFIX = "INFO_GROUP_";

    /**
     * CONF_SERVICE_{ENV} 服务配置存储set
     */
    private static final String SERVICE_CONFIG_SUFFIX = "CONF_SERVICE_";

    /**
     * `MIG_QUEUE_{SERVICE_KEY}_{ENV}` 任务调度QUEUE
     */
    private static final String QUEUE_SUFFIX = "MIG_QUEUE_";
    /**
     * `MIG_TASK_{SERVICEKEY}_{ENV}` 保存任务的TASK
     */
    private static final String TASK_SUFFIX = "MIG_TASK_";

    /**
     * CONF_APP_{APP_KEY}_{ENV} 应用配置存储key
     */
    private static final String APP_CONFIG_SUFFIX = "CONF_APP_";
    /**
     * CONF_GROUP_{GROUP_KEY}_{ENV} 组配置存储key
     */
    private static final String GROUP_CONFIG_SUFFIX = "CONF_GROUP_";
    /**
     * CONF_APP_KEY_SET_{ENV} 应用列表存储key
     */
    private static final String APP_KEY_SET = "CONF_APP_KEY_SET_";
    /**
     * CONF_APP_KEY_MAPPING_{ENV} 存储GW中appkey映射到UDS中的appKey
     */
    private static final String APP_KEY_MAPPING = "CONF_APP_KEY_MAPPING_";
    /**
     * NLU_BUSINESS_CONFIG_SUFFIX_{BUSINESS_ID}_{ENV} nlu业务id配置详情
     */
    private static final String NLU_BUSINESS_CONFIG_SUFFIX = "CONF_NLU_BUSINESS_";

    /**
     * NLU_MODEL_LOAD_STATUS_SUFFIX_{UPDATE_ID}_{ENV} nlu业务id配置详情
     */
    private static final String NLU_MODEL_LOAD_STATUS_SUFFIX = "NLU_MODEL_LOAD_STATUS_";
    
    /**
     * 存储服务的KEY
     *
     * @param env
     * @return
     */
    public static String getServiceConfigKey(String serviceKey, String appKey, String env) {
        return INFO_SUFFIX + serviceKey + "_" + appKey + "_" + env;
    }

    /**
     * 存储服务的KEY
     *
     * @param env
     * @return
     */
    static String getServiceConfigKey(String env) {
        return SERVICE_CONFIG_SUFFIX + env;
    }

    /**
     * 存储服务器资源信息的KEY
     *
     * @param groupKey
     * @param env
     * @return
     */
    static String getNodeInfoKey(String groupKey, String env) {
        return NODE_INFO_SUFFIX + groupKey + "_" + env;
    }

    /**
     * 任务分发QUEUE的KEY
     *
     * @param serviceKey
     * @param env
     * @return
     */
    static String getQueueKey(String serviceKey, String env) {
        return QUEUE_SUFFIX + serviceKey + "_" + env;
    }

    static String getTaskKey(String env) {
        return TASK_SUFFIX + env;
    }

    /**
     * 从合法的groupKey中取前面的前缀出来，为serviceKey。
     * <br/>
     * 合法的groupKey格式为 serviceKey_env_group000X
     *
     * @param groupKey
     * @return
     */
    public static String getServiceKeyFromGroupKey(String groupKey) {
        return groupKey.substring(0, groupKey.indexOf("_"));
    }

    /**
     * 存储服务的KEY
     *
     * @param env
     * @return
     */
    public static String getAppConfigKey(String appkey, String env) {
        return APP_CONFIG_SUFFIX + appkey + "_" + env;
    }

    /**
     * 存储服务器资源信息的KEY
     *
     * @param groupKey
     * @param env
     * @return
     */
    public static String getGroupConfigKey(String groupKey, String env) {
        return GROUP_CONFIG_SUFFIX + groupKey + "_" + env;
    }

    public static String getGroupStatusKey(String groupKey, String env) {
        return GROUP_INFO_SUFFIX + groupKey + "_" + env;
    }

    /**
     * 存储appkey列表的KEY
     *
     * @param env
     * @return
     */
    public static String getAppSetKey(String env) {
        return APP_KEY_SET + env;
    }

    /**
     * 存储GW中appkey映射到UDS中的appKey。
     *
     * @param env
     * @return
     */
    public static String getAppKeyMappingKey(String env) {
        return APP_KEY_MAPPING + env;
    }

    public static String getBusinessConfigKey(String businessId, String env) {
        return NLU_BUSINESS_CONFIG_SUFFIX + businessId + "_" + env;
    }

    public static String getNluModelLoadStatusKey(String updateId, String env) {
        return NLU_MODEL_LOAD_STATUS_SUFFIX + updateId + "_" + env;
    }
}
