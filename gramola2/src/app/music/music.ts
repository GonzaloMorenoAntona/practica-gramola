// src/app/music/music.component.ts
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SpotifyService } from '../spotify'; 

@Component({
  selector: 'app-music',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './music.html',
  styleUrl: './music.css'
})
export class MusicComponent implements OnInit {

  devices: any[] = [];
  currentDevice: any;
  playlists : any[] = []; // En el documento: PlayList[]
  queue : any[] = [];     // En el documento: TrackObject[]
  tracks : any[] = [];    // En el documento: TrackObject[]

  title = 'Lost';
  artist = 'LP';

  deviceError? : string;
  playlistError? : string;
  currentPlaylistError? : string;
  songError? : string;

  constructor(private spoti : SpotifyService) {}

  ngOnInit(): void {
    this.getDevices();
    // this.getPlaylists();
    // this.getCurrentPlayList();
  }


  resetErrors() {
    this.deviceError = undefined;
    this.playlistError = undefined;
    this.currentPlaylistError = undefined;
    this.songError = undefined;
  }

  getDevices() {
    this.resetErrors();
    console.log('Access Token:', sessionStorage.getItem('spotify_access_token'));
    this.spoti.getDevices().subscribe({
      next: (result) => {
        console.log('Respuesta de Spotify:', result);
        this.devices = result.devices;
        this.currentDevice = this.devices.find((d => d.is_active));
        if (!this.currentDevice) {
          this.deviceError = "No hay ningún dispositivo conectado";
        }
      },
      error: (err) => {
        console.error('MusicComponent: Error al cargar dispositivos:', err);
        if (err.error && err.error.message) {
            this.deviceError = err.error.message;
        } else if (err.message) {
            this.deviceError = err.message;
        } else {
            this.deviceError = 'Error desconocido al cargar dispositivos.';
        }
        this.devices = []; 
        this.currentDevice = null;
      }
    });
  }

}
