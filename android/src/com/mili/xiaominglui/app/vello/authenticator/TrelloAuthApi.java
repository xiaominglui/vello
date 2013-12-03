package com.mili.xiaominglui.app.vello.authenticator;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class TrelloAuthApi extends DefaultApi10a {
	private static final String AUTHORIZE_URL = "https://trello.com/1/OAuthAuthorizeToken?oauth_token=%s&name=Vello&scope=read,write&expiration=never";
	
	@Override
	public String getAccessTokenEndpoint() {
		return "https://trello.com/1/OAuthGetAccessToken";
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return String.format(AUTHORIZE_URL, requestToken.getToken());
	}

	@Override
	public String getRequestTokenEndpoint() {
		return "https://trello.com/1/OAuthGetRequestToken";
	}

}
