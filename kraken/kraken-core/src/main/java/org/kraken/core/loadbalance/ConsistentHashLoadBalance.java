package org.kraken.core.loadbalance;

import org.kraken.core.common.bean.URL;
import org.kraken.core.remoting.protocol.Request;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.kraken.core.common.utils.ConfigUtils.makeServiceKey;
/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/3/7 21:25
 * 一致性哈希策略
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance{

    // 存放各自的service(请求)对应的hash选择器(环)
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected URL doSelect(List<URL> urls, Request request) {
        int identityHashCode = System.identityHashCode(urls);
        String key = makeServiceKey(request.getInterfaceName(), request.getMethodName(), request.getParamTypes());

        ConsistentHashSelector selector = selectors.get(key);
        // 如果之前没有存入selectors中，或者请求对应的urls集合已经改变(增加或者减少了url)
        // 进行新建一个新的selector，并且存入selectors中
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(key, new ConsistentHashSelector(urls, 160, identityHashCode));
            selector = selectors.get(key);
        }
        return selector.select(key);
    }

    /**
     * 一致哈希环
     */
    static class ConsistentHashSelector {
        // 虚拟节点
        private final TreeMap<Long, URL> virtualNodes;
        // urls的对应的hashcode
        private final int identityHashCode;

        public ConsistentHashSelector(List<URL> nodes, int replicaNumber, int identityHashCode) {
            this.virtualNodes = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            // 对每个URL的node生成replicaNumber个虚拟结点，并存放于TreeMap中
            for (URL node : nodes) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    // 对 address + i 进行 md5 运算,为每4个结点生成一个消息摘要，摘要长为16字节128位。
                    byte[] digest = md5(node.getAddress() + i);

                    // 随后将128位分为4部分，0-31,32-63,64-95,95-128，并生成4个32位数，存于long中，long的高32位都为0
                    // 并作为虚拟结点的key。
                    for (int h = 0; h < 4; h++) {
                        // h = 0 时，取 digest 中下标为 0 ~ 3 的4个字节进行位运算
                        // h = 1 时，取 digest 中下标为 4 ~ 7 的4个字节进行位运算
                        // h = 2, h = 3 时过程同上
                        long m = hash(digest, h);
                        // 将 hash 到 node 的映射关系存储到 virtualNodes 中，
                        // virtualNodes 需要提供高效的查询操作，因此选用 TreeMap 作为存储结构
                        virtualNodes.put(m, node);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 0xFF) << 24 |
                    (long) (digest[2 + idx * 4] & 0xFF) << 16 |
                    (long) (digest[1 + idx * 4] & 0xFF) << 8 |
                    (long) (digest[idx * 4] & 255)) & 0xFFFFFFFFL;
        }

        URL select(Object rpcServiceKey) {
            // 对参数 key 进行 md5 运算
            byte[] digest = md5(rpcServiceKey.toString());
            // 取 digest 数组的前四个字节进行 hash 运算，再将 hash 值传给 selectForKey 方法，
            // 寻找合适的 url
            return selectForKey(hash(digest, 0));
        }

        URL selectForKey(long hashCode) {
            // 到 TreeMap(virtualNodes) 中查找第一个节点值大于或等于当前 hash 的 URL
            Map.Entry<Long, URL> entry = virtualNodes.tailMap(hashCode, true).firstEntry();
            // 如果 hash 大于 virtualNodes 在圆环上最大的位置，此时 entry = null，
            // 需要将 TreeMap 的头结点赋值给 entry
            if (entry == null) {
                entry = virtualNodes.firstEntry();
            }

            return entry.getValue();
        }
    }
}
