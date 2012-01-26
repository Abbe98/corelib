/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they 
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "License");
 * you may not use this work except in compliance with the
 * License.
 * You may obtain a copy of the License at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the License is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package eu.europeana.corelib.web.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;

import eu.europeana.corelib.definitions.db.entity.Token;
import eu.europeana.corelib.definitions.db.entity.User;
import eu.europeana.corelib.definitions.exception.ProblemType;
import eu.europeana.corelib.web.email.EmailBuilder;
import eu.europeana.corelib.web.exception.EmailServiceException;
import eu.europeana.corelib.web.service.EmailService;

/**
 * @author Willem-Jan Boogerd <www.eledge.net/contact>
 * 
 * @see eu.europeana.corelib.web.service.EmailService
 */
public abstract class EmailServiceImpl implements EmailService {

	@Resource
	private JavaMailSender mailSender;

	@Override
	public void sendToken(final Token token, final String url) throws EmailServiceException {
		if ( (token == null) || StringUtils.isBlank(token.getToken()) || StringUtils.isBlank(token.getEmail()) || StringUtils.isBlank(url)) {
			throw new EmailServiceException(ProblemType.INVALIDARGUMENTS);
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("token", token.getToken());
		model.put("url", url);
		EmailBuilder builder = createEmailBuilder();
		builder.setModel(model);
		builder.setTemplate("register");
		builder.setEmailTo(token.getEmail());
		mailSender.send(builder);
	}
	
	@Override
	public void sendForgotPassword(final User user, final String url) throws EmailServiceException {
		if ( (user == null) || (user.getId() == null) || StringUtils.isBlank(url)) {
			throw new EmailServiceException(ProblemType.INVALIDARGUMENTS);
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("url", url);
		EmailBuilder builder = createEmailBuilder();
		builder.setModel(model);
		builder.setTemplate("forgotPassword");
		builder.setEmailTo(user.getEmail());
		mailSender.send(builder);
	}
	
	@Override
	public void sendFeedback(String email, String feedback) throws EmailServiceException {
		if ( StringUtils.isBlank(email) || StringUtils.isBlank(feedback)) {
			throw new EmailServiceException(ProblemType.INVALIDARGUMENTS);
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("email", email);
		model.put("feedback", feedback);
		// one email to organisation
		EmailBuilder builder = createEmailBuilder();
		builder.setModel(model);
		builder.setTemplate("userFeedback");
		mailSender.send(builder);
		// and one email to user
		builder.setTemplate("userFeedbackConfirm");
		builder.setEmailTo(email);
		mailSender.send(builder);
	}
	
	/**
	 * This method will be handled by Spring Framework.
	 * No implementation needed
	 * 
	 * @return a instance of EmailBuilder
	 */
	protected abstract EmailBuilder createEmailBuilder();

}
