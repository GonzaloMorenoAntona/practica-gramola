import { Component, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DevicesComponent } from '../devices/devices';
import { PlaylistsComponent } from '../playlists/playlists';
import { ClientComponent } from '../client/client'; 
import { UserService } from '../user';

@Component({
  selector: 'app-music',
  standalone: true,
  imports: [CommonModule, DevicesComponent, PlaylistsComponent, ClientComponent],
  templateUrl: './music.html',
  styleUrls: ['./music.css']
})
export class MusicComponent implements OnDestroy {

  constructor(private userService: UserService) {}
  
  isAdmin: boolean = true;

  // Accedemos al hijo (ClientComponent) para poder decirle que recargue la cola
  @ViewChild(ClientComponent) clientComponent!: ClientComponent;

  toggleMode() {
    this.isAdmin = !this.isAdmin; //para saber si estamos en modo admin o cliente
    this.userService.showNavbar.set(this.isAdmin); //mostrar u ocultar la barra de navegación
  }

  // esta función se ejecuta cuando PlaylistsComponent nos avisa de que ha puesto música
  onPlaylistPlayed() {
    console.log("Playlist activada, mandando señal al cliente...");
    
    // le decimos al componente Cliente que recargue su cola
    if (this.clientComponent) {
      setTimeout(() => {
        this.clientComponent.getRealQueue();
      }, 1500);
    }
  }
  ngOnDestroy() {
    this.userService.showNavbar.set(true);  //si salimos de esta página, asegurar que la barra vuelve a aparecer
  }
}