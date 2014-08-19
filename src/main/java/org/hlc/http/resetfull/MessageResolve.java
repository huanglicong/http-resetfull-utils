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
package org.hlc.http.resetfull;

/**
 * JSON结果解析器接口，定义数据解析方法.
 *
 * @author huanglicong
 * @version V1.0
 * @param <E> the element type
 */
public interface MessageResolve<E> {

	/**
	 * 解析json结果,当http请求成功，返回状态200时调用该方法。.
	 *
	 * @param messageType the message type
	 * @param message the json string
	 * @return the e
	 */
	E resolve(String messageType, String message);

	/**
	 * 客户端请求发生错误时调用该方法.
	 *
	 * @param executeCode the execute code
	 * @param messageType the message type
	 * @param message the message
	 */
	void error(int executeCode, String messageType, String message);

}