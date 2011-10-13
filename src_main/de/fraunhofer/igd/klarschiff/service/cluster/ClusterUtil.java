package de.fraunhofer.igd.klarschiff.service.cluster;

import java.net.InetAddress;

import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.commons.lang.StringUtils;

public class ClusterUtil {
	
	static String serverConnectorPort;
	static String serverIp;
	static String serverName;
	
	public static String getServerConnectorPort() {
		if (serverConnectorPort==null)
			try {
				String p = MBeanServerFactory.findMBeanServer(null).get(0).queryNames(new ObjectName("Catalina:type=ThreadPool,*"), null).iterator().next().getKeyProperty("name");
				p = p.substring(1, p.length()-1);
				serverConnectorPort = p;
			} catch (Exception e) {}
		 return serverConnectorPort;
	}
	
	public static String getServerIp() {
		if (serverIp==null)
			try {
				InetAddress addrs[] = InetAddress.getAllByName(getServerName());

				String ip = "";
				for (InetAddress addr: addrs) {
					if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) ip = ip + addr.getHostAddress()+", ";
				}
				serverIp = StringUtils.isBlank(ip) ? "UNKNOWN" : ip.substring(0, ip.length()-2);
			} catch (Exception e) {}
		 return serverIp;
	}

	public static String getServerName() {
		if (serverName==null)
			try {
				serverName = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {}
			return serverName;
	}
}
