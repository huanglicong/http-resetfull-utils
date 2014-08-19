/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hlc.http.resetfull.network;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.hlc.http.resetfull.Message;
import org.hlc.http.resetfull.MessageFilter;
import org.hlc.http.resetfull.MessageResolve;
import org.hlc.http.resetfull.filter.DefaultMessageFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Perform HTTP requests.
 *
 * @author huanglicong
 * @version V1.0
 */
public class HttpExecutor extends ErrorCode {

	/** The Log. */
	private final Logger Log = LoggerFactory.getLogger(getClass());

	/** 完整日期时间格式:yyyy-MM-dd HH:mm:ss. */
	public static final SimpleDateFormat FULL_DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/** The Constant CONNECTION_TIMEOUT. */
	private final static int CONNECTION_TIMEOUT = 8 * 1000;

	/** The Constant READ_TIMEOUT. */
	private final static int READ_TIMEOUT = 10 * 1000;

	/** 默认的消息类型 */
	public final static String DEFUALT_MESSAGE_TYPE = "text/plain";

	/** 错误消息过滤器 */
	private final MessageFilter messageFilter;

	/** HTTP头参数 */
	private final Map<String, String> httpHeaderParamter;

	/**
	 * 创建一个新的HttpExecutor实例.
	 */
	public HttpExecutor() {
		this.httpHeaderParamter = Maps.newLinkedHashMap();
		this.messageFilter = new DefaultMessageFilter();
	}

	/**
	 * 创建一个新的HttpExecutor实例.
	 *
	 * @param messageFilter
	 */
	public HttpExecutor(MessageFilter messageFilter) {
		this.httpHeaderParamter = Maps.newLinkedHashMap();
		this.messageFilter = messageFilter;
	}

	/**
	 * 创建一个新的HttpExecutor实例.
	 *
	 * @param messageFilter
	 * @param httpHeaderParamter
	 */
	public HttpExecutor(MessageFilter messageFilter, Map<String, String> httpHeaderParamter) {
		this.messageFilter = messageFilter;
		this.httpHeaderParamter = httpHeaderParamter;
	}

	/**
	 * Builds the http params.
	 *
	 * @param readTimeout the read timeout
	 * @return the http params
	 */
	protected HttpParams buildHttpParams(int readTimeout) {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, readTimeout <= 0 ? READ_TIMEOUT : readTimeout);

