package com.secpro.platform.test.services;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.PlatformTestLevel;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.test.Activator;
import com.secpro.platform.test.junit.XmlResultPrinter;
import com.secpro.platform.test.testcase.TestClassSample;

/**
 * @author baiyanwei
 */
@ServiceInfo(description = "test Service", configurationPath = "application/services/platformTestService/")
public class PlatformTestService implements BundleListener, IService {
	//
	private static PlatformLogger _logger = PlatformLogger.getLogger(PlatformTestService.class);

	// which test level we will test it.
	@XmlElement(name = "provider")
	private String _provider = "";
	// which test level we will test it.
	@XmlElement(name = "testLevel")
	private String _testLevel = "level-1";
	// what bundles will join the test.
	@XmlElementWrapper(name = "testBundles")
	@XmlElement(name = "Bundle", type = String.class)
	private List<String> _testBundleList = new ArrayList<String>();
	// which test level we will test it.
	@XmlElement(name = "testReportFile")
	private String _testReportFile = "c:/report_" + System.currentTimeMillis() + ".xml";
	@XmlElement(name = "exitOnFinish", type = Boolean.class)
	private Boolean isExitOnFinish = new Boolean(true);

	// remember which bundle we have checked.
	private List<Bundle> _ready4TestBundleList = new ArrayList<Bundle>();
	// key:Class,value:Methods Arrays.
	private HashMap<Class<?>, List<String>> _testClassMap = new HashMap<Class<?>, List<String>>();
	private Boolean _runTests = new Boolean(true);
	private TestResult _testResult = null;
	private String _defaultMethodTestLevel = "level-1";
	private Class<?> testClass=null;

	@Override
	public void start() throws Exception {
		_logger.info("testTargetStart");
		_logger.info("testTargetInfor", this._provider, this._testLevel, this._testBundleList, this._testReportFile);
		// add two basic bundles ,log and core, current test bundle depend on
		// two bundles.
		Bundle[] bundles = Activator.getContext().getBundles();
		for (int i = 0; i < bundles.length; i++) {
			if (_testBundleList.contains(bundles[i].getSymbolicName()) == false) {
				continue;
			}
			if (bundles[i].getSymbolicName().equals("com.secpro.platform.log") || bundles[i].getSymbolicName().equals("com.secpro.platform.core")) {
				synchronized (_ready4TestBundleList) {
					// add into launching.
					_ready4TestBundleList.add(bundles[i]);
				}
				_logger.info("findTestTargetBundle", bundles[i].getSymbolicName());
			}
		}
		//
	}

	@Override
	public void stop() throws Exception {
		_logger.info("testTargetStop");
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		// We just run test once for every bundle,Need to check the bundle is
		// started And We havn't added it.
		if (event.getType() != BundleEvent.STARTED) {
			return;
		}
		if (_ready4TestBundleList.contains(event.getBundle())) {
			return;
		}
		// get bundle's symbolic name.
		String name = event.getBundle().getSymbolicName();
		// check the bundle is in our test list.
		if (_testBundleList.contains(name) == true) {
			synchronized (_ready4TestBundleList) {
				// add into launching.
				_ready4TestBundleList.add(event.getBundle());
			}
			_logger.info("findTestTargetBundle", name);
		}

		// launching
		startTests();
	}

	/**
	 * We don't want to start any tests until all the of the Platform modules
	 * have been started.
	 * 
	 * @return
	 */
	private boolean isPlatformModulesReady() {
		Bundle[] bundles = Activator.getContext().getBundles();
		for (int i = 0; i < bundles.length; i++) {
			String name = bundles[i].getSymbolicName();
			if (name.contains(this._provider) == true && bundles[i].getState() != Bundle.ACTIVE) {
				return false;
			}
		}
		return true;
	}

	/**
	 * We are going to start all the tests. If all the tests that were found are
	 * started then go.
	 */
	private void startTests() {
		// If we have started all the test projects, lets start testing.
		if (_runTests.booleanValue() == true) {

			synchronized (_ready4TestBundleList) {

				new Thread(new Runnable() {

					@Override
					public void run() {

						if (isPlatformModulesReady() == true && _runTests.booleanValue() == true) {

							_runTests = new Boolean(false);

							_logger.info("scanTestCase", PlatformTestService.this._testLevel);

							// Find all the test classes
							for (Bundle bundle : _ready4TestBundleList) {
								_logger.info("scanTestCase", bundle.getSymbolicName());
								findTestClasses(bundle);
							}

							// Run all the tests.
							_logger.info("runningTest");
							launchTest();
							_logger.info("shutDownTest");
							// Stop all the test bundles.
							for (Bundle bundle : _ready4TestBundleList) {
								stopBundle(bundle);
							}

						}

					}
				}).start();

			}
		}
	}

