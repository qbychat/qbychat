package org.qbynet.chat.controller;

import org.qbynet.chat.entity.SearchResult;
import org.qbynet.shared.entity.RestBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    @GetMapping("mixed")
    public ResponseEntity<RestBean<List<SearchResult>>> mixedSearch() {
        return null; // todo search
    }
}
