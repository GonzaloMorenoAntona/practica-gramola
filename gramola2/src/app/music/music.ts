import { Component, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DevicesComponent } from '../devices/devices';
import { PlaylistsComponent } from '../playlists/playlists';
import { ClientComponent } from '../client/client'; 
import { UserService } from '../user';

@Component({
  selector: 'app-music',
  standalone: true,
  // AQUÍ "DECLARAMOS" QUE VAMOS A USAR ESTOS COMPONENTES EN EL HTML
  imports: [CommonModule, DevicesComponent, PlaylistsComponent, ClientComponent],
  templateUrl: './music.html',
  styleUrls: ['./music.css']
})
export class MusicComponent implements OnDestroy {

  constructor(private userService: UserService) {}
  
  isAdmin: boolean = true;

  // Accedemos al hijo (ClientComponent) para poder decirle "¡Actualízate!"
  @ViewChild(ClientComponent) clientComponent!: ClientComponent;

  toggleMode() {
    this.isAdmin = !this.isAdmin;
    this.userService.showNavbar.set(this.isAdmin);
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
  //Si salimos de esta página, asegurar que la barra vuelve a aparecer
  ngOnDestroy() {
    this.userService.showNavbar.set(true);
  }
}