	/**
	 * This method will run all the tests that have been configured
	 */
	private void launchTest() {
		// write the report.
		XmlResultPrinter writer = new XmlResultPrinter(this._testReportFile);
		_testResult = new TestResult();
		_testResult.addListener(writer);

		writer.printHeader(_testResult, 0);
		TestSuite suite = new TestSuite("Run test level on:" + this._testLevel);
		for (Iterator<Class<?>> keyIter = this._testClassMap.keySet().iterator(); keyIter.hasNext();) {
			Class<?> testCaseClass = keyIter.next();
			List<String> testCaseMethodList = this._testClassMap.get(testCaseClass);
			if (testCaseMethodList == null || testCaseMethodList.isEmpty()) {
				suite.addTestSuite((Class<? extends TestCase>) testCaseClass);
			} else {
				for (int i = 0; i < testCaseMethodList.size(); i++) {
					suite.addTest(TestSuite.createTest(testCaseClass, testCaseMethodList.get(i)));
				}
			}
		}
		suite.addTest(new JUnit4TestAdapter(testClass));
		long startTime = System.currentTimeMillis();
		suite.run(_testResult);
		writer.print(_testResult, System.currentTimeMillis() - startTime);
		writer.printFooter(_testResult);
		_logger.info("writeReport");

	}

	/**
	 * According to test level,find which class or method will be added into
	 * test suite
	 * 
	 * @param bundle
	 */
	private void findTestClasses(Bundle bundle) {
		try {
			// get all type from bundle.
			Enumeration<?> entries = bundle.findEntries("", null, true);
			// find which test class or method we need to do.
			while (entries != null && entries.hasMoreElements()) {
				URL url = (URL) entries.nextElement();
				String path = url.getPath();
				if (path == null) {
					continue;
				}
				//
				String className = null;
				// just add resource is end with .class and .java.
				if (path.endsWith(".class") == true) {
					className = path.substring(0, path.lastIndexOf(".class"));
				} else if (path.endsWith(".java") == true) {
					className = path.substring(0, path.lastIndexOf(".java"));
				} else {
					// other types just pass them.
					continue;
				}
				// need to strip off anything before the "com" and replace the
				// "/" with a "."
				int indexOf = className.indexOf("com/");
				if (indexOf != -1) {
					className = className.substring(indexOf);
					className = className.replaceAll("/", ".");
				}
				try {
					// load the class by className.
					Class<?> testTargetClass = bundle.loadClass(className);
					// find the super class ,because we want to check the type.
					Class<?> superClass = testTargetClass.getSuperclass();
					// make sure the class is an extension of JUnit
					// TestCase and we haven't already added it.
					if(testTargetClass.getName().indexOf("TestClassSample")!=-1){
						this.testClass=testTargetClass;
					}
					if (superClass == null || TestCase.class.getName().equals(superClass.getName()) == false || _testClassMap.containsKey(testTargetClass)) {
						continue;
					}
					// if do all level test ,just add all test case .
					if (this._testLevel.indexOf("level-all") != -1) {
						_testClassMap.put(testTargetClass, null);
						continue;
					}
					// check which test level we need to do.
					if (testTargetClass.isAnnotationPresent(PlatformTestLevel.class) == true) {
						// if define the test level on type ,we do test all
						// method in this level
						String targetTestLevel = testTargetClass.getAnnotation(PlatformTestLevel.class).testLevel();
						if (this._testLevel.indexOf(targetTestLevel) != -1) {
							_testClassMap.put(testTargetClass, null);
						}
					} else {
						// check the annotation for method when type's
						// annotation is empty.
						Method[] methods = testTargetClass.getDeclaredMethods();
						ArrayList<String> methodList = new ArrayList<String>();
						for (int i = 0; i < methods.length; i++) {
							// if no annotation on method ,give it a default
							// level.
							String methodTestLevel = this._defaultMethodTestLevel;
							if (methods[i].isAnnotationPresent(PlatformTestLevel.class) == true) {
								methodTestLevel = methods[i].getAnnotation(PlatformTestLevel.class).testLevel();
							}
							if (this._testLevel.indexOf(methodTestLevel) != -1) {
								methodList.add(methods[i].getName());
							}
						}
						if (methodList.isEmpty() == false) {
							_testClassMap.put(testTargetClass, methodList);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}
		} catch (RuntimeException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Called to stop the bundle.
	 */
	private void stopBundle(Bundle event) {
		if (isExitOnFinish) {
			int exitCode = (_testResult.errorCount() == 0 && _testResult.failureCount() == 0) ? 0 : -1;
			System.exit(exitCode);
		}
	}

}
