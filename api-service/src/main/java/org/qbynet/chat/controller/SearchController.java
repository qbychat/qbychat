package org.qbynet.chat.controller;

import jakarta.annotation.Resource;
import org.qbynet.chat.entity.SearchResult;
import org.qbynet.chat.entity.User;
import org.qbynet.chat.service.SearchService;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Secured("SCOPE_search")
@RequestMapping("/api/search")
public class SearchController {
    @Resource
    SearchService searchService;

    @GetMapping("mixed")
    public ResponseEntity<RestBean<List<SearchResult>>> mixedSearch(@RequestParam(name = "q") String query,
                                                                    @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                                    @RequestAttribute("user") User user) {
        if (query.length() < 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Query too short"));
        }
        return ResponseEntity.ok(RestBean.success(searchService.mixed(query, user, page)));
    }

    @GetMapping("media")
    public ResponseEntity<RestBean<List<SearchResult>>> searchMedia(@RequestParam(name = "q") String query,
                                                                    @RequestParam(name = "type", required = false) String contentType,
                                                                    @RequestParam(name = "page", required = false, defaultValue = "0") int page) {
        if (query.length() < 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Query too short"));
        }
        return ResponseEntity.ok(RestBean.success(searchService.media(query, contentType, page)));
    }

    @GetMapping("tag")
    public ResponseEntity<RestBean<List<SearchResult>>> searchByTag(@RequestParam(name = "q") String query,
                                                                    @RequestParam(name = "page", required = false, defaultValue = "0") int page,
                                                                    @RequestAttribute("user") User user) {
        return ResponseEntity.ok(RestBean.success(searchService.tag(query, user, page)));
    }
}
