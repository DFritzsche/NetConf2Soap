/* Copyright (C) 2018 Daniel Fritzsche, Pierluigi Greto */

package com.technologies.highstreet.netconf2soapmediator.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.technologies.highstreet.netconf2soapmediator.server.networkelement.Netconf2SoapNetworkElement;


public class HTTPServlet extends HttpServlet {

	private static final long serialVersionUID = 5071770086030271370L;
	private static Netconf2SoapNetworkElement networkElement = null;
	private static boolean connActive = false;
	private static boolean setParam = false;
	private static CWMPMessage CWMPmsg = new CWMPMessage();

	public static Map<Integer, ArrayList<String>> setParamMap = new HashMap<Integer, ArrayList<String>>();
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		System.out.println(HTTPServlet.getBody(request));
		response.getWriter().println("Get Hello World!");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		System.out.println("Received msg from device");
		System.out.println(request);

		final String reqBody = HTTPServlet.getBody(request);
		StringBuilder sb = new StringBuilder(10);

		if (reqBody.contains("Fault")) {
			System.out.println("Received Fault msg");
			return;
		}
		else if (reqBody.contains("cwmp:Inform")) {
			sb = handleInform(reqBody);
		}
		else if (reqBody.contains("cwmp:GetParameterValuesResponse")) {
			sb = handleGetParameterValuesResponse(reqBody);
		}
		else if (reqBody.contains("cwmp:SetParameterValuesResponse")) {
			sb = handleSetParameterValuesResponse(reqBody);
		}
		else if (reqBody.contains("cwmp:GetParameterAttributesResponse")) {
			sb = handleGetParameterAttributesResponse(reqBody);
		}
		else if (reqBody.equals("")) {
			sb = handleEmptyResponse(reqBody);
		}
		else {
			System.out.println("Received Unknown msg");
			return;
		}

		System.out.println("Sending HTTP reply:");
		System.out.println(sb);
		response.getWriter().println(sb);
	}

	public static String getBody(HttpServletRequest request) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

	public static  StringBuilder handleEmptyResponse(String reqBody) {
		System.out.println("Received HTTP request: Empty");

		StringBuilder sb = new StringBuilder(10);
		sb = CWMPmsg.getParameterValues();

		return sb;
	}

	public static  StringBuilder handleGetParameterValuesResponse(String reqBody) {
		System.out.println("Received GetParameterValuesResponse msg");

		StringBuilder sb = new StringBuilder(10);

		if (getSetParam() == true) {
			sb = CWMPmsg.setParameterValues(setParamMap);
		} else {
			networkElement.setTr069DocumentCFromString(reqBody);
			sb = CWMPmsg.getParameterAttributes();
		}

		return sb;
	}

	public static  StringBuilder handleSetParameterValuesResponse(String reqBody) {
		System.out.println("Received SetParameterValuesResponse msg");

		setSetParam(false);
		StringBuilder sb = new StringBuilder(10);
		sb = CWMPmsg.getParameterValues();

		return sb;
	}

	public static  StringBuilder handleGetParameterAttributesResponse(String reqBody) {
		System.out.println("Received GetParameterAttributesResponse msg");

		StringBuilder sb = new StringBuilder(10);

		setConnActive(false);
		
		return sb;
	}

	public static  StringBuilder handleInform(String reqBody) {
		if (reqBody.contains("<EventCode>0 BOOTSTRAP")) {
			System.out.println("Received Inform msg with event code: 0 (BOOTSTRAP)");
		}
		else if (reqBody.contains("<EventCode>1 BOOT")) {
			if (reqBody.contains("<EventCode>2 PERIODIC")) {
				System.out.println("Received Inform msg with event code: 1 (BOOT PERIODIC REQUEST)");
			} else {
				System.out.println("Received Inform msg with event code: 1 (BOOT)");
			}
		}
		else if (reqBody.contains("<EventCode>2 PERIODIC")) {
			System.out.println("Received Inform msg with event code: 2 (PERIODIC REQUEST)");
		}
		else if (reqBody.contains("<EventCode>6 CONNECTION REQUEST")) {
			System.out.println("Received Inform msg with event code: 6 (CONNECTION REQUEST)");
		}
		else {
			System.out.println("Received Inform msg (unknown event code)");
		}

		setConnActive(true);
		networkElement.setTr069DocumentCFromString(reqBody);

		StringBuilder sb = new StringBuilder(10);
		sb = CWMPmsg.getInformResponse();

		return sb;
	}

	public static Netconf2SoapNetworkElement getNetworkElement() {
		return networkElement;
	}

	public static void setNetworkElement(Netconf2SoapNetworkElement networkElement) {
		HTTPServlet.networkElement = networkElement;
	}

	public static boolean getSetParam() {
		return setParam;
	}

	public static void setSetParam(boolean setParam) {
		HTTPServlet.setParam = setParam;
	}

	public static boolean getConnActive() {
		return connActive;
	}

	public static void setConnActive(boolean connActive) {
		HTTPServlet.connActive = connActive;
	}

}
