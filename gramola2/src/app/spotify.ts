// src/app/services/spotify.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SpotifyService {

  // URLs de Spotify
  private readonly authorizeUrl: string = 'https://accounts.spotify.com/authorize';
  private readonly redirectUrl: string = 'http://127.0.0.1:4200/callback'; // Debe coincidir con la app de Spotify
  private readonly spotiV1Url: string = 'https://api.spotify.com/v1';

  // Scopes solicitados (Figura 16 del PDF)
  private readonly scopes: string[] = [
    "user-read-private", "user-read-email", "playlist-read-private",
    "playlist-read-collaborative", "user-read-playback-state",
    "user-modify-playback-state", "user-read-currently-playing",
    "user-library-read", "user-library-modify", "user-read-recently-played",
    "user-top-read", "app-remote-control", "streaming"
  ];

  // URL base de tu backend para el intercambio de código (debe coincidir con el del controlador)
  private readonly backendUrl: string = 'http://127.0.0.1:8080/spoti';

  constructor(private http: HttpClient) { }

  /**
   * Inicia el flujo de autorización OAuth 2.0 con Spotify.
   * Recupera el client_id de sessionStorage, genera el state,
   * construye la URL y redirige el navegador.
   */
  getToken(): void {
    // 1. Recuperar clientId de sessionStorage
    const clientId = sessionStorage.getItem('clientId');
    if (!clientId) {
      console.error('SpotifyService: No se encontró clientId en sessionStorage. Debe iniciar sesión primero.');
      return;
    }

    // 2. Generar state
      const state = this.generateString();

    // 4. Construir parámetros de la URL
    let params = "response_type=code";
    params += `&client_id=${sessionStorage.getItem("clientId")}`;
    params += `&scope=${encodeURIComponent(this.scopes.join(" "))}`;
    params += `&redirect_uri=${this.redirectUrl}`;
    params += `&state=${state}`;

    sessionStorage.setItem('oauth_state', state);
    let url = this.authorizeUrl + "?" + params
    window.location.href = url;   
  }

  getAuthorizationToken(code: string): Observable<any> {
      // 1. Recuperar clientId de sessionStorage
    let url = `${this.backendUrl}/getAuthorizationToken?code=${code}&clientId=${sessionStorage.getItem("clientId")}`;
    return this.http.get(url)
  }


    /**
     * Genera un string aleatorio para el parámetro state (prevención de CSRF).
     * @returns El string generado.
     */
  private generateString(): string {
    // Ejemplo simple, puedes usar librerías más robustas
    return Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
  }

  getDevices(): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${sessionStorage.getItem("spotify_access_token")}` 
    });
    let url = `${this.spotiV1Url}/me/player/devices`;
  return this.http.get<any>(url, { headers } );
  }

  playContext(uri: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${sessionStorage.getItem("spotify_access_token")}`
    });

    const body = {
      context_uri: uri // El DNI de la playlist
    };
    return this.http.put(`${this.spotiV1Url}/me/player/play`, body, { headers });
}

  getPlaylists(): Observable<any> {
    const headers = new HttpHeaders({
      // Usamos el mismo token que ya guarde en el callback
      'Authorization': `Bearer ${sessionStorage.getItem("spotify_access_token")}`
    });
    // La URL base (https://api.spotify.com/v1) + el endpoint
    let url = `${this.spotiV1Url}/me/playlists`;
  return this.http.get<any>(url, { headers });
  }

  getPlaylistTracks(playlistId: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${sessionStorage.getItem("spotify_access_token")}`
    });
    return this.http.get(`${this.spotiV1Url}/playlists/${playlistId}/tracks`, { headers });
  }

  search(q: string, type: string = 'track'): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${sessionStorage.getItem("spotify_access_token")}`
    });
    // Endpoint oficial de búsqueda
    let url = `${this.spotiV1Url}/search?q=${encodeURIComponent(q)}&type=${type}`;
    return this.http.get(url, { headers });
  }

  addToQueue(uri: string): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${sessionStorage.getItem("spotify_access_token")}`
    });
    const url = `${this.spotiV1Url}/me/player/queue?uri=${uri}`;
    return this.http.post(url, null, { headers, responseType: 'text' as 'json' }); 
  }
  prepareSongPayment(songName: string): Observable<any> {
    return this.http.post('http://127.0.0.1:8080/payments/prepay-song', { songName });
  }
  saveSongInDb(track: any, barName: string): Observable<any> {
    const url = 'http://127.0.0.1:8080/songs/add';
    const body = {
      title: track.name,
      artist: track.artists[0].name,
      bar: barName 
    };
    return this.http.post(url, body);
  }

  getUserQueue(): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${sessionStorage.getItem("spotify_access_token")}`
    });
    // Llamamos al endpoint oficial de la cola
    return this.http.get(`${this.spotiV1Url}/me/player/queue`, { headers });
  }
}
