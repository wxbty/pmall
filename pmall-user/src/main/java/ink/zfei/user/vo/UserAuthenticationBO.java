package ink.zfei.user.vo;

import java.io.Serializable;

//@ApiModel("用户认证 BO")
//@Data  生成get set方法
//@Accessors(chain = true)
public class UserAuthenticationBO implements Serializable {


    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public OAuth2AccessTokenBO getToken() {
        return token;
    }

    public void setToken(OAuth2AccessTokenBO token) {
        this.token = token;
    }

    private String nickname;

    private OAuth2AccessTokenBO token;

}
