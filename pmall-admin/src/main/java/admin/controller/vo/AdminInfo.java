package admin.controller.vo;

import admin.bean.Admin;
import admin.bean.AdminRole;

public class AdminInfo {

    private Admin admin;

    private AdminRole adminRole;

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public AdminRole getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(AdminRole adminRole) {
        this.adminRole = adminRole;
    }
}
