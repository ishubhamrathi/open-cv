package com.opencv.ama.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Demo-only admin credentials. Real hosts must bring their own security. */
@ConfigurationProperties(prefix = "ama.demo")
public class DemoProperties {

    private String adminUser = "admin";
    private String adminPassword = "admin";

    public String getAdminUser() { return adminUser; }
    public void setAdminUser(String adminUser) { this.adminUser = adminUser; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
}