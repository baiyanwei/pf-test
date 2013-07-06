package com.secpro.platform.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.secpro.platform.test.services.PlatformTestService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.log.utils.PlatformLogger;

public class Activator implements BundleActivator {
	private static PlatformLogger logger = PlatformLogger.getLogger(Activator.class);
	private static BundleContext context;
	private PlatformTestService platformTestService = null;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		platformTestService = new PlatformTestService();
		ServiceHelper.registerService(platformTestService);
		logger.info("#PL-TEST is started~");
		Activator.context.addBundleListener(platformTestService);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		Activator.context.removeBundleListener(platformTestService);
	}

}
