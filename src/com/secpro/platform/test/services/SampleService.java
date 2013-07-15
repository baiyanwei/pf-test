package com.secpro.platform.test.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

//import com.secpro.platform.api.common.http.client.HttpClient;


import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.metrics.Metric;
import com.secpro.platform.core.metrics.MetricUtils;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.api.client.ClientConfiguration;
import com.secpro.platform.api.common.http.client.HttpClient;

@ServiceInfo(description = "sample Service", configurationPath = "application/services/sampleService/")
public class SampleService extends AbstractMetricMBean implements IService {
	private static PlatformLogger logger = PlatformLogger.getLogger(SampleService.class);
	@XmlElement(name = "name")
	private String name = "aa";
	@Metric(description = "This is a test attribute for metric function.")
	public int counter = 10;
	//
	@XmlElementWrapper(name = "datas")
	@XmlElement(name = "data", type = String.class)
	private List<String> dataList = new ArrayList<String>();
	//
	@XmlElement(name = "stringData", type = List.class)
	private List<String> stringDataList = new ArrayList<String>();
	//
	@XmlElementWrapper(name = "cfgBeans")
	@XmlElement(name = "cfgBean", type = CfgBean.class)
	private List<CfgBean> cfgBeanList = new ArrayList<CfgBean>();
	//
	@XmlElement(name = "cfgBean", type = CfgBean.class)
	private CfgBean bean = new CfgBean();

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub
		logger.info("showLogTest", this.getClass().toString());
		logger.info("showAttributeTest", name, "1", "3");
		MetricUtils.registerMBean(this);
		System.out.println(">>>>>>>>>>>>>>" + dataList);
		System.out.println(">>>>>>>>>>>>>>" + stringDataList);
		System.out.println(">>>>>>>>>>>>>>" + cfgBeanList);
		System.out.println("<>>>>>>>>>>>>>>>>bean:" + bean);
		// testClient();
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		logger.info("stop The test service.");
	}

	@Metric(description = "This is a test operation for metric function.")
	public String testMetric4JMX() {
		return this.getClass().getName() + ".testMetric4JMX";
	}

	public void testClient() {
		try {
			final ClientConfiguration clientCfg = new ClientConfiguration();
			clientCfg._endPointURI = "http://localhost:8888/?sdsf=322";
			clientCfg._endPointPort = 8080;
			for (int i = 0; i < 10; i++) {
				try {
					new Thread() {
						public void run() {
							try {
								this.sleep(10000);
								HttpClient httpClient = new HttpClient();
								httpClient.configure(clientCfg);
								httpClient.start();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
