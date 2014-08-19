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
 * 用户解析请求中的错误消息.
 *
 * @author huanglicong
 * @version V1.0
 */
public interface MessageFilter {

	/**
	 * 解析错误消息.
	 *
	 * @param stausCode the staus code
	 * @param contentType the content type
	 * @param message the message
	 * @return the message
	 */
	Message resolve(int stausCode, String contentType, String message);

}
