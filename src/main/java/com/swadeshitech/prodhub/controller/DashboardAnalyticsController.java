package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.*;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.service.DashboardAnalyticsService;
import com.swadeshitech.prodhub.utils.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard-analytics")
public class DashboardAnalyticsController {

    @Autowired
    private DashboardAnalyticsService dashboardAnalyticsService;

    @GetMapping("/deployments")
    public ResponseEntity<Response> getDeploymentAnalytics() {
        String userId = UserContextUtil.getUserIdFromRequestContext();
        DeploymentAnalyticsDTO analytics = dashboardAnalyticsService.getDeploymentAnalytics(userId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Deployment analytics fetched successfully")
                .response(analytics)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/builds")
    public ResponseEntity<Response> getBuildAnalytics() {
        String userId = UserContextUtil.getUserIdFromRequestContext();
        BuildAnalyticsDTO analytics = dashboardAnalyticsService.getBuildAnalytics(userId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Build analytics fetched successfully")
                .response(analytics)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/overall")
    public ResponseEntity<Response> getOverallAnalytics() {
        String userId = UserContextUtil.getUserIdFromRequestContext();
        OverallAnalyticsDTO analytics = dashboardAnalyticsService.getOverallAnalytics(userId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Overall analytics fetched successfully")
                .response(analytics)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/peer-comparison/team/{teamId}")
    public ResponseEntity<Response> getTeamPeerComparison(@PathVariable String teamId) {
        var comparison = dashboardAnalyticsService.getTeamPeerComparison(teamId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Team peer comparison fetched successfully")
                .response(comparison)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/peer-comparison/department/{departmentId}")
    public ResponseEntity<Response> getDepartmentPeerComparison(@PathVariable String departmentId) {
        var comparison = dashboardAnalyticsService.getDepartmentPeerComparison(departmentId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Department peer comparison fetched successfully")
                .response(comparison)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/deployment-time/user/{userId}")
    public ResponseEntity<Response> getDeploymentTimeComparison(@PathVariable String userId) {
        var comparison = dashboardAnalyticsService.getDeploymentTimeComparison(userId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Deployment time comparison fetched successfully")
                .response(comparison)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/deployment-time/team/{teamId}")
    public ResponseEntity<Response> getTeamDeploymentTimeComparison(@PathVariable String teamId) {
        var comparison = dashboardAnalyticsService.getTeamDeploymentTimeComparison(teamId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Team deployment time comparison fetched successfully")
                .response(comparison)
                .build();

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/deployment-time/department/{departmentId}")
    public ResponseEntity<Response> getDepartmentDeploymentTimeComparison(@PathVariable String departmentId) {
        var comparison = dashboardAnalyticsService.getDepartmentDeploymentTimeComparison(departmentId);

        Response response = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Department deployment time comparison fetched successfully")
                .response(comparison)
                .build();

        return ResponseEntity.ok().body(response);
    }
}
