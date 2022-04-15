package main.controller;

import lombok.AllArgsConstructor;
import main.api.response.CalendarResponse;
import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.service.CalendarService;
import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagService tagService;
    private final CalendarService calendarService;

    @GetMapping("/init")
    private InitResponse init() {
        return initResponse;
    }

    @GetMapping("/settings")
    private ResponseEntity<SettingsResponse> settings() {
        return new ResponseEntity<>(settingsService.getGlobalSettings(), HttpStatus.OK);
    }

    @GetMapping("/tag")
    private ResponseEntity<TagResponse> tags(@RequestParam(defaultValue = "") String query) {
        return new ResponseEntity<>(tagService.getTags(query), HttpStatus.OK);
    }

    @GetMapping("/calendar")
    private ResponseEntity<CalendarResponse> calendar(@RequestParam(defaultValue = "") String year) {
        return new ResponseEntity<>(calendarService.getCalendar(year), HttpStatus.OK);
    }
}