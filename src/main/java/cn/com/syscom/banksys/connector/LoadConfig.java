package cn.com.syscom.banksys.connector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadConfig 
{
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Properties properties;
	private String configFilePath;

	public LoadConfig() 
	{
		this.configFilePath = new String("CommConfig.properties");
		
		LoadProperties();
	}

	public LoadConfig(String path) 
	{
		//path = path.trim();
		//this.configFilePath = path;
		
		if (setFilePath(path))
		{
			LoadProperties();
			logger.info("已读取通讯配置文件 " + this.configFilePath);
		} else
		{
			logger.error("读取配置文件 " + this.configFilePath + "出错！");
		}
	}

	public Boolean fileExists(String fileName)
	{
		File file = new File(fileName);
		
		if (file.isFile())
		{
			return true;
		} else
		{
			return false;
		}
	}
	
	public Boolean setFilePath(String path)
	{
		path = path.trim();
		
		if (fileExists(path))
		{
			this.configFilePath = path;
			return true;
		} 
				
		String userdir = System.getProperty("user.dir");
		String AbsolutePath = userdir + File.separator + path;

		if (fileExists(AbsolutePath))
		{
			this.configFilePath = AbsolutePath;
			return true;
		}
		
		return false;
	}

	public void LoadProperties() 
	{
		properties = new Properties();
		Reader in = null;

		try {
			in = new BufferedReader(new FileReader(configFilePath));
			properties.load(in);
		} catch (IOException e) 
		{
			logger.error("打开通讯配置文件 " + configFilePath + "出错！");
			e.printStackTrace();
		} finally
		{
			try
			{
				in.close();
			} catch (IOException e)
			{
				logger.warn("关闭BufferedReader出错！");
				e.printStackTrace();
			}
		}
	}

	public String getProperty(String key) 
	{
		String value;

		value = properties.getProperty(key);
		return value;
	}
	
	public Properties returnProperties()
	{
		return this.properties;
	}

	public void printProps() 
	{
		Iterator<String> it = properties.stringPropertyNames().iterator();

		while (it.hasNext()) 
		{
			String key = it.next();
			System.out.println("the Property " + key + " = " + properties.getProperty(key));
		}
	}
}