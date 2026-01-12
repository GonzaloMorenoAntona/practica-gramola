package edu.uclm.esi.gramola.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.uclm.esi.gramola.dao.SongDao;
import edu.uclm.esi.gramola.model.Song;

@Service
public class SongService {

    @Autowired
    private SongDao songDao;

    public void addSong(String title, String artist, String bar) {
        Song song = new Song(title, artist, new Date().toString(), bar);

        this.songDao.save(song);
    }
}
