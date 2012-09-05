package org.cipango.console;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.management.MBeanServerConnection;

import org.cipango.console.data.Property;
import org.cipango.console.data.PropertyList;
import org.cipango.console.util.PrinterUtil;

public class EnvManager
{
	private MBeanServerConnection _mbsc;
	
	public EnvManager(MBeanServerConnection mbsc)
	{
		_mbsc = mbsc;
	}
	
	
	public PropertyList getVersion() throws Exception
	{
		PropertyList properties = new PropertyList(_mbsc, JettyManager.SERVER, "version");
		Long startupTime = (Long) _mbsc.getAttribute(JettyManager.SERVER, "startupTime");
		properties.add(new Property("Startup Time", new Date(startupTime)));
		properties.add(new Property("Server Uptime", PrinterUtil.getDuration(System.currentTimeMillis() - startupTime)));
		return properties;
	}

	

	public PropertyList getEnvironment()
	{
		PropertyList env = new PropertyList();
		env.setTitle("Environment");
		env.add(new Property("OS / Hardware", System.getProperty("os.name") + " " + System.getProperty("os.version")
				+ " - " + System.getProperty("os.arch")));
		env.add(new Property("Jetty Home", System.getProperty("jetty.home")));
		env.add(new Property("Java Runtime", System.getProperty("java.runtime.name") + " " + System.getProperty("java.runtime.version")));
		
		Runtime r = Runtime.getRuntime();
		long usedMemory = r.totalMemory() - r.freeMemory();
		NumberFormat f = DecimalFormat.getPercentInstance();
		f.setMinimumFractionDigits(1);
		String percentage = f.format(((float) usedMemory) / r.maxMemory());
		env.add(new Property("Memory", (usedMemory >> 20) + " Mb of " + (r.maxMemory() >> 20) + " Mb (" +
				percentage + ")"));
		
		env.add(new Property("Available processors", ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors()));
		StringBuilder sb = new StringBuilder();
		for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments())
			sb.append(arg).append(' ');
		env.add(new Property("VM arguments", sb.toString()));
		
		return env;
	}
	
	public SortedMap<String, String> getSystemProperties()
	{
		Properties properties = System.getProperties();
		
		SortedMap<String, String> map = new TreeMap<String, String>();
		Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();
		while (it.hasNext())
		{
			Entry<Object, Object> entry = it.next();
			map.put((String) entry.getKey(), (String) entry.getValue());	
		}
		return map;
	}
}