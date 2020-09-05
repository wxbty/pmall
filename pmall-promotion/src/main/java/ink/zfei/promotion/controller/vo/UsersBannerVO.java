package ink.zfei.promotion.controller.vo;

public class UsersBannerVO {

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    //    @ApiModelProperty(value = "跳转链接", required = true, example = "http://www.baidu.com")
    private String url;
//    @ApiModelProperty(value = "图片链接", required = true, example = "http://www.iocoder.cn/01.jpg")
    private String picUrl;

}
