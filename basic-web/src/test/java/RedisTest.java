import com.basic.core.redis.utils.RedisUtils;
import com.basic.web.WebApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisClusterNode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SpringBootTest(classes = {WebApplication.class})
class RedisTest {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testWriteToEveryMasterNode() {
        // 1. 获取集群中所有的 Master 节点信息
        RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
        Iterable<RedisClusterNode> nodes = connectionFactory.getClusterConnection().clusterGetNodes();

        Set<String> masterNodeIds = new HashSet<>();
        for (RedisClusterNode node : nodes) {
            if (node.isMaster()) {
                masterNodeIds.add(node.getId());
                System.out.println("发现 Master 节点: " + node.getHost() + ":" + node.getPort() + " (ID: " + node.getId() + ")");
            }
        }

        System.out.println("--- 开始测试：尝试覆盖所有 " + masterNodeIds.size() + " 个主节点 ---");

        Set<String> coveredMasters = new HashSet<>();
        int attempts = 0;
        int maxAttempts = 500; // 防止陷入死循环

        while (coveredMasters.size() < masterNodeIds.size() && attempts < maxAttempts) {
            String testKey = "test_key_" + UUID.randomUUID().toString().substring(0, 8);
            String testValue = "value_" + attempts;

            // 写入数据
            redisUtils.set(testKey, testValue);

            // 获取该 Key 所在的节点信息 (通过 RedisTemplate 获取连接验证)
            // 注意：这里利用 RedisConnection 的 clusterGetNodeForKey 来定位 Key 所属节点
            RedisClusterNode nodeForKey = connectionFactory.getClusterConnection().clusterGetNodeForKey(testKey.getBytes());

            if (nodeForKey != null) {
                String nodeId = nodeForKey.getId();
                if (masterNodeIds.contains(nodeId) && !coveredMasters.contains(nodeId)) {
                    coveredMasters.add(nodeId);
                    System.out.println("成功写入节点 [" + nodeForKey.getHost() + ":" + nodeForKey.getPort() +
                            "] -> Key: " + testKey + " (已覆盖: " + coveredMasters.size() + "/" + masterNodeIds.size() + ")");
                }
            }

            // 验证读取
            Object result = redisUtils.get(testKey);
            assert testValue.equals(result);

            attempts++;
        }

        System.out.println("--- 测试完成 ---");
        System.out.println("总共尝试生成 Key 次数: " + attempts);
        if (coveredMasters.size() == masterNodeIds.size()) {
            System.out.println("结果: 成功覆盖所有物理 Master 节点！");
        } else {
            System.out.println("结果: 未能在限制次数内覆盖所有节点，请检查集群状态。");
        }
    }
}
