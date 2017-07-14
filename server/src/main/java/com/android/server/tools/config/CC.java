package com.android.server.tools.config;

import com.typesafe.config.*;

import java.io.File;


public class CC {
    private static CC mCC;
    private ConfigObject mpRoot;

    private Config mConfig;
    private String mLogLevel;
    private String mLogPath;
    private int session_expired_time;
    private int max_heartbeat;
    private int max_packet_size;
    private int min_heartbeat;
    private long compress_threshold;
    private int max_hb_timeout_times;
    private int connect_server_port;
    private int gateway_server_port;
    private int admin_server_port;
    private int gateway_client_port;
    private String gateway_server_net;
    private String gateway_server_multicast;
    private String gateway_client_multicast;
    private int conn_work;
    private int http_work;
    private int push_task;
    private int push_client;
    private int ack_timer;
    private int gateway_server_work;
    private int gateway_client_work;
    private long profile_slowly_duration;

    public synchronized static CC getInstance() {
        if (mCC == null) {
            synchronized (Config.class) {
                if (mCC == null)
                    mCC = new CC();
            }
        }
        return mCC;
    }

    public CC() {
    }

    public Config load() {
        mConfig = ConfigFactory.load();//扫描加载所有可用的配置文件
        String custom_conf = "mp.conf";//加载自定义配置, 值来自jvm启动参数指定-Dmp.conf
        if (mConfig.hasPath(custom_conf)) {
            File file = new File(mConfig.getString(custom_conf));
            if (file.exists()) {
                Config custom = ConfigFactory.parseFile(file);
                mConfig = custom.withFallback(mConfig);
            }
        }
        loadData();
        return mConfig;
    }

    private void loadData() {
        Config mp = mConfig.getObject("mp").toConfig();
        mLogLevel = mp.getString("log-level");
        mLogPath = mp.getString("log-conf-path");
        mpRoot = mp.root();

        Config core = mp.getObject("core").toConfig();
        session_expired_time = core.getInt("session-expired-time");
        max_heartbeat = core.getMilliseconds("max-heartbeat").intValue();
        max_packet_size = (int) core.getMemorySize("max-packet-size").toBytes();
        min_heartbeat = core.getMilliseconds("min-heartbeat").intValue();
        compress_threshold = core.getBytes("compress-threshold");

        Config net = mp.getObject("net").toConfig();
        connect_server_port = net.getInt("connect-server-port");
        gateway_server_port = net.getInt("gateway-server-port");
        admin_server_port = net.getInt("admin-server-port");
        gateway_client_port = net.getInt("gateway-client-port");

        gateway_server_net = net.getString("gateway-server-net");
        gateway_server_multicast = net.getString("gateway-server-multicast");
        gateway_client_multicast = net.getString("gateway-client-multicast");

        Config thread = mp.getObject("thread").toConfig();
        Config pool = thread.getObject("pool").toConfig();
        conn_work = pool.getInt("conn-work");
        http_work = pool.getInt("http-work");
        push_task = pool.getInt("push-task");
        push_client = pool.getInt("push-client");
        ack_timer = pool.getInt("ack-timer");
        gateway_server_work = pool.getInt("gateway-server-work");
        gateway_client_work = pool.getInt("gateway-client-work");

        Config monitor = mp.getObject("monitor").toConfig();
        profile_slowly_duration = monitor.getMilliseconds("profile-slowly-duration");
    }

    public boolean udpGateway() {
        return "udp".equals(gateway_server_net);
    }

    public boolean tcpGateway(){
        return "tcp".equals(gateway_server_net);
    }