		return httpParams;
	}

	/**
	 * Do execute.
	 *
	 * @param <E> the element type
	 * @param httpMethod the http method
	 * @param readTimeout the read timeout
	 * @param result the result
	 * @return the e
	 */
	protected <E> E doExecute(HttpUriRequest httpMethod, int readTimeout, MessageResolve<E> result) {
		try {
			// Step1 封装请求
			HttpParams httpParams = buildHttpParams(readTimeout);
			HttpClient httpClient = new DefaultHttpClient(httpParams);
			httpMethod.setHeader("User-Agent", "");
			for (Entry<String, String> item : httpHeaderParamter.entrySet()) {
				httpMethod.setHeader(item.getKey(), item.getValue());
			}
			HttpContext context = new BasicHttpContext();

			// Step2 执行请求
			HttpResponse response = httpClient.execute(httpMethod, context);
			Header contentType = response.getFirstHeader("Content-Type");
			HeaderElement[] elements = contentType.getElements();
			int stausCode = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String responseBody = EntityUtils.toString(entity);
			Log.info(stausCode + "/" + contentType.getValue() + "/" + responseBody);

			// Step3 解析消息
			Message errorMessage = this.messageFilter.resolve(stausCode, elements[0].getName(), responseBody);
			if (errorMessage == null) {
				throw new IllegalArgumentException("HTTP parsing results cannot be empty.");
			}
			if (errorMessage.getCode() != HttpStatus.SC_OK) {
				result.error(errorMessage.getCode(), errorMessage.getMessageType(), errorMessage.getMessage());
				return null;
			}
			return result.resolve(errorMessage.getMessageType(), errorMessage.getMessage());
		} catch (SocketTimeoutException e) {
			Log.debug(e.getMessage());
			result.error(NETWORK_ERROR, DEFUALT_MESSAGE_TYPE, e.getMessage());
			return null;
		} catch (Exception e) {
			Log.debug(e.getMessage());
			result.error(NETWORK_ERROR, DEFUALT_MESSAGE_TYPE, e.getMessage());
			return null;
		}
	}

	/**
	 * Do post.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param result the result
	 * @return the e
	 */
	public <E> E doPost(String url, Map<String, Object> paramter, MessageResolve<E> result) {
		return doPost(url, paramter, null, result);
	}

	/**
	 * Do post.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param files the files
	 * @param result the result
	 * @return the e
	 */
	public <E> E doPost(String url, Map<String, Object> paramter, Map<String, File> files, MessageResolve<E> result) {
		return doPost(url, paramter, files, READ_TIMEOUT, result);
	}

	/**
	 * Do post.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param files the files
	 * @param readTimeout the read timeout
	 * @param result the result
	 * @return the e
	 */
	public <E> E doPost(String url, Map<String, Object> paramter, Map<String, File> files, int readTimeout, MessageResolve<E> result) {
		MultipartEntity multipartEntity = new MultipartEntity();
		try {
			if (paramter != null) {
				for (String name : paramter.keySet()) {
					multipartEntity.addPart(name, new StringBody(String.valueOf(paramter.get(name)), Charset.forName(HTTP.UTF_8)));
				}
			}
			if (files != null) {
				for (String name : files.keySet()) {
					multipartEntity.addPart(name, new FileBody(files.get(name)));
				}
			}
		} catch (Exception e) {
			Log.debug(e.getMessage());
			result.error(NETWORK_ERROR, DEFUALT_MESSAGE_TYPE, e.getMessage());
			return null;
		}
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(multipartEntity);
		return doExecute(httpPost, readTimeout, result);
	}

	/**
	 * Do get.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param result the result
	 * @return the e
	 */
	public <E> E doGet(String url, MessageResolve<E> result) {
		return doExecute(new HttpGet(url), READ_TIMEOUT, result);
	}

	/**
	 * Builds the requst params body.
	 *
	 * @param paramter the paramter
	 * @return the list
	 */
	protected List<NameValuePair> buildRequstParamsBody(Map<String, Object> paramter) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		Set<Entry<String, Object>> entrys = paramter.entrySet();

		String temp = null;
		for (Entry<String, Object> item : entrys) {
			if (item.getValue() instanceof Date) {
				temp = FULL_DATE_TIME_FORMATTER.format((Date) item.getValue());
			} else {
				temp = (item.getValue() != null ? item.getValue().toString() : "");
			}
			nvps.add(new BasicNameValuePair(item.getKey(), temp));
		}
		return nvps;
	}

	/**
	 * Do put.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param result the result
	 * @return the e
	 */
	public <E> E doSimplePost(String url, Map<String, Object> paramter, MessageResolve<E> result) {
		return doSimplePost(url, paramter, READ_TIMEOUT, result);
	}

	/**
	 * Do put.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param readTimeout the read timeout
	 * @param result the result
	 * @return the e
	 */
	public <E> E doSimplePost(String url, Map<String, Object> paramter, int readTimeout, MessageResolve<E> result) {

		List<NameValuePair> nvps = buildRequstParamsBody(paramter);
		HttpPost httpPost = new HttpPost(url);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		return doExecute(httpPost, readTimeout, result);
	}

	/**
	 * Do put.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param result the result
	 * @return the e
	 */
	public <E> E doPut(String url, Map<String, Object> paramter, MessageResolve<E> result) {
		return doPut(url, paramter, READ_TIMEOUT, result);
	}

	/**
	 * Do put.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param readTimeout the read timeout
	 * @param result the result
	 * @return the e
	 */
	public <E> E doPut(String url, Map<String, Object> paramter, int readTimeout, MessageResolve<E> result) {

		List<NameValuePair> nvps = buildRequstParamsBody(paramter);
		nvps.add(new BasicNameValuePair("_method", "put"));

		// 服务器不支持浏览器采用PUT方式，因此采用POST伪装PUT
		// HttpPut httpPut = new HttpPut(url);
		HttpPost httpPut = new HttpPost(url);
		try {
			httpPut.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		return doExecute(httpPut, readTimeout, result);
	}

	/**
	 * Do delete.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param result the result
	 * @return the e
	 */
	public <E> E doDelete(String url, Map<String, Object> paramter, MessageResolve<E> result) {
		return doDelete(url, paramter, READ_TIMEOUT, result);
	}

	/**
	 * Do delete.
	 *
	 * @param <E> the element type
	 * @param url the url
	 * @param paramter the paramter
	 * @param readTimeout the read timeout
	 * @param result the result
	 * @return the e
	 */
	public <E> E doDelete(String url, Map<String, Object> paramter, int readTimeout, MessageResolve<E> result) {

		List<NameValuePair> nvps = buildRequstParamsBody(paramter);
		nvps.add(new BasicNameValuePair("_method", "delete"));

		// 服务器不支持浏览器采用DELETE方式，因此采用POST伪装DELETE
		// HttpDelete httpDelete = new HttpDelete(url);
		HttpPost httpDelete = new HttpPost(url);
		try {
			httpDelete.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		return doExecute(httpDelete, readTimeout, result);
	}
}
