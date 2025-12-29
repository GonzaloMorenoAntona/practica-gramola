import { Component, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
// IMPORTAMOS TUS NUEVOS COMPONENTES
import { DevicesComponent } from '../devices/devices';
import { PlaylistsComponent } from '../playlists/playlists';
import { ClientComponent } from '../client/client'; // <--- El que acabas de crear

@Component({
  selector: 'app-music',
  standalone: true,
  // AQUÍ "DECLARAMOS" QUE VAMOS A USAR ESTOS COMPONENTES EN EL HTML
  imports: [CommonModule, DevicesComponent, PlaylistsComponent, ClientComponent],
  templateUrl: './music.html',
  styleUrls: ['./music.css']
})
export class MusicComponent {
  
  isAdmin: boolean = false;

  // Accedemos al hijo (ClientComponent) para poder decirle "¡Actualízate!"
  @ViewChild(ClientComponent) clientComponent!: ClientComponent;

  toggleMode() {
    this.isAdmin = !this.isAdmin;
  }

  // Esta función se ejecuta cuando PlaylistsComponent nos avisa de que ha puesto música
  onPlaylistPlayed() {
    console.log("Playlist activada, mandando señal al cliente...");
    
    // Le decimos al componente Cliente que recargue su cola
    // Esperamos 1.5s a que Spotify procese la orden
    if (this.clientComponent) {
      setTimeout(() => {
        this.clientComponent.getRealQueue();
      }, 1500);
    }
  }
}