    public boolean udtGateway(){
        return "udt".equals(gateway_server_net);
    }

//    public boolean useNettyEpoll() {
//        if (!"netty".equals(epoll_provider)) return false;
//        String name = CC.cfg.getString("os.name").toLowerCase(Locale.UK).trim();
//        return name.startsWith("linux");//只在linux下使用netty提供的epoll库
//    }

//    class mp {
//        Config mConfig = CC.cfg.getObject("mp").toConfig();
//        String log_dir = cfg.getString("log-dir");
//        String log_level = cfg.getString("log-level");
//        String log_conf_path = cfg.getString("log-conf-path");
//
//        class core {
//            Config cfg = mConfig.getObject("core").toConfig();
//
//            int session_expired_time = (int) cfg.getDuration("session-expired-time").getSeconds();
//
//            int max_heartbeat = (int) cfg.getDuration("max-heartbeat", TimeUnit.MILLISECONDS);
//
//            int max_packet_size = (int) cfg.getMemorySize("max-packet-size").toBytes();
//
//            int min_heartbeat = (int) cfg.getDuration("min-heartbeat", TimeUnit.MILLISECONDS);
//
//            long compress_threshold = cfg.getBytes("compress-threshold");
//
//            int max_hb_timeout_times = cfg.getInt("max-hb-timeout-times");
//
//            String epoll_provider = cfg.getString("epoll-provider");
//
//            static boolean useNettyEpoll() {
//                if (!"netty".equals(core.epoll_provider)) return false;
//                String name = CC.cfg.getString("os.name").toLowerCase(Locale.UK).trim();
//                return name.startsWith("linux");//只在linux下使用netty提供的epoll库
//            }
//        }
//
//        class net {
//            Config cfg = mConfig.getObject("net").toConfig();
//
//            int connect_server_port = cfg.getInt("connect-server-port");
//            int gateway_server_port = cfg.getInt("gateway-server-port");
//            int admin_server_port = cfg.getInt("admin-server-port");
//            int gateway_client_port = cfg.getInt("gateway-client-port");
//
//            String gateway_server_net = cfg.getString("gateway-server-net");
//            String gateway_server_multicast = cfg.getString("gateway-server-multicast");
//            String gateway_client_multicast = cfg.getString("gateway-client-multicast");
//            int ws_server_port = cfg.getInt("ws-server-port");
//            String ws_path = cfg.getString("ws-path");
//            int gateway_client_num = cfg.getInt("gateway-client-num");
//
//            boolean tcpGateway() {
//                return "tcp".equals(gateway_server_net);
//            }
//
//            boolean udpGateway() {
//                return "udp".equals(gateway_server_net);
//            }
//
//            boolean wsEnabled() {
//                return ws_server_port > 0;
//            }
//
//            boolean udtGateway() {
//                return "udt".equals(gateway_server_net);
//            }
//
//            boolean sctpGateway() {
//                return "sctp".equals(gateway_server_net);
//            }
//
//
//            class public_ip_mapping {
//
//                Map<String, Object> mappings = net.cfg.getObject("public-host-mapping").unwrapped();
//
//                static String getString(String localIp) {
//                    return (String) mappings.get(localIp);
//                }
//            }
//
//            interface snd_buf {
//                Config cfg = net.cfg.getObject("snd_buf").toConfig();
//                int connect_server = (int) cfg.getMemorySize("connect-server").toBytes();
//                int gateway_server = (int) cfg.getMemorySize("gateway-server").toBytes();
//                int gateway_client = (int) cfg.getMemorySize("gateway-client").toBytes();
//            }
//
//            interface rcv_buf {
//                Config cfg = net.cfg.getObject("rcv_buf").toConfig();
//                int connect_server = (int) cfg.getMemorySize("connect-server").toBytes();
//                int gateway_server = (int) cfg.getMemorySize("gateway-server").toBytes();
//                int gateway_client = (int) cfg.getMemorySize("gateway-client").toBytes();
//            }
//
//            interface write_buffer_water_mark {
//                Config cfg = net.cfg.getObject("write-buffer-water-mark").toConfig();
//                int connect_server_low = (int) cfg.getMemorySize("connect-server-low").toBytes();
//                int connect_server_high = (int) cfg.getMemorySize("connect-server-high").toBytes();
//                int gateway_server_low = (int) cfg.getMemorySize("gateway-server-low").toBytes();
//                int gateway_server_high = (int) cfg.getMemorySize("gateway-server-high").toBytes();
//            }
//
//            interface traffic_shaping {
//                Config cfg = net.cfg.getObject("traffic-shaping").toConfig();
//
//                interface gateway_client {
//                    Config cfg = traffic_shaping.cfg.getObject("gateway-client").toConfig();
//                    boolean enabled = cfg.getBoolean("enabled");
//                    long check_interval = cfg.getDuration("check-interval", TimeUnit.MILLISECONDS);
//                    long write_global_limit = cfg.getBytes("write-global-limit");
//                    long read_global_limit = cfg.getBytes("read-global-limit");
//                    long write_channel_limit = cfg.getBytes("write-channel-limit");
//                    long read_channel_limit = cfg.getBytes("read-channel-limit");
//                }
//
//                interface gateway_server {
//                    Config cfg = traffic_shaping.cfg.getObject("gateway-server").toConfig();
//                    boolean enabled = cfg.getBoolean("enabled");
//                    long check_interval = cfg.getDuration("check-interval", TimeUnit.MILLISECONDS);
//                    long write_global_limit = cfg.getBytes("write-global-limit");
//                    long read_global_limit = cfg.getBytes("read-global-limit");
//                    long write_channel_limit = cfg.getBytes("write-channel-limit");
//                    long read_channel_limit = cfg.getBytes("read-channel-limit");
//                }
//
//                interface connect_server {
//                    Config cfg = traffic_shaping.cfg.getObject("connect-server").toConfig();
//                    boolean enabled = cfg.getBoolean("enabled");
//                    long check_interval = cfg.getDuration("check-interval", TimeUnit.MILLISECONDS);
//                    long write_global_limit = cfg.getBytes("write-global-limit");
//                    long read_global_limit = cfg.getBytes("read-global-limit");
//                    long write_channel_limit = cfg.getBytes("write-channel-limit");
//                    long read_channel_limit = cfg.getBytes("read-channel-limit");
//                }
//            }
//        }
//
//        class security {
//
//            Config cfg = mp.cfg.getObject("security").toConfig();
//
//            int aes_key_length = cfg.getInt("aes-key-length");
//
//            String public_key = cfg.getString("public-key");
//
//            String private_key = cfg.getString("private-key");
//
//        }
//
//        class thread {
//
//            Config cfg = mp.cfg.getObject("thread").toConfig();
//
//            class pool {
//
//                Config cfg = thread.cfg.getObject("pool").toConfig();
//
//                int conn_work = cfg.getInt("conn-work");
//                int http_work = cfg.getInt("http-work");
//                int push_task = cfg.getInt("push-task");
//                int push_client = cfg.getInt("push-client");
//                int ack_timer = cfg.getInt("ack-timer");
//                int gateway_server_work = cfg.getInt("gateway-server-work");
//                int gateway_client_work = cfg.getInt("gateway-client-work");
//
//                class event_bus {
//                    Config cfg = pool.cfg.getObject("event-bus").toConfig();
//                    int min = cfg.getInt("min");
//                    int max = cfg.getInt("max");
//                    int queue_size = cfg.getInt("queue-size");
//
//                }
//
//                class mq {
//                    Config cfg = pool.cfg.getObject("mq").toConfig();
//                    int min = cfg.getInt("min");
//                    int max = cfg.getInt("max");
//                    int queue_size = cfg.getInt("queue-size");
//                }
//            }
//        }
//
//        class zk {
//
//            Config cfg = mp.cfg.getObject("zk").toConfig();
//
//            int sessionTimeoutMs = (int) cfg.getDuration("sessionTimeoutMs", TimeUnit.MILLISECONDS);
//
//            String watch_path = cfg.getString("watch-path");
//
//            int connectionTimeoutMs = (int) cfg.getDuration("connectionTimeoutMs", TimeUnit.MILLISECONDS);
//
//            String namespace = cfg.getString("namespace");
//
//            String digest = cfg.getString("digest");
//
//            String server_address = cfg.getString("server-address");
//
//            class retry {
//
//                Config cfg = zk.cfg.getObject("retry").toConfig();
//
//                int maxRetries = cfg.getInt("maxRetries");
//
//                int baseSleepTimeMs = (int) cfg.getDuration("baseSleepTimeMs", TimeUnit.MILLISECONDS);
//
//                int maxSleepMs = (int) cfg.getDuration("maxSleepMs", TimeUnit.MILLISECONDS);
//            }
//        }
//
//        class redis {
////            Config cfg = mp.cfg.getObject("redis").toConfig();
////
////            boolean write_to_zk = cfg.getBoolean("write-to-zk");
////            String password = cfg.getString("password");
////            String clusterModel = cfg.getString("cluster-model");
////
////            List<RedisNode> nodes = cfg.getList("nodes")
////                    .stream()//第一纬度数组
////                    .map(v -> RedisNode.from(v.unwrapped().toString()))
////                    .collect(toCollection(ArrayList::new));
////
////            static boolean isCluster() {
////                return "cluster".equals(clusterModel);
////            }
////
////            static <T> T getPoolConfig(Class<T> clazz) {
////                return ConfigBeanImpl.createInternal(cfg.getObject("mConfig").toConfig(), clazz);
////            }
//        }
//
//        class http {
//
//            Config cfg = mp.cfg.getObject("http").toConfig();
//            boolean proxy_enabled = cfg.getBoolean("proxy-enabled");
//            int default_read_timeout = (int) cfg.getDuration("default-read-timeout", TimeUnit.MILLISECONDS);
//            int max_conn_per_host = cfg.getInt("max-conn-per-host");
//
//
//            long max_content_length = cfg.getBytes("max-content-length");
//
//            Map<String, List<DnsMapping>> dns_mapping = loadMapping();
//
//            static Map<String, List<DnsMapping>> loadMapping() {
//                Map<String, List<DnsMapping>> map = new HashMap<>();
////                cfg.getObject("dns-mapping").forEach((s, v) ->
////                        map.put(s, ConfigList.class.cast(v)
////                                .stream()
////                                .map(cv -> DnsMapping.parse((String) cv.unwrapped()))
////                                .collect(toCollection(ArrayList::new))
////                        )
////                );
//                return map;
//            }
//        }
//
//        class push {
//
//            Config cfg = mp.cfg.getObject("push").toConfig();
//
//            class flow_control {
//
//                Config cfg = push.cfg.getObject("flow-control").toConfig();
//
//                class global {
//                    Config cfg = flow_control.cfg.getObject("global").toConfig();
//                    int limit = cfg.getNumber("limit").intValue();
//                    int max = cfg.getInt("max");
//                    int duration = (int) cfg.getDuration("duration").toMillis();
//                }
//
//                class broadcast {
//                    Config cfg = flow_control.cfg.getObject("broadcast").toConfig();
//                    int limit = cfg.getInt("limit");
//                    int max = cfg.getInt("max");
//                    int duration = (int) cfg.getDuration("duration").toMillis();
//                }
//            }
//        }
//
//        class monitor {
//            Config cfg = mp.cfg.getObject("monitor").toConfig();
//            String dump_dir = cfg.getString("dump-dir");
//            boolean dump_stack = cfg.getBoolean("dump-stack");
//            boolean print_log = cfg.getBoolean("print-log");
//            Duration dump_period = cfg.getDuration("dump-period");
//            boolean profile_enabled = cfg.getBoolean("profile-enabled");
//            Duration profile_slowly_duration = cfg.getDuration("profile-slowly-duration");
//        }
//
//        class spi {
//            Config cfg = mp.cfg.getObject("spi").toConfig();
//            String thread_pool_factory = cfg.getString("thread-pool-factory");
//            String dns_mapping_manager = cfg.getString("dns-mapping-manager");
//        }
//    }

