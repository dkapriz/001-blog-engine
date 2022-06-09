package main.controller;

import lombok.AllArgsConstructor;
import main.api.request.CommentRequest;
import main.api.request.ModeratePostRequest;
import main.api.request.ProfileRequest;
import main.api.request.SettingsRequest;
import main.api.response.*;
import main.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagService tagService;
    private final PostService postService;
    private final UserService userService;
    private final StatisticService statisticService;
    private final ImageService imageService;

    @GetMapping("/init")
    public InitResponse init() {
        return initResponse;
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingsResponse> settings() {
        return new ResponseEntity<>(settingsService.getGlobalSettings(), HttpStatus.OK);
    }

    @GetMapping("/tag")
    public ResponseEntity<TagResponse> tags(@RequestParam(defaultValue = "") String query) {
        return new ResponseEntity<>(tagService.getTags(query), HttpStatus.OK);
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> calendar(@RequestParam(defaultValue = "") String year) {
        return new ResponseEntity<>(postService.getCalendar(year), HttpStatus.OK);
    }

    @PostMapping(value = "/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> loadImage(MultipartFile image) throws IOException {
        return new ResponseEntity<>(imageService.loadImage(image), HttpStatus.OK);
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<IDResponse> addComment(@RequestBody CommentRequest commentRequest) {
        return new ResponseEntity<>(postService.addComment(commentRequest), HttpStatus.OK);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<ResultResponse> moderatePost(@RequestBody ModeratePostRequest moderatePostRequest) {
        return new ResponseEntity<>(postService.moderate(moderatePostRequest), HttpStatus.OK);
    }

    @PostMapping(value = "/profile/my",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> editUserProfile(
            @RequestParam("photo") MultipartFile photo,
            @ModelAttribute ProfileRequest profileRequest) throws IOException {
        return new ResponseEntity<>(userService.editProfile(photo, profileRequest), HttpStatus.OK);
    }

    @PostMapping("/profile/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> editUserProfile(
            @RequestBody ProfileRequest profileRequest) {
        return new ResponseEntity<>(userService.editProfile(profileRequest), HttpStatus.OK);
    }

    @GetMapping("/statistics/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<StatisticResponse> getMyStatistic() {
        return new ResponseEntity<>(statisticService.getMyStatistic(), HttpStatus.OK);
    }

    @GetMapping("/statistics/all")
    public ResponseEntity<StatisticResponse> getAllStatistic() {
        return new ResponseEntity<>(statisticService.getAllStatistic(), HttpStatus.OK);
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public void saveSettings(@RequestBody SettingsRequest settingsRequest) {
        settingsService.setGlobalSettings(settingsRequest);
    }
}