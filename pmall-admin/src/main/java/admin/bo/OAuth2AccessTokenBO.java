package admin.bo;

import java.io.Serializable;


public class OAuth2AccessTokenBO implements Serializable {

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    //    @ApiModelProperty(value = "accessToken", required = true, example = "001e8f49b20e47f7b3a2de774497cd50")
    private String accessToken;

//    @ApiModelProperty(value = "refreshToken", required = true, example = "001e8f49b20e47f7b3a2de774497cd50")
    private String refreshToken;

//    @ApiModelProperty(value = "过期时间，单位：秒", required = true, example = "1024")
    private Integer expiresIn;

}