    public String getmLogLevel() {
        return mLogLevel;
    }

    public void setmLogLevel(String mLogLevel) {
        this.mLogLevel = mLogLevel;
    }

    public String getmLogPath() {
        return mLogPath;
    }

    public void setmLogPath(String mLogPath) {
        this.mLogPath = mLogPath;
    }

    public int getSession_expired_time() {
        return session_expired_time;
    }

    public void setSession_expired_time(int session_expired_time) {
        this.session_expired_time = session_expired_time;
    }

    public int getMax_heartbeat() {
        return max_heartbeat;
    }

    public void setMax_heartbeat(int max_heartbeat) {
        this.max_heartbeat = max_heartbeat;
    }

    public int getMax_packet_size() {
        return max_packet_size;
    }

    public void setMax_packet_size(int max_packet_size) {
        this.max_packet_size = max_packet_size;
    }

    public int getMin_heartbeat() {
        return min_heartbeat;
    }

    public void setMin_heartbeat(int min_heartbeat) {
        this.min_heartbeat = min_heartbeat;
    }

    public long getCompress_threshold() {
        return compress_threshold;
    }

    public void setCompress_threshold(long compress_threshold) {
        this.compress_threshold = compress_threshold;
    }

    public int getMax_hb_timeout_times() {
        return max_hb_timeout_times;
    }

