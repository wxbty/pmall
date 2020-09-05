package ink.zfei.user.vo;

import java.io.Serializable;


public class OAuth2AccessTokenBO implements Serializable {

    public String getAccessToken() {
        return accessToken;
    }

    public OAuth2AccessTokenBO setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public OAuth2AccessTokenBO setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public OAuth2AccessTokenBO setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    private String accessToken;

    private String refreshToken;

    private Integer expiresIn;

}
