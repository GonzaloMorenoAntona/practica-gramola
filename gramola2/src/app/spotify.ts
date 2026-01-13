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
  private readonly redirectUrl: string = 'http://127.0.0.1:4200/callback'; 
  private readonly spotiV1Url: string = 'https://api.spotify.com/v1';

  // Scopes solicitados 
  private readonly scopes: string[] = [
    "user-read-private", "user-read-email", "playlist-read-private",
    "playlist-read-collaborative", "user-read-playback-state",
    "user-modify-playback-state", "user-read-currently-playing",
    "user-library-read", "user-library-modify", "user-read-recently-played",
    "user-top-read", "app-remote-control", "streaming"
  ];

  // URL base del backend para el intercambio de código 
  private readonly backendUrl: string = 'http://127.0.0.1:8080/spoti';

  constructor(private http: HttpClient) { }

  getToken(): void {
    // recuperar clientId de sessionStorage
    const clientId = sessionStorage.getItem('clientId');
    if (!clientId) {
      console.error('SpotifyService: No se encontró clientId en sessionStorage. Debe iniciar sesión primero.');
      return;
    }

    // generar state, que sirve para asegurarse de que el usuario que inicia sesión es el mismo que vuelve
      const state = this.generateString();

    // construir parámetros de la URL
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
      // recuperar clientId de sessionStorage
    let url = `${this.backendUrl}/getAuthorizationToken?code=${code}&clientId=${sessionStorage.getItem("clientId")}`;
    return this.http.get(url)
  }
  // método para generar un string aleatorio para el state, para evitar CSRF
  private generateString(): string {
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
      context_uri: uri 
    };
    return this.http.put(`${this.spotiV1Url}/me/player/play`, body, { headers });
}

  getPlaylists(): Observable<any> {
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${sessionStorage.getItem("spotify_access_token")}`
    });
    // a URL base (https://api.spotify.com/v1) + el endpoint
    let url = `${this.spotiV1Url}/me/playlists`;
  return this.http.get<any>(url, { headers });
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
  prepareSongPayment(songName: string, email: string): Observable<any> {
    return this.http.post('http://127.0.0.1:8080/payments/prepay-song', { songName, email });
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
    return this.http.get(`${this.spotiV1Url}/me/player/queue`, { headers });
  }
}
