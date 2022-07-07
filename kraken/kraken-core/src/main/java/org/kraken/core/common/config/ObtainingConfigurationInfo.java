package org.kraken.core.common.config;


import org.kraken.core.common.bean.Constants;
import org.kraken.core.common.exception.AppException;
import org.kraken.core.common.utils.PropertiesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/23 22:10
 *
 * 获取配置信息的类：
 */
public class ObtainingConfigurationInfo {

    private Properties properties;
    private String configFilePath;

    /**
     * 进行获取操作之前的初始化
     * 实例化ObtainingConfigurationInfo对象后必定调用此方法
     */
    public void initObtain() {
        if (configFilePath.trim().equals("")) throw new AppException("文件路径不能为空");
        InputStream inputStream;
        try {
            inputStream = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(configFilePath);

            // 如果传入的path获取失败，使用默认的
            if (inputStream == null || inputStream.available() == 0) {
                /*inputStream = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(Constants.CONFIGURATION_DEFAULT_PATH);*/
                properties = new Properties();
                return;
            }
        } catch (AppException | IOException e) {
            throw new AppException("指定的配置文件的位置不存在");
        }

        properties = PropertiesUtils.load(inputStream);
    }

    public String getProperty(String key, String defaultVal) {
        return properties.getProperty(key, defaultVal);
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String path) {
        this.configFilePath = path;
        // 重新设置了路径，需要再次进行初始化
        initObtain();
    }

}
