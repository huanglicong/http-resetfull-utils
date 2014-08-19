/*
 * Copyright 2012-2014 Chengdu Totoole technology co., LTD.
 * 
 * The file is not a public documents, without permission shall not copy, modify and propagate, otherwise, shall be investigated for responsibility according to law.
 */
package org.hlc.http.resetfull.filter;

import org.hlc.http.resetfull.MessageResolve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class TextPrintMessageResolve.
 *
 * @author huanglicong
 * @version V2.0
 */
public class TextPrintMessageResolve implements MessageResolve<String> {
	/** The Log. */
	private final Logger Log = LoggerFactory.getLogger(getClass());

	/** {@inheritDoc} */
	@Override
	public String resolve(String messageType, String message) {
		Log.info("\r\n消息类型:" + messageType + "\r\n消息内容:" + message);
		return message;
	}

	/** {@inheritDoc} */
	@Override
	public void error(int executeCode, String messageType, String message) {
		Log.info("\r\n错误代码:" + executeCode + "\r\n消息类型:" + messageType + "\r\n消息内容:" + message);
	}

}
