// callback.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SpotifyService } from '../spotify'; // Ajusta la ruta si es necesario

@Component({
  selector: 'app-callback',
  standalone: true,
  imports: [], // Añade imports si necesitas mostrar un mensaje de carga, etc.
  templateUrl: './callback.html',
  styleUrl: './callback.css'
})
export class CallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private spotifyService: SpotifyService // Inyecta SpotifyService (o SpotiService si lo llamaste así)
  ) { }

  ngOnInit(): void {
    // 1. Recuperar parámetros de la URL
    const qp = this.route.snapshot.queryParamMap;
    const code = qp.get('code');
    const state = qp.get('state');
    const error = qp.get('error');

    // 2. Manejar posibles errores de Spotify
    if (error) {
      console.error('CallbackComponent: Error recibido de Spotify:', error);
      // Opcional: mostrar mensaje en la vista
      alert(`Error de Spotify: ${error}`);
      this.router.navigateByUrl('/login'); // O a donde consideres
      return;
    }

    // 3. Verificar que se recibieron code y state
    if (!code || !state) {
      console.error('CallbackComponent: No se recibió code o state de Spotify.');
      alert("No hay código o estado"); // O manejo más elegante
      // Opcional: redirigir a login o página de error
      this.router.navigateByUrl('/login');
      return;
    }

   history.replaceState({}, '', '/callback'); 
 
    this.spotifyService.getAuthorizationToken(code).subscribe({ 
      next: (data) => { 
        sessionStorage.setItem("spotify_access_token", data.access_token); 
        this.router.navigateByUrl('/music'); 
      }, 
      error: (err) => { 
        console.error('Error fetching access token:', err); 
        // AÑADE ESTO:
        alert("Error conectando con el servidor de Spotify. Mira la consola (F12)");
        this.router.navigateByUrl('/login'); // Te devuelve al inicio para reintentar
      }
    });
}

}