package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.SettingsRequest;
import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.model.GlobalSetting;
import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagService tagService;

    @GetMapping("/init")
    private InitResponse init() {
        return initResponse;
    }

    @GetMapping("/settings")
    private ResponseEntity<SettingsResponse> settings() {
        return new ResponseEntity<>(settingsService.getGlobalSettings(), HttpStatus.OK);
    }

    @GetMapping("/tag")
    private ResponseEntity<TagResponse> tags(@RequestParam String query) {
        return new ResponseEntity<>(tagService.getTags(query), HttpStatus.OK);
    }
}