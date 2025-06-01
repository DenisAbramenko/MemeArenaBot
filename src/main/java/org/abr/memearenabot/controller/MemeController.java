package org.abr.memearenabot.controller;

import org.abr.memearenabot.model.Meme;
import org.abr.memearenabot.service.MemeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for meme management
 */
@RestController
@RequestMapping("/api/memes")
public class MemeController {
    private static final Logger logger = LoggerFactory.getLogger(MemeController.class);

    private final MemeService memeService;

    @Autowired
    public MemeController(MemeService memeService) {
        this.memeService = memeService;
    }

    /**
     * Get top memes
     */
    @GetMapping("/top")
    public ResponseEntity<List<Meme>> getTopMemes() {
        logger.info("REST request to get top memes");
        return ResponseEntity.ok(memeService.getTopMemes());
    }

    /**
     * Get contest memes
     */
    @GetMapping("/contest")
    public ResponseEntity<List<Meme>> getContestMemes() {
        logger.info("REST request to get contest memes");
        return ResponseEntity.ok(memeService.getContestMemes());
    }

    /**
     * Vote for a meme
     */
    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> voteMeme(@PathVariable Long id) {
        logger.info("REST request to vote for meme : {}", id);
        boolean success = memeService.voteMeme(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get available templates
     */
    @GetMapping("/templates")
    public ResponseEntity<List<String>> getTemplates() {
        logger.info("REST request to get available templates");
        return ResponseEntity.ok(memeService.getAvailableTemplates());
    }
} 