// src/app/music/music.component.ts
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SpotifyService } from '../spotify'; 

declare var Stripe: any;

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

  searchQuery: string = '';
  searchResults: any[] = [];
  searchError?: string;
  
  stripe: any;
  elements: any;
  card: any;

  showPaymentModal: boolean = false; // Controla si se ve la ventana
  selectedTrack: any = null;         // Canción elegida
  isProcessing: boolean = false;     // Para bloquear el botón
  paymentError: string = '';

  isAdmin: boolean = false;

  stripePublicKey = 'pk_test_51SIV0yRm0ClsCnoVWXB3iOiEfdtda0z61OvJDYLWIIAq5FQZuIdFOAb4sEwtk8w2eEooAbJXOSKxsuGw3j56g5G900aYokx6Qx';

  constructor(private spoti : SpotifyService) {}

  ngOnInit(): void {
    this.getDevices();
    this.getPlaylists();
    this.getCurrentPlayList();
    this.stripe = Stripe(this.stripePublicKey);
  }

  toggleMode() {
    this.isAdmin = !this.isAdmin;
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
        if (!this.devices || this.devices.length === 0) {
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

  getPlaylists() {
    this.resetErrors();
    this.spoti.getPlaylists().subscribe({
      next: (result) => {
        // La API de Spotify devuelve un objeto con una propiedad 'items' que es el array de playlists
        this.playlists = result.items; 
      },
      error: (err) => {
        // Manejo de errores parecido al de devices
        if (err.error && err.error.message) {
          this.playlistError = err.error.message;
        } else {
          this.playlistError = 'Error al cargar playlists';
        }
      }
    });
  }
  selectPlaylist(item: any) {
    console.log('Activando playlist:', item.name);

    // 1. Mandamos la orden de REPRODUCIR a Spotify
    this.spoti.playContext(item.uri).subscribe({
      next: () => {
        console.log('Reproduciendo...');
        
        // 2. Ahora pedimos la lista de canciones para rellenar TU pantalla
        this.loadPlaylistTracks(item.id);
      },
      error: (err) => {
        console.error('Error al reproducir:', err);
        // El error 404 suele ser "No active device"
        if(err.status === 404) alert("Abre Spotify primero para que suene.");
      }
    });
  }

  loadPlaylistTracks(playlistId: string) {
  this.spoti.getPlaylistTracks(playlistId).subscribe((response: any) => {
    this.queue = response.items.map((item: any) => item.track);
    console.log('Cola visual actualizada:', this.queue);
  });
}
  
  getCurrentPlayList() {
    if (this.playlists.length > 0) {
      this.selectPlaylist(this.playlists[0]);
    }
  }

  search() {
    if (!this.searchQuery) return;

    this.searchError = undefined;
    console.log('Buscando:', this.searchQuery);

    this.spoti.search(this.searchQuery).subscribe({
      next: (result) => {
        // Guardamos los resultados que nos da Spotify
        this.searchResults = result.tracks.items;
        console.log('Resultados:', this.searchResults);
      },
      error: (err) => {
        console.error(err);
        this.searchError = 'Error al buscar la canción';
      }
    });
  } 

  openPaymentModal(track: any) {
    this.selectedTrack = track;
    this.showPaymentModal = true;
    this.paymentError = '';

    // Esperamos 100ms a que aparezca el HTML para "pintar" la tarjeta dentro
    setTimeout(() => {
      this.mountStripeCard();
    }, 100);
  }

  mountStripeCard() {
    if (!this.elements) {
      this.elements = this.stripe.elements();
      this.card = this.elements.create('card'); // Crea campo nº tarjeta, fecha, CVC
    }
    this.card.mount('#card-element'); // Lo mete en el div con ese ID
  }

  // 2. CERRAR VENTANA
  cancelPayment() {
    this.showPaymentModal = false;
    if (this.card) {
      this.card.unmount(); // Limpiamos para evitar errores
      this.elements = null;
    }
  }

  // 3. PAGAR DE VERDAD (Conecta Front -> Java -> Stripe)
  async confirmPayment() {
    this.isProcessing = true;
    this.paymentError = '';

    // A) Pedimos permiso al Backend (Java)
    // Usamos el método nuevo que acabas de crear en spotify.ts
    this.spoti.prepareSongPayment(this.selectedTrack.name).subscribe({
      next: async (res) => {
        const clientSecret = res.clientSecret; // El permiso del servidor

        // B) Confirmamos con Stripe (Front)
        const result = await this.stripe.confirmCardPayment(clientSecret, {
          payment_method: { card: this.card }
        });

        if (result.error) {
          // Fallo (tarjeta rechazada, etc.)
          this.paymentError = result.error.message;
          this.isProcessing = false;
        } else {
          if (result.paymentIntent.status === 'succeeded') {
            console.log('Pago OK:', result.paymentIntent.id);
            this.addSongToQueue(this.selectedTrack); // Llamamos a tu método de añadir

            let barName = sessionStorage.getItem('barName');
            if (!barName) barName = "Bar no identificado";

          // Llamamos a guardar
          this.spoti.saveSongInDb(this.selectedTrack, barName).subscribe({
          error: (e) => console.error('Error al guardar', e)
          });
            
            this.cancelPayment(); // Cerramos modal
            this.isProcessing = false;
          }
        }
      },
      error: (err) => {
        console.error(err);
        this.paymentError = 'Error al conectar con el servidor de pagos.';
        this.isProcessing = false;
      }
    });
  }

  // TU MÉTODO PARA AÑADIR A LA COLA 
  addSongToQueue(track: any) {
    this.spoti.addToQueue(track.uri).subscribe({
      next: () => {
        alert(`Pago Recibido, "${track.name}" añadida a la cola.`);
        this.searchQuery = '';
        this.searchResults = [];
        this.queue.push(track);
      },
      error: (err) => this.songError = 'Se cobró, pero no se pudo añadir a la cola.'
    });
  }
}
