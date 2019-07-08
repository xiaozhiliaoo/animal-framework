package org.lili.forfun.infra.engine.message;


public enum UpdateType {
    /**
     * 落组
     */
    LAND_RELOAD,
    /**
     * 迁组
     */
    MIGRATE_RELOAD,
    /**
     * 重新加载
     */
    UPDATE_RELOAD,
    /**
     * 卸载
     */
    UNLOAD,
    /**
     * 配置更新
     */
    REFRESH_RELOAD,
    /**
     * 更新阈值
     */
    THRESHOLD_RELOAD,
    /**
     * 更新nlu+中tagger词典
     */
    NLU_PLUS_TAGGER_RELOAD,
    /**
     * 更新nlu+中jsgf文件
     */
    NLU_PLUS_JSGF_RELOAD,
    /**
     * 加载nlu+中model文件
     */
    NLU_PLUS_MODEL_LAND_RELOAD,
    /**
     * 更新nlu+中model文件
     */
    NLU_PLUS_MODEL_RELOAD,
    /**
     * 卸载nlu+中model文件
     */
    NLU_PLUS_MODEL_UNLOAD,
    /**
     * 重载通用技能
     */
    NLU_PLUS_COMMON_RELOAD,
    /**
     * 更新配置通知
     */
    UPDATE_CONFIG
}
