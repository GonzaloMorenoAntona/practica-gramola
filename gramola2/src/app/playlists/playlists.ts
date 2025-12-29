import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SpotifyService } from '../spotify';


@Component({
  selector: 'app-playlists',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './playlists.html',
  styleUrls: ['./playlists.css']
})
export class PlaylistsComponent implements OnInit {
  playlists: any[] = [];
  playlistError?: string;

  // Evento para avisar al padre
  @Output() playlistPlayed = new EventEmitter<void>();

  constructor(private spoti: SpotifyService) {}

  ngOnInit() {
    this.getPlaylists();
  }

  getPlaylists() {
    this.spoti.getPlaylists().subscribe({
      next: (res: any) => this.playlists = res.items,
      error: () => this.playlistError = "Error cargando playlists"
    });
  }

  selectPlaylist(item: any) {
    console.log('Activando playlist:', item.name);
    this.spoti.playContext(item.uri).subscribe({
      next: () => {
        // Emitimos evento para que el padre se entere
        this.playlistPlayed.emit();
      },
      error: (err) => {
        if(err.status === 404) alert("Abre Spotify primero.");
      }
    });
  }
}