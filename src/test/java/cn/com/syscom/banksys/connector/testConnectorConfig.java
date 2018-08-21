package cn.com.syscom.banksys.connector;

import java.util.Scanner;

import cn.com.syscom.banksys.connector.LoadConfig;

public class testConnectorConfig 
{

	public static void main(String[] args)
	{
		String path;
		LoadConfig config;
	
		if (args.length == 0)
		{
			System.out.println("请输入配置文件路径:");
			
			Scanner in = new Scanner(System.in);
			path = in.nextLine();
			
			System.out.println("The input config file path is " + path);
			
			in.close();
		}
		else
		{
			path = args[0];
		}
		
		config = new LoadConfig(path);
		
		if (config != null)
		{
			config.printProps();
		} else
		{
			System.out.println("Class LoadConfig Initial Failure");
		}
		config.printProps(); 
	}

}
