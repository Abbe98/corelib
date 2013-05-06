package eu.europeana.corelib.db.service.impl;

import java.util.List;

import javax.annotation.Resource;

import eu.europeana.corelib.db.entity.nosql.AccessToken;
import eu.europeana.corelib.db.entity.nosql.RefreshToken;
import eu.europeana.corelib.db.internal.service.OAuth2AccessTokenService;
import eu.europeana.corelib.db.internal.service.OAuth2RefreshTokenService;
import eu.europeana.corelib.db.service.OAuth2TokenService;

public class OAuth2TokenServiceImpl implements OAuth2TokenService {
	
	@Resource
	private OAuth2AccessTokenService accessTokenService;
	
	@Resource
	private OAuth2RefreshTokenService refreshTokenService;

	@Override
	public AccessToken store(AccessToken token) {
		return accessTokenService.store(token);
	}

	@Override
	public RefreshToken store(RefreshToken token) {
		return refreshTokenService.store(token);
	}

	@Override
	public RefreshToken findRefreshTokenByID(String id) {
		return refreshTokenService.findByID(id);
	}

	@Override
	public AccessToken findAccessTokenByID(String id) {
		return accessTokenService.findByID(id);
	}
	
	@Override
	public List<AccessToken> findByClientId(String clientId) {
		return accessTokenService.findByClientId(clientId);
	}
	
	@Override
	public List<AccessToken> findByUserName(String userName) {
		return accessTokenService.findByUserName(userName);
	}
	
	@Override
	public void removeAccessToken(String id) {
		accessTokenService.remove(id);
	}
	
	@Override
	public void removeRefreshToken(String id) {
		refreshTokenService.remove(id);
	}
	
	@Override
	public void removeAccessTokenByRefreshTokenId(String refreshTokenId) {
		accessTokenService.removeByRefreshTokenId(refreshTokenId);
	}
	
	@Override
	public AccessToken findAccessTokenByAuthenticationId(String authId) {
		return accessTokenService.findByAuthenticationId(authId);
	}
	
	@Override
	public void cleanExpiredTokens() {
		accessTokenService.cleanExpiredTokens();
	}

}