    public void setMax_hb_timeout_times(int max_hb_timeout_times) {
        this.max_hb_timeout_times = max_hb_timeout_times;
    }

    public int getConnect_server_port() {
        return connect_server_port;
    }

    public void setConnect_server_port(int connect_server_port) {
        this.connect_server_port = connect_server_port;
    }

    public int getGateway_server_port() {
        return gateway_server_port;
    }

    public void setGateway_server_port(int gateway_server_port) {
        this.gateway_server_port = gateway_server_port;
    }

    public int getAdmin_server_port() {
        return admin_server_port;
    }

    public void setAdmin_server_port(int admin_server_port) {
        this.admin_server_port = admin_server_port;
    }

    public int getGateway_client_port() {
        return gateway_client_port;
    }

    public void setGateway_client_port(int gateway_client_port) {
        this.gateway_client_port = gateway_client_port;
    }

    public String getGateway_server_net() {
        return gateway_server_net;
    }

    public void setGateway_server_net(String gateway_server_net) {
        this.gateway_server_net = gateway_server_net;
    }

    public String getGateway_server_multicast() {
        return gateway_server_multicast;
    }

    public void setGateway_server_multicast(String gateway_server_multicast) {
        this.gateway_server_multicast = gateway_server_multicast;
    }

    public String getGateway_client_multicast() {
        return gateway_client_multicast;
    }

