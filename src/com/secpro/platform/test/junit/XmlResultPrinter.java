package com.secpro.platform.test.junit;

import java.io.File;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;
import junit.textui.ResultPrinter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.secpro.platform.test.Activator;

/**
 * This class will listen for the test class events and create an xml structure
 * as follows
 */
public class XmlResultPrinter implements TestListener {

	private static final double ONE_SECOND = 1000.0;
	private String _reportFile = "";

	class MyResultPrinter extends ResultPrinter {

		public MyResultPrinter(PrintStream writer) {
			super(writer);
		}

		public void resultPrinter(TestResult result, long runTime) {
			printHeader(runTime);
			printErrors(result);
			printFailures(result);
			printFooter(result);
		}

	};

	MyResultPrinter _resultPrinter = new MyResultPrinter(System.out);

	/** constant for unnnamed testsuites/cases */
	private static final String UNKNOWN = "unknown";

	private static DocumentBuilder getDocumentBuilder() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (Exception exc) {
			throw new ExceptionInInitializerError(exc);
		}
	}

	public static final String ISO8601_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

	/**
	 * The XML document.
	 */
	private Document doc;
	/**
	 * The wrapper for the whole testsuite.
	 */
	private Element rootElement;
	/**
	 * Element for the current test.
	 */
	private Hashtable<Test, Object> testElements = new Hashtable<Test, Object>();
	/**
	 * tests that failed.
	 */
	private Hashtable<Test, Object> failedTests = new Hashtable<Test, Object>();

	/**
	 * tests that failed.
	 */
	private Hashtable<String, Element> classElements = new Hashtable<String, Element>();

	/**
	 * Timing helper.
	 */
	private Hashtable<Test, Long> testStarts = new Hashtable<Test, Long>();

	public XmlResultPrinter(String reportFile) {
		this._reportFile = reportFile;
	}

	/*
	 * API for use by textui.TestRunner
	 */

	public void print(TestResult result, long runTime) {
		_resultPrinter.resultPrinter(result, runTime);
	}

	/*
	 * Internal methods
	 */

	public void printHeader(TestResult result, long runTime) {
		doc = getDocumentBuilder().newDocument();
		Element suitesRootElement = doc.createElement(ReportConstants.TESTSUITES);
		rootElement = doc.createElement(ReportConstants.TESTSUITE);
		suitesRootElement.appendChild(rootElement);

		ProcessingInstruction instruction = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"junit-noframes.xsl\"");
		doc.appendChild(instruction);

		// add the timestamp
		final String timestamp = new SimpleDateFormat(ISO8601_DATETIME_PATTERN).format(new Date());
		rootElement.setAttribute(ReportConstants.TIMESTAMP, timestamp);
		rootElement.setAttribute(ReportConstants.HOSTNAME, getHostname());
		rootElement.setAttribute(ReportConstants.ATTR_NAME, Activator.getContext().getBundle().getSymbolicName());

		// Output properties
		Element propsElement = doc.createElement(ReportConstants.PROPERTIES);
		rootElement.appendChild(propsElement);

		doc.appendChild(suitesRootElement);
	}

	/**
	 * get the local hostname
	 * 
	 * @return the name of the local host, or "localhost" if we cannot work it
	 *         out
	 */
	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "localhost";
		}
	}

	public void printFooter(TestResult result) {
		rootElement.setAttribute(ReportConstants.ATTR_TESTS, "" + result.runCount());
		rootElement.setAttribute(ReportConstants.ATTR_FAILURES, "" + result.failureCount());
		rootElement.setAttribute(ReportConstants.ATTR_ERRORS, "" + result.errorCount());

		// rootElement.setAttribute(
		// ATTR_TIME, "" + (result.() / ONE_SECOND));
		try {
			// Prepare the DOM document for writing
			Source source = new DOMSource(doc);

			// Prepare the output file
			String fileName = this._reportFile;
			if (fileName != null && fileName.trim().length() > 0) {
				File file = new File(fileName);
				Result xmlResult = new StreamResult(file);

				// Write the DOM document to the file
				Transformer xformer = TransformerFactory.newInstance().newTransformer();
				xformer.transform(source, xmlResult);
			}
		} catch (TransformerConfigurationException e) {
		} catch (TransformerException e) {
		}
	}

	/**
	 * Returns the formatted string of the elapsed time. Duplicated from
	 * BaseTestRunner. Fix it.
	 */
	protected String elapsedTimeAsString(long runTime) {
		return NumberFormat.getInstance().format((double) runTime / 1000);
	}

	/**
	 * Interface TestListener.
	 * 
	 * <p>
	 * A new Test is started.
	 * 
	 * @param t
	 *            the test.
	 */
	public void startTest(Test test) {
		String testMethodName = JUnitVersionHelper.getTestCaseName(test);
		String testCaseClassName = JUnitVersionHelper.getTestCaseClassName(test);
		System.out.println("#---starting test at (" + testCaseClassName + ", " + testMethodName + ")---#");
		testStarts.put(test, new Long(System.currentTimeMillis()));
	}

	/**
	 * Interface TestListener.
	 * 
	 * <p>
	 * A Test is finished.
	 * 
	 * @param test
	 *            the test.
	 */
	public void endTest(Test test) {
		// Fix for bug #5637 - if a junit.extensions.TestSetup is
		// used and throws an exception during setUp then startTest
		// would never have been called
		if (!testStarts.containsKey(test)) {
			startTest(test);
		}

		Element currentTest = null;
		if (!failedTests.containsKey(test)) {
			String testMethodName = JUnitVersionHelper.getTestCaseName(test);
			String testCaseClassName = JUnitVersionHelper.getTestCaseClassName(test);

			Element classElement = classElements.get(testCaseClassName);
			if (classElement == null) {
				classElement = doc.createElement("class");
				classElements.put(testCaseClassName, classElement);
				rootElement.appendChild(classElement);
				classElement.setAttribute(ReportConstants.ATTR_CLASSNAME, testCaseClassName);
			}

			currentTest = doc.createElement(ReportConstants.TESTCASE);
			currentTest.setAttribute(ReportConstants.ATTR_NAME, testMethodName == null ? UNKNOWN : testMethodName);
			// a TestSuite can contain Tests from multiple classes,
			// even tests with the same name - disambiguate them.
			currentTest.setAttribute(ReportConstants.ATTR_CLASSNAME, testCaseClassName);

			// currentTest.setAttribute(ReportConstants.ATTR_VALUE,
			// ((TestCase)test).);

			classElement.appendChild(currentTest);
			testElements.put(test, currentTest);
			System.out.println("#---end test(" + testCaseClassName + ", " + testMethodName + ")--#");
		} else {
			currentTest = (Element) testElements.get(test);
		}

		Long l = (Long) testStarts.get(test);
		currentTest.setAttribute(ReportConstants.ATTR_TIME, "" + ((System.currentTimeMillis() - l.longValue()) / ONE_SECOND));

	}

	/**
	 * Interface TestListener for JUnit &lt;= 3.4.
	 * 
	 * <p>
	 * A Test failed.
	 * 
	 * @param test
	 *            the test.
	 * @param t
	 *            the exception.
	 */
	public void addFailure(Test test, Throwable t) {
		formatError(ReportConstants.FAILURE, test, t);
	}

	/**
	 * Interface TestListener for JUnit &gt; 3.4.
	 * 
	 * <p>
	 * A Test failed.
	 * 
	 * @param test
	 *            the test.
	 * @param t
	 *            the assertion.
	 */
	public void addFailure(Test test, AssertionFailedError t) {
		addFailure(test, (Throwable) t);
	}

	/**
	 * Interface TestListener.
	 * 
	 * <p>
	 * An error occurred while running the test.
	 * 
	 * @param test
	 *            the test.
	 * @param t
	 *            the error.
	 */
	public void addError(Test test, Throwable t) {
		formatError(ReportConstants.ERROR, test, t);
	}

	private void formatError(String type, Test test, Throwable t) {
		if (test != null) {
			endTest(test);
			failedTests.put(test, test);
		}

		Element nested = doc.createElement(type);
		Element currentTest = null;
		if (test != null) {
			currentTest = (Element) testElements.get(test);
		} else {
			currentTest = rootElement;
		}

		currentTest.appendChild(nested);

		String message = t.getMessage();
		if (message != null && message.length() > 0) {
			nested.setAttribute(ReportConstants.ATTR_MESSAGE, t.getMessage());
		}
		nested.setAttribute(ReportConstants.ATTR_TYPE, t.getClass().getName());

		String strace = BaseTestRunner.getFilteredTrace(t);
		Text trace = doc.createTextNode(strace);
		nested.appendChild(trace);
	}

	private void formatOutput(String type, String output) {
		Element nested = doc.createElement(type);
		rootElement.appendChild(nested);
		nested.appendChild(doc.createCDATASection(output));
	}

	/** {@inheritDoc}. */
	public void setSystemOutput(String out) {
		formatOutput(ReportConstants.SYSTEM_OUT, out);
	}

	/** {@inheritDoc}. */
	public void setSystemError(String out) {
		formatOutput(ReportConstants.SYSTEM_ERR, out);
	}

}
