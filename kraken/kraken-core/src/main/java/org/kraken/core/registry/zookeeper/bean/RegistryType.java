package org.kraken.core.registry.zookeeper.bean;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/2/2 12:22
 */
public enum RegistryType {

    PROVIDER("1", "provider"),
    CONSUMER("2", "consumer");

    private String key;

    private String value;

    RegistryType(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static String getValueByKey(String key) {
        for (RegistryType type : RegistryType.values()) {
            if (type.getKey().equals(key)) {
                return type.getValue();
            }
        }
        return null;
    }

    public static String getKeyByValue(String value) {
        for (RegistryType type : RegistryType.values()) {
            if (type.getValue().equals(value)) {
                return type.getKey();
            }
        }
        return null;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }
}