    public void setGateway_client_multicast(String gateway_client_multicast) {
        this.gateway_client_multicast = gateway_client_multicast;
    }


    public int getConn_work() {
        return conn_work;
    }

    public void setConn_work(int conn_work) {
        this.conn_work = conn_work;
    }

    public int getHttp_work() {
        return http_work;
    }

    public void setHttp_work(int http_work) {
        this.http_work = http_work;
    }

    public int getPush_task() {
        return push_task;
    }

    public void setPush_task(int push_task) {
        this.push_task = push_task;
    }

    public int getPush_client() {
        return push_client;
    }

    public void setPush_client(int push_client) {
        this.push_client = push_client;
    }

    public int getAck_timer() {
        return ack_timer;
    }

    public void setAck_timer(int ack_timer) {
        this.ack_timer = ack_timer;
    }

    public int getGateway_server_work() {
        return gateway_server_work;
    }

    public void setGateway_server_work(int gateway_server_work) {
        this.gateway_server_work = gateway_server_work;
    }

    public int getGateway_client_work() {
        return gateway_client_work;
    }

    public void setGateway_client_work(int gateway_client_work) {
        this.gateway_client_work = gateway_client_work;
    }

    public long getProfile_slowly_duration() {
        return profile_slowly_duration;
    }

    public void setProfile_slowly_duration(long profile_slowly_duration) {
        this.profile_slowly_duration = profile_slowly_duration;
    }

    public ConfigObject getMpRoot() {
        return mpRoot;
    }

    public void setMpRoot(ConfigObject mpRoot) {
        this.mpRoot = mpRoot;
    }
}