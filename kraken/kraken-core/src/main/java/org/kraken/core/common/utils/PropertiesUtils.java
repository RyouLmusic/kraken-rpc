package org.kraken.core.common.utils;


import org.kraken.core.common.exception.AppException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Author: 汉高鼠刘邦
 * @Date: 2022/1/23 22:18
 *
 * 操作properties文件
 */
public class PropertiesUtils {

    /**
     * 加载配置文件中的信息到Properties类中，并返回
     * @param input 配置文件输入流
     * @return Properties类
     */
    public static Properties load (InputStream input) {
        Properties properties = new Properties();

        try {
            properties.load(input);
        } catch (IOException e) {
            throw new AppException("加载.properties配置文件失败");
        }

        return properties;
    }


}
