/*
 * Copyright 2012-2014 Chengdu Totoole technology co., LTD.
 * 
 * The file is not a public documents, without permission shall not copy, modify and propagate, otherwise, shall be investigated for responsibility according to law.
 */
package org.hlc.http.resetfull.filter;

import org.hlc.http.resetfull.Message;
import org.hlc.http.resetfull.MessageFilter;
import org.hlc.http.resetfull.network.HttpExecutor;


// TODO: Auto-generated Javadoc
/**
 * The Class TextMessageFilter.
 *
 * @author huanglicong
 * @version V2.0
 */
public class TextMessageFilter implements MessageFilter {

	/** The default message filter. */
	private final MessageFilter defaultMessageFilter;

	/**
	 * Instantiates a new text message filter.
	 */
	public TextMessageFilter() {
		this.defaultMessageFilter = new DefaultMessageFilter();
	}

	/** {@inheritDoc} */
	@Override
	public Message resolve(int stausCode, String contentType, String message) {
		if (HttpExecutor.DEFUALT_MESSAGE_TYPE.equals(contentType)) {
			return new Message(stausCode, contentType, message);
		}
		return this.defaultMessageFilter.resolve(stausCode, contentType, message);
	}

}
