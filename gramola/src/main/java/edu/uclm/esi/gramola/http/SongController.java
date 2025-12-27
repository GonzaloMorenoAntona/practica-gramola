package edu.uclm.esi.gramola.http;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.gramola.dao.SongDao; 
import edu.uclm.esi.gramola.model.Song;

@RestController
@RequestMapping("songs")
@CrossOrigin(origins = "http://127.0.0.1:4200")
public class SongController {

    @Autowired
    private SongDao songDao; 

    @PostMapping("/add")
    public void addSong(@RequestBody Map<String, String> info) {
        String title = info.get("title");
        String artist = info.get("artist"); 
        String bar = info.get("bar");      
        Song song = new Song(title, artist, new Date().toString(), bar);   
        songDao.save(song); 
    }
}