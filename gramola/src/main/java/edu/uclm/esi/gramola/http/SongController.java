package edu.uclm.esi.gramola.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.gramola.services.SongService;

@RestController
@RequestMapping("songs")
@CrossOrigin(origins = "http://127.0.0.1:4200")
public class SongController {

    @Autowired
    private SongService songService; // Usamos el Servicio, NO el Dao

    @PostMapping("/add")
    public void addSong(@RequestBody Map<String, String> info) {
        // El controlador solo extrae los datos del JSON...
        String title = info.get("title");
        String artist = info.get("artist");
        String bar = info.get("bar");

        this.songService.addSong(title, artist, bar);
    }
}