package org.kraken.core.registry.zookeeper.bean;

import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.bean.URL;
import org.kraken.core.common.exception.RemotingException;
import org.kraken.core.common.utils.ConfigUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kraken.core.common.utils.StringUtils;

import java.util.Locale;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/2 12:24
 */
@Data
@NoArgsConstructor
public class ZkNode extends URL {
    private final String ZK_ROOT_PATH = ConfigUtils.getAppConfigBean().getZkRegisterRootPath();

    /**
     * com.kraken.service.HelloService
     */
    private String serviceName;
    /**
     * provider、consumer
     */
    private RegistryType registryType;

    /**
     * 通过传入的path进行实例化
     * @param path ： /kraken/com.kraken.service.HelloService/providers/192.168.87.139:8080
     */
    public ZkNode(String path) {

        parsePath(path);
    }

    public ZkNode(URL url) {
        setHost(url.getHost());
        setPort(url.getPort());
        setWarmup(url.getWarmup());
        setWeight(url.getWeight());
        setTimestamp(url.getTimestamp());
        setGroup(url.getGroup());
        setVersion(url.getVersion());
        setActive(url.getActive());
        setTimeout(url.getTimeout());
        setVersion_0(url.getVersion_0());
    }

    /**
     * path: 是有编号的，是路径/1/2/3/4/5
     * 1: root_path
     * 2: serviceName
     * 3: registryType
     * 4: host:port
     * @param path : /kraken/com.kraken.service.HelloService/providers/192.168.87.139:8080
     */
    private void parsePath(String path) {

        String[] split = path.substring(0, path.indexOf('?'))
                                .split(Constants.SEPARATOR);
        for (int i = 0; i < split.length; i++) {
            if (i+1 >= split.length) break;
            String str = split[i+1];
            switch (i) {
                case 1:
                    setServiceName(str);
                    break;
                case 2:
                    setRegistryType(RegistryType.valueOf(str.toUpperCase(Locale.ROOT)));
                    break;
                case 3:
                    String[] ip = split[4].split(":");

                    setHost(ip[0]);
                    if (!StringUtils.isDigit(ip[1])) {
                        throw new RemotingException("不存在的端口号: " + ip[1]);
                    }
                    setPort(Integer.valueOf(ip[1]));
                    break;
                default:
                    break;
            }
        }
        // 设置param
        String[] params = path.substring(path.indexOf('?')+1).split("&");

        /*"?version=" + version +
        "&group=" + group +
        "&weight=" + weight +
        "&timestamp=" + timestamp +
        "&warmup=" + warmup;*/
        for (int i = 0; i < params.length; i++) {
            String param = params[i];
            param = param.substring(param.indexOf('=')+1);
            if (param.equalsIgnoreCase("null")) continue;
            switch (i) {

                case 0:
                    setVersion(param);
                    break;
                case 1:
                    setGroup(param);
                    break;
                case 2:
                    int weight = Integer.parseInt(param);
                    setWeight(weight);
                    break;
                case 3:

                    long timestamp = Long.parseLong(param);

                    setTimestamp(timestamp);
                    break;
                case 4:
                    int warmup = Integer.parseInt(param);
                    setWarmup(warmup);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 返回整个 zk叶子节点的路径
     * @return 例子： /kraken/com.kraken.service.HelloService/providers/192.168.87.139:8080
     */
    public String getPath() {
        setTimestamp(System.currentTimeMillis());
        return ZK_ROOT_PATH + Constants.SEPARATOR
                + serviceName + Constants.SEPARATOR
                + registryType.getValue() + Constants.SEPARATOR + getAddress() + getParam();
    }

    public static ZkNode UrlToZkNode(URL url) {
        return new ZkNode(url);
    }


    @Override
    public String toString() {
        return "ZkNode{" +
                "ZK_ROOT_PATH='" + ZK_ROOT_PATH + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", registryType=" + registryType + '\'' +
                ", host=" + getHost() + '\'' +
                ", port=" + getPort() +
                '}';
    }
}
