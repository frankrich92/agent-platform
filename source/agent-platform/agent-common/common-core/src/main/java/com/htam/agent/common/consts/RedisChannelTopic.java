package com.htam.agent.common.consts;

/**
 * 描述：RedisChannelTopic
 *
 * @author huxuehao
 **/
public class RedisChannelTopic {
    public static final String AGENT_REREGISTER_CHANNEL = "agent:cluster:reRegister";
    public static final String AGENT_UNREGISTER_CHANNEL = "agent:cluster:unRegister";

    public static final String AGENT_CONSTRUCTOR_REREGISTER_CHANNEL = "agent:cluster:constructor:reRegister";
    public static final String AGENT_CONSTRUCTOR_UNREGISTER_CHANNEL = "agent:cluster:constructor:unRegister";

    public static final String SK_SYNC_CHANNEL = "agent:sk:sync";
    public static final String JOB_CLUSTER_CONTROL = "agent:job:cluster:control";
    public static final String WS_CHANNEL_PATTERN = "agent:ws:cluster:*";
}
