package com.example.restservice.skills;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/skills")
public class SkillsController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SkillsManager skillsManager;

    @PostMapping
    public ResponseEntity<Void> createUserAndSkills(@RequestParam String username, @RequestBody String[] skills) {
        logger.info("Creating user {} and skills {}", username, skills);

        String userId = skillsManager.insertSkills(username, skills);
        logger.info("Created user {}", userId);
        return ResponseEntity.created(URI.create(String.format("/skills/%s", userId))).build();
    }

    @GetMapping
    public ResponseEntity<List<SkilledPerson>> searchSkills(@RequestParam("skill") String skill) {
        logger.info("Searching for skill {}", skill);
        List<SkilledPerson> skilledPeople = skillsManager.searchSkills(skill);
        return ResponseEntity.ok(skilledPeople);
    }
}
