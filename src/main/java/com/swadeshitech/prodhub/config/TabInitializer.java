package com.swadeshitech.prodhub.config;

import com.swadeshitech.prodhub.entity.Role;
import com.swadeshitech.prodhub.entity.Tab;
import com.swadeshitech.prodhub.repository.TabRepository;
import com.swadeshitech.prodhub.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class TabInitializer implements CommandLineRunner {

    @Autowired
    private TabRepository tabRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        initializePeerComparisonTab();
    }

    private void initializePeerComparisonTab() {
        // Check if Peer Comparison tab already exists
        List<Tab> existingTabs = tabRepository.findAll();
        boolean tabExists = existingTabs.stream()
                .anyMatch(tab -> "Peer Comparison".equals(tab.getName()));

        if (tabExists) {
            log.info("Peer Comparison tab already exists");
            return;
        }

        // Find Manager and Admin roles
        List<Role> allRoles = roleRepository.findAll();
        Role managerRole = allRoles.stream()
                .filter(role -> "Manager".equalsIgnoreCase(role.getName()))
                .findFirst()
                .orElse(null);

        Role adminRole = allRoles.stream()
                .filter(role -> "Admin".equalsIgnoreCase(role.getName()))
                .findFirst()
                .orElse(null);

        if (managerRole == null && adminRole == null) {
            log.warn("Manager or Admin roles not found. Skipping Peer Comparison tab initialization.");
            return;
        }

        // Create tab with Manager and Admin roles
        Set<Role> roles = new HashSet<>();
        if (managerRole != null) {
            roles.add(managerRole);
        }
        if (adminRole != null) {
            roles.add(adminRole);
        }

        Tab peerComparisonTab = new Tab();
        peerComparisonTab.setName("Peer Comparison");
        peerComparisonTab.setLink("/peer-comparison");
        peerComparisonTab.setActive(true);
        peerComparisonTab.setRoles(roles);

        tabRepository.save(peerComparisonTab);
        log.info("Peer Comparison tab created successfully with Manager and Admin roles");
    }
}
