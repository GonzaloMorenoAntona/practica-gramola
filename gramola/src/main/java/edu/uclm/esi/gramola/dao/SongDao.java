package edu.uclm.esi.gramola.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.uclm.esi.gramola.model.Song;

@Repository
public interface SongDao extends JpaRepository<Song, Long> {
}