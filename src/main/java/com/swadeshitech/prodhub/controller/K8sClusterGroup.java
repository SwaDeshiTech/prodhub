package com.swadeshitech.prodhub.controller;

import com.swadeshitech.prodhub.dto.K8sClusterGroupRequest;
import com.swadeshitech.prodhub.dto.K8sClusterGroupResponse;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.services.K8sClusterGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/k8sClusterGroup")
public class K8sClusterGroup {

    @Autowired
    private K8sClusterGroupService k8sClusterGroupService;

    @PostMapping
    public ResponseEntity<Response> createClusterGroup(@RequestBody K8sClusterGroupRequest request) {
        K8sClusterGroupResponse response = k8sClusterGroupService.createClusterGroup(request);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.CREATED)
                .message("K8s cluster group created successfully")
                .response(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<Response> getClusterGroup(@PathVariable String groupId) {
        K8sClusterGroupResponse response = k8sClusterGroupService.getClusterGroup(groupId);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("K8s cluster group fetched successfully")
                .response(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<Response> getAllClusterGroups() {
        List<K8sClusterGroupResponse> response = k8sClusterGroupService.getAllClusterGroups();
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("K8s cluster groups fetched successfully")
                .response(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<Response> updateClusterGroup(@PathVariable String groupId, @RequestBody K8sClusterGroupRequest request) {
        K8sClusterGroupResponse response = k8sClusterGroupService.updateClusterGroup(groupId, request);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("K8s cluster group updated successfully")
                .response(response)
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Response> deleteClusterGroup(@PathVariable String groupId) {
        k8sClusterGroupService.deleteClusterGroup(groupId);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("K8s cluster group deleted successfully")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/{groupId}/clusters/{clusterId}")
    public ResponseEntity<Response> addClusterToGroup(@PathVariable String groupId, @PathVariable String clusterId) {
        k8sClusterGroupService.addClusterToGroup(groupId, clusterId);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Cluster added to group successfully")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }

    @DeleteMapping("/{groupId}/clusters/{clusterId}")
    public ResponseEntity<Response> removeClusterFromGroup(@PathVariable String groupId, @PathVariable String clusterId) {
        k8sClusterGroupService.removeClusterFromGroup(groupId, clusterId);
        Response apiResponse = Response.builder()
                .httpStatus(HttpStatus.OK)
                .message("Cluster removed from group successfully")
                .build();
        return ResponseEntity.ok().body(apiResponse);
    }
}
