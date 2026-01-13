// callback.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SpotifyService } from '../spotify'; 

@Component({
  selector: 'app-callback',
  standalone: true,
  imports: [], 
  templateUrl: './callback.html',
  styleUrl: './callback.css'
})
export class CallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private spotifyService: SpotifyService 
  ) { }

  ngOnInit(): void {
    // extrae los parámetros de la URL
    const qp = this.route.snapshot.queryParamMap; 
    const code = qp.get('code'); //cupon temporal
    const state = qp.get('state');
    const error = qp.get('error');

    // manejar posibles errores de Spotify
    if (error) {
      console.error('CallbackComponent: Error recibido de Spotify:', error);
      alert(`Error de Spotify: ${error}`);
      this.router.navigateByUrl('/login'); //redirige a login o página de error
      return;
    }

    // verificar que se recibieron code y state
    if (!code || !state) {
      console.error('CallbackComponent: No se recibió code o state de Spotify.');
      alert("No hay código o estado"); 
      this.router.navigateByUrl('/login');
      return;
    }

   history.replaceState({}, '', '/callback'); // limpia la URL y la deja con el /callback
    // intercambia el código por un token de acceso
    this.spotifyService.getAuthorizationToken(code).subscribe({ 
      next: (data) => { 
        sessionStorage.setItem("spotify_access_token", data.access_token); 
        this.router.navigateByUrl('/music'); 
      }, 
      error: (err) => { 
        console.error('Error fetching access token:', err); 
        alert("Error conectando con el servidor de Spotify. Mira la consola (F12)");
        this.router.navigateByUrl('/login'); // te devuelve al inicio para reintentar
      }
    });
}

}