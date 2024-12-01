package com.swadeshitech.prodhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.swadeshitech.prodhub.dto.Response;
import com.swadeshitech.prodhub.dto.TeamRequest;
import com.swadeshitech.prodhub.dto.TeamResponse;
import com.swadeshitech.prodhub.services.TeamService;

@RestController
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping("/{id}")
    public ResponseEntity<Response> team(@PathVariable("id") String uuid) {
        
        TeamResponse teamResponse = teamService.getTeamDetail(uuid);
        
        Response response = Response.builder()
            .httpStatus(HttpStatus.OK)
            .message("Team Detail has been fetched successfully")
            .response(teamResponse)
            .build();

        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<Response> team(@RequestBody TeamRequest teamRequest) {
        
        TeamResponse teamResponse = teamService.addTeam(teamRequest);

        Response response = Response.builder()
            .httpStatus(HttpStatus.CREATED)
            .message("Team has been created")
            .response(teamResponse)
            .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
}
