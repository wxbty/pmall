package admin.bo;

public class AdminAuthenticationBO {

//    @ApiModelProperty(value = "管理员编号", required = true, example = "1")
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

    //    @ApiModelProperty(value = "昵称", required = true, example = "小王")
    private String nickname;

    private OAuth2AccessTokenBO token;

}
