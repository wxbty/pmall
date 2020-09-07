package admin.controller.vo;

import java.util.List;

public class AdminMenuTreeNodeVO {

    private Integer id;
    //    @ApiModelProperty(value = "菜单名", required = true, example = "商品管理")
//    private String name;

    private String handler;

    private Integer pid;

    private Integer sort;

    private String displayName;

    private List<AdminMenuTreeNodeVO> children;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<AdminMenuTreeNodeVO> getChildren() {
        return children;
    }

    public void setChildren(List<AdminMenuTreeNodeVO> children) {
        this.children = children;
    }
